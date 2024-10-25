package com.example.RoomBooking.controllers;

import com.example.RoomBooking.dto.*;
import com.example.RoomBooking.repositories.PropertyRepository;
import com.example.RoomBooking.services.DetailService;
import com.example.RoomBooking.services.PropertyService;
import com.example.RoomBooking.services.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
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
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Payment failed. Response code: " + vnp_ResponseCode));
            }

            try {
                PropertyRequest propertyRequest = propertyService.getTempProperty(reference);

                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, 37);
                propertyRequest.setExpirationDate(new Timestamp(cal.getTimeInMillis()));

                propertyRequest.setApproved(true);
                propertyRequest.setPaid(true);

                propertyService.addProperty(propertyRequest);

                BigDecimal amount = new BigDecimal(queryParams.get("vnp_Amount"))
                        .divide(new BigDecimal(100));

                detailService.createDetail(propertyRequest.getUserId(), vnp_OrderInfo, amount);
                propertyService.deleteTempProperty(reference);
                return ResponseEntity.ok(Map.of("message", "Thanh toán thành công!"));

            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ErrorResponse("Error processing property data: " + e.getMessage()));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error in payment callback: " + e.getMessage()));
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

