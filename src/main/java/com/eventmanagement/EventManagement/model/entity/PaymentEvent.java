package com.eventmanagement.EventManagement.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String paymentId;

   private String eventId;

    private String paymentMethod;

    private Long amount;

    private String paymentStatus;

    private Date paymentDate;

    private String paypalPaymentId;

    private String payerId;

    private String saleId;

}

