package com.example.RoomBooking.services;

import com.example.RoomBooking.models.Detail;
import com.example.RoomBooking.models.Property;
import com.example.RoomBooking.models.User;
import com.example.RoomBooking.repositories.DetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class DetailService {

    @Autowired
    private DetailRepository detailRepository;

    public Detail createDetail(User user, Property property, BigDecimal amount) {
        Detail detail = new Detail(user, property, amount);
        return detailRepository.save(detail);
    }

    public void deleteDetail(Long id) {
        detailRepository.deleteById(id);
    }

    public Optional<Detail> getDetailById(Long id) {
        return detailRepository.findById(id);
    }
}
