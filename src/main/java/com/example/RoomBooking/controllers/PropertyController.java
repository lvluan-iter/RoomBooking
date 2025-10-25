package com.example.RoomBooking.controllers;

import com.example.RoomBooking.dto.*;
import com.example.RoomBooking.exceptions.ResourceNotFoundException;
import com.example.RoomBooking.models.Property;
import com.example.RoomBooking.repositories.PropertyRepository;
import com.example.RoomBooking.services.DetailService;
import com.example.RoomBooking.services.PropertyService;
import com.example.RoomBooking.services.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.net.URI;
import java.sql.Timestamp;
import java.time.YearMonth;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/properties")
public class PropertyController {
    private final PropertyService propertyService;
    private final VNPayService vnPayService;
    private final DetailService detailService;
    private final PropertyRepository propertyRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getAllProperties() {
        List<PropertyResponse> response = propertyService.getAllProperties();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/available")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getAvailableProperties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {
        Page<PropertyResponse> properties = propertyService.getAvailableProperties(PageRequest.of(page, size));
        return ResponseEntity.ok(properties);
    }

    @PostMapping("/search")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> searchProperties(
            @RequestBody PropertySearchDTO searchDTO,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<PropertyResponse> properties = propertyService.searchProperties(searchDTO, PageRequest.of(page, size));
        return ResponseEntity.ok(properties);
    }

