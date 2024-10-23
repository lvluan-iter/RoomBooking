package com.example.RoomBooking.services;

import com.example.RoomBooking.dto.BookingRequest;
import com.example.RoomBooking.exceptions.ResourceNotFoundException;
import com.example.RoomBooking.models.Booking;
import com.example.RoomBooking.models.Property;
import com.example.RoomBooking.models.User;
import com.example.RoomBooking.repositories.BookingRepository;
import com.example.RoomBooking.repositories.PropertyRepository;
import com.example.RoomBooking.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PropertyRepository propertyRepository;
    @Autowired
    private UserRepository userRepository;

    public void reserveProperty(BookingRequest bookingRequest) {
        // Kiểm tra xem bất động sản có sẵn để thuê không
        Property property = propertyRepository.findById(bookingRequest.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + bookingRequest.getPropertyId()));
        if (!property.isAvailable()) {
            throw new RuntimeException("Property is not available for booking.");
        }
        User user = userRepository.findById(bookingRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + bookingRequest.getUserId()));

        // Ghi lại giao dịch thuê
        Booking booking = new Booking();
        booking.setProperty(property);
        booking.setUser(user);
        booking.setStartDate(bookingRequest.getStartDate());
        booking.setEndDate(bookingRequest.getEndDate());
        booking.setTotalPrice(calculateTotalPrice(property.getPrice(), bookingRequest.getStartDate(), bookingRequest.getEndDate()));

        // Cập nhật trạng thái của bất động sản
        property.setAvailable(false);
        propertyRepository.save(property);

        // Lưu giao dịch thuê vào cơ sở dữ liệu
        bookingRepository.save(booking);
    }

    private double calculateTotalPrice(double pricePerNight, LocalDate startDate, LocalDate endDate) {
        long numberOfNights = ChronoUnit.DAYS.between(startDate, endDate);
        return pricePerNight * numberOfNights;
    }
}

