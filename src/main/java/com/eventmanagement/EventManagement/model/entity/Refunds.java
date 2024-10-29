package com.eventmanagement.EventManagement.model.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor

public class Refunds {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String refundId;

    @OneToOne
    @JoinColumn(name = "payment_id")
    private Payments payment;
    private Long refundAmount;
    private String refundStatus;
    private Date refundDate;
}
