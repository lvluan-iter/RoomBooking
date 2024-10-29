package com.example.RoomBooking.controllers;

import com.example.RoomBooking.dto.*;
import com.example.RoomBooking.exceptions.ResourceNotFoundException;
import com.example.RoomBooking.models.Property;
import com.example.RoomBooking.repositories.PropertyRepository;
import com.example.RoomBooking.services.DetailService;
import com.example.RoomBooking.services.PropertyService;
import com.example.RoomBooking.services.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    @Autowired
    private final PropertyService propertyService;

    @Autowired
    private final VNPayService vnPayService;

    @Autowired
    private final DetailService detailService;

    @Autowired
    private final PropertyRepository propertyRepository;

    @Autowired
    private final RedisTemplate<String, String> redisTemplate;

    public PropertyController(PropertyService propertyService, VNPayService vnPayService, DetailService detailService, PropertyRepository propertyRepository, RedisTemplate<String, String> redisTemplate) {
        this.propertyService = propertyService;
        this.vnPayService = vnPayService;
        this.detailService = detailService;
        this.propertyRepository = propertyRepository;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping
    public ResponseEntity<List<PropertyResponse>> getAllLocations() {
        List<PropertyResponse> response = propertyService.getAllProperties();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/available")
    public ResponseEntity<Page<PropertyResponse>> getAvailableProperties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {
        Page<PropertyResponse> properties = propertyService.getAvailableProperties(PageRequest.of(page, size));
        return ResponseEntity.ok(properties);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<PropertyResponse>> getTop10PopularProperties() {
        List<PropertyResponse> popular = propertyService.getTop10PopularProperties();
        return ResponseEntity.ok(popular);
    }

    @PostMapping("/search")
    public ResponseEntity<Page<PropertyResponse>> searchProperties(
            @RequestBody PropertySearchDTO searchDTO,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<PropertyResponse> properties = propertyService.searchProperties(searchDTO, PageRequest.of(page, size));
        return ResponseEntity.ok(properties);
    }

    @GetMapping("/searchNearby")
    public ResponseEntity<?> searchNearbyProperties(@RequestParam String location,
                                                    @RequestParam Long propertyId,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "5") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<PropertyResponse> properties = propertyService.searchNearBy(location, propertyId, pageable);
            return ResponseEntity.ok(properties);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Error: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponse> getPropertyById(@PathVariable Long id) {
        PropertyResponse property = propertyService.getPropertyById(id);
        return ResponseEntity.ok(property);
    }

    @PostMapping
    public ResponseEntity<?> addProperty(@RequestBody PropertyRequest propertyRequest, HttpServletRequest request) {
        try {
            if (propertyRequest.isPaid()) {
                String reference = propertyService.createTempPropertyAndGetRef(propertyRequest);

                String paymentUrl = vnPayService.createPaymentUrl(propertyRequest, reference, request);

                Map<String, String> response = new HashMap<>();
                response.put("paymentUrl", paymentUrl);
                response.put("reference", reference);
                return ResponseEntity.ok(response);
            } else {
                propertyService.addProperty(propertyRequest);
                return ResponseEntity.ok(Map.of("message", "Đăng tin miễn phí thành công!"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing property: " + e.getMessage());
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
    public ResponseEntity<?> extendProperty(@PathVariable Long propertyId, HttpServletRequest request) {
        try {
            String reference = UUID.randomUUID().toString();
            String paymentUrl = vnPayService.createExtensionPaymentUrl(propertyId, reference, request);

            String redisKey = "extension:" + reference;
            redisTemplate.opsForValue().set(redisKey, propertyId.toString(), 30, TimeUnit.MINUTES);

            Map<String, String> response = new HashMap<>();
            response.put("paymentUrl", paymentUrl);
            response.put("reference", reference);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing extension: " + e.getMessage());
        }
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
    public ResponseEntity<?> updateProperty(@PathVariable Long propertyId, @RequestBody PropertyRequest propertyRequest) {
        propertyService.updateProperty(propertyId, propertyRequest);
        return ResponseEntity.ok(Map.of("message", "Chỉnh sửa thông tin thành công!"));
    }

    @DeleteMapping("/{propertyId}")
    public ResponseEntity<?> deleteProperty(@PathVariable Long propertyId) {
        propertyService.deleteProperty(propertyId);
        return ResponseEntity.ok(Map.of("message", "Xóa bất động sản thành công!"));
    }

    @GetMapping("/quick-stats/{userId}")
    public ResponseEntity<Map<String, Object>> getUserQuickStats(@PathVariable Long userId) {
        Map<String, Object> quickStats = propertyService.getQuickStatsForUser(userId);
        return ResponseEntity.ok(quickStats);
    }

    @GetMapping("/stats/{userId}")
    public ResponseEntity<List<PropertyStats>> getPropertyStats(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {

        List<PropertyStats> stats = propertyService.getPropertyStatsForUser(userId, yearMonth);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("user/{userId}")
    public ResponseEntity<List<PropertyResponse>> getPropertyForUser(@PathVariable Long userId) {
        List<PropertyResponse> responses = propertyService.getPropertyForUser(userId);
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{propertyId}/toggle-visibility")
    public ResponseEntity<?> togglePropertyVisibility(@PathVariable Long propertyId) {
        try {
            propertyService.togglePropertyVisibility(propertyId);
            return ResponseEntity.ok()
                    .body(Map.of("message", "Property visibility toggled successfully"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error toggling property visibility: " + e.getMessage()));
        }
    }
}