package com.example.RoomBooking.models;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "details")
public class Detail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "detail_date", nullable = false)
    private Timestamp detailDate;

    public Detail(Long userId, String name, BigDecimal amount) {
        this.userId = userId;
        this.name = name;
        this.amount = amount;
        this.detailDate = new Timestamp(System.currentTimeMillis());
    }

}
