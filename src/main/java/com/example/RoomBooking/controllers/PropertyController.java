package com.example.RoomBooking.controllers;

import com.example.RoomBooking.dto.*;
import com.example.RoomBooking.models.Property;
import com.example.RoomBooking.models.User;
import com.example.RoomBooking.repositories.PropertyRepository;
import com.example.RoomBooking.services.DetailService;
import com.example.RoomBooking.services.PropertyService;
import com.example.RoomBooking.services.VNPayService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private DetailService detailService;

    @Autowired
    private PropertyRepository propertyRepository;

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
            // Trả về một ResponseEntity với HttpStatus.BAD_REQUEST và thông báo lỗi dưới dạng JSON
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
    public ResponseEntity<?> addProperty(@RequestBody PropertyRequest propertyRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        try {
            System.out.println("isPaid value: " + propertyRequest.isPaid());
            if (propertyRequest.isPaid()) {
                Property tempProperty = propertyService.createTempProperty(propertyRequest);
                String propertyJson = new ObjectMapper().writeValueAsString(tempProperty);
                Cookie cookie = new Cookie("temp_property", Base64.getEncoder().encodeToString(propertyJson.getBytes()));
                cookie.setMaxAge(30 * 60);
                cookie.setPath("/");
                response.addCookie(cookie);

                String paymentUrl = vnPayService.createPaymentUrl(tempProperty, request);

                Map<String, String> responseUrl = new HashMap<>();
                responseUrl.put("paymentUrl", paymentUrl);
                return ResponseEntity.ok(responseUrl);
            } else {
                propertyService.addProperty(propertyRequest);
                Map<String, String> success = new HashMap<>();
                success.put("message", "Đăng tin miễn phí thành công!");
                return ResponseEntity.ok(success);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing property: " + e.getMessage());
        }
    }

    @GetMapping("/payment-callback")
    public ResponseEntity<?> paymentCallback(@RequestParam Map<String, String> queryParams,
                                             @CookieValue(name = "temp_property", required = false) String tempPropertyCookie) {
        try {
            String vnp_ResponseCode = queryParams.get("vnp_ResponseCode");
            BigDecimal amount = new BigDecimal(queryParams.get("vnp_Amount")).divide(new BigDecimal(100));

            if ("00".equals(vnp_ResponseCode) && tempPropertyCookie != null) {
                byte[] decodedBytes = Base64.getDecoder().decode(tempPropertyCookie);
                String propertyJson = new String(decodedBytes);
                PropertyRequest tempProperty = new ObjectMapper().readValue(propertyJson, PropertyRequest.class);

                Property property = new Property();
                BeanUtils.copyProperties(tempProperty, property);

                property.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                property.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, 37);
                property.setExpirationDate(new Timestamp(cal.getTimeInMillis()));
                property.setApproved(true);
                property.setPaid(true);

                Property savedProperty = propertyRepository.save(property);

                detailService.createDetail(savedProperty.getUser(), savedProperty, amount);

                Map<String, String> success = new HashMap<>();
                success.put("message", "Thanh toán thành công!");
                return ResponseEntity.ok(success);
            } else {
                return ResponseEntity.badRequest().body("Payment failed or invalid property data");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing payment callback: " + e.getMessage());
        }
    }

    @PutMapping("/{propertyId}")
    public ResponseEntity<?> updateProperty(@PathVariable Long propertyId, @RequestBody PropertyRequest propertyRequest) {
        propertyService.updateProperty(propertyId, propertyRequest);
        return ResponseEntity.ok("Property updated successfully.");
    }

    @DeleteMapping("/{propertyId}")
    public ResponseEntity<?> deleteProperty(@PathVariable Long propertyId) {
        propertyService.deleteProperty(propertyId);
        return ResponseEntity.ok("Property deleted successfully.");
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
}