    @GetMapping("/searchNearby")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> searchNearbyProperties(@RequestParam String location,
                                                    @RequestParam Long propertyId,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PropertyResponse> properties = propertyService.searchNearBy(location, propertyId, pageable);
        return ResponseEntity.ok(properties);
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getPropertyById(@PathVariable Long id) {
        PropertyResponse property = propertyService.getPropertyById(id);
        return ResponseEntity.ok(property);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addProperty(@RequestBody PropertyRequest propertyRequest, HttpServletRequest request) {
        if (propertyRequest.isPaid()) {
            String reference = propertyService.createTempPropertyAndGetRef(propertyRequest);
            String paymentUrl = vnPayService.createPaymentUrl(propertyRequest, reference, request);

            return ResponseEntity.ok(Map.of(
                    "paymentUrl", paymentUrl,
                    "reference", reference
            ));
        } else {
            propertyService.addProperty(propertyRequest);
            return ResponseEntity.ok(Map.of("message", "Đăng tin miễn phí thành công!"));
        }
    }

    @GetMapping("/payment-callback")
    public ResponseEntity<?> paymentCallback(@RequestParam Map<String, String> queryParams) {
        try {
            String vnp_ResponseCode = queryParams.get("vnp_ResponseCode");
            String vnp_TransactionStatus = queryParams.get("vnp_TransactionStatus");
            String vnp_OrderInfo = queryParams.get("vnp_OrderInfo");
            String reference = queryParams.get("vnp_TxnRef");

            if (!"00".equals(vnp_ResponseCode) || !"00".equals(vnp_TransactionStatus)) {
                propertyService.deleteTempProperty(reference);
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create("https://propertyweb.onrender.com/payment-result?status=error"))
                        .build();
            }
            try {
                PropertyRequest propertyRequest = propertyService.getTempProperty(reference);

                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, 37);
                propertyRequest.setExpirationDate(new Timestamp(cal.getTimeInMillis()));

                propertyRequest.setApproved(true);
                propertyRequest.setPaid(true);
                propertyRequest.setAvailable(true);

                propertyService.addProperty(propertyRequest);

                BigDecimal amount = new BigDecimal(queryParams.get("vnp_Amount"))
                        .divide(new BigDecimal(100));

                detailService.createDetail(propertyRequest.getUserId(), vnp_OrderInfo, amount);
                propertyService.deleteTempProperty(reference);
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create("https://propertyweb.onrender.com/payment-result?status=success"))
                        .build();
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create("https://propertyweb.onrender.com/payment-result?status=system-error"))
                        .build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("https://propertyweb.onrender.com/payment-result?status=system-error"))
                    .build();
        }
    }

    @PostMapping("/{propertyId}/extend")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> extendProperty(@PathVariable Long propertyId, HttpServletRequest request) {
        String reference = UUID.randomUUID().toString();
        String paymentUrl = vnPayService.createExtensionPaymentUrl(propertyId, reference, request);

        String redisKey = "extension:" + reference;
        redisTemplate.opsForValue().set(redisKey, propertyId.toString(), 30, TimeUnit.MINUTES);

        Map<String, String> response = new HashMap<>();
        response.put("paymentUrl", paymentUrl);
        response.put("reference", reference);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/extension-callback")
    public ResponseEntity<?> extensionCallback(@RequestParam Map<String, String> queryParams) {
        try {
            String vnp_ResponseCode = queryParams.get("vnp_ResponseCode");
            String vnp_TransactionStatus = queryParams.get("vnp_TransactionStatus");
            String vnp_OrderInfo = queryParams.get("vnp_OrderInfo");
            String reference = queryParams.get("vnp_TxnRef");

            if (!"00".equals(vnp_ResponseCode) || !"00".equals(vnp_TransactionStatus)) {
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create("https://propertyweb.onrender.com/payment-result?status=error"))
                        .build();
            }

            String redisKey = "extension:" + reference;
            String propertyIdStr = redisTemplate.opsForValue().get(redisKey);

            if (propertyIdStr == null) {
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create("https://propertyweb.onrender.com/payment-result?status=error"))
                        .build();
            }

            Long propertyId = Long.parseLong(propertyIdStr);
            propertyService.processExtension(propertyId);

            BigDecimal amount = new BigDecimal(queryParams.get("vnp_Amount"))
                    .divide(new BigDecimal(100));
            Property property = propertyRepository.findById(propertyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
            detailService.createDetail(property.getUser().getId(), vnp_OrderInfo, amount);

            redisTemplate.delete(redisKey);

            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("https://propertyweb.onrender.com/payment-result?status=success"))
                    .build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("https://propertyweb.onrender.com/payment-result?status=system-error"))
                    .build();
        }
    }

    @PutMapping("/{propertyId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateProperty(@PathVariable Long propertyId, @RequestBody PropertyRequest propertyRequest) {
        propertyService.updateProperty(propertyId, propertyRequest);
        return ResponseEntity.ok(Map.of("message", "Chỉnh sửa thông tin thành công!"));
    }

    @DeleteMapping("/{propertyId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteProperty(@PathVariable Long propertyId) {
        propertyService.deleteProperty(propertyId);
        return ResponseEntity.ok(Map.of("message", "Xóa bất động sản thành công!"));
    }

    @GetMapping("/quick-stats/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserQuickStats(@PathVariable Long userId) {
        Map<String, Object> quickStats = propertyService.getQuickStatsForUser(userId);
        return ResponseEntity.ok(quickStats);
    }

    @GetMapping("/admin/quick-stats")
    @PreAuthorize("hasAnyRole('Owner', 'Admin')")
    public ResponseEntity<?> getAdminQuickStats() {
        Map<String, Object> quickStats = propertyService.getQuickStatsForAdmin();
        return ResponseEntity.ok(quickStats);
    }

    @GetMapping("/stats/{userId}")
    public ResponseEntity<?> getPropertyStats(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {

        List<PropertyStats> stats = propertyService.getPropertyStatsForUser(userId, yearMonth);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getPropertyForUser(@PathVariable Long userId) {
        List<PropertyResponse> responses = propertyService.getPropertyForUser(userId);
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{propertyId}/toggle-visibility")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> togglePropertyVisibility(@PathVariable Long propertyId) {
        propertyService.togglePropertyVisibility(propertyId);
        return ResponseEntity.ok(Map.of("message", "Property visibility toggled successfully"));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('Owner', 'Admin')")
    public ResponseEntity<?> approveListing(@PathVariable("id") Long propertyId) {
        propertyService.approveListing(propertyId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('Owner', 'Admin')")
    public ResponseEntity<?> rejectListing(
            @PathVariable("id") Long propertyId) {
        propertyService.rejectListing(propertyId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/lock")
    @PreAuthorize("hasAnyRole('Owner', 'Admin')")
    public ResponseEntity<?> lockListing(
            @PathVariable("id") Long propertyId,
            @RequestBody Map<String, Object> payload) {
        String reason = (String) payload.get("reason");
        propertyService.lockListing(propertyId, reason);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/unlock")
    @PreAuthorize("hasAnyRole('Owner', 'Admin')")
    public ResponseEntity<?> unlockListing(@PathVariable("id") Long propertyId) {
        propertyService.unlockListing(propertyId);
        return ResponseEntity.ok().build();
    }
}