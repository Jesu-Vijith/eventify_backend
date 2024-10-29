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
public class Payments {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String paymentId;

    @OneToOne
    @JoinColumn(name = "ticket_id" )
    private Ticket ticket;

    private String paymentMethod;

    private Long amount;

    private String paymentStatus;

    private Date paymentDate;

    private String paypalPaymentId;

    private String payerId;

    private String saleId;

}