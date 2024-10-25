package com.example.RoomBooking.services;

import com.example.RoomBooking.models.Detail;
import com.example.RoomBooking.repositories.DetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class DetailService {

    @Autowired
    private final DetailRepository detailRepository;

    public DetailService(DetailRepository detailRepository) {
        this.detailRepository = detailRepository;
    }

    public void createDetail(Long userId, String name, BigDecimal amount) {
        Detail detail = new Detail(userId, name, amount);
        detailRepository.save(detail);
    }

    public void deleteDetail(Long id) {
        detailRepository.deleteById(id);
    }

    public Optional<Detail> getDetailById(Long id) {
        return detailRepository.findById(id);
    }
}
