package com.eventmanagement.EventManagement.controller;

import com.eventmanagement.EventManagement.model.entity.Payments;
import com.eventmanagement.EventManagement.repository.PaymentRepository;
import com.paypal.api.payments.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentRepository paymentRepository;

    @GetMapping("/ticketPayments/{attendeeId}")
    public ResponseEntity<List<Payments>> ticketPayment(@PathVariable String attendeeId){
        List<Payments>payments=paymentRepository.findAllByAttendeeId(attendeeId);
        return ResponseEntity.ok(payments);

    }
}
