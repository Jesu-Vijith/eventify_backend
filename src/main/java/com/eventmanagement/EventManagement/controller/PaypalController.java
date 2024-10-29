package com.eventmanagement.EventManagement.controller;

import com.eventmanagement.EventManagement.exception.CustomException;
import com.eventmanagement.EventManagement.model.entity.Ticket;
import com.eventmanagement.EventManagement.repository.TicketRepository;
import com.eventmanagement.EventManagement.service.PaypalService;
import com.paypal.api.payments.Links;

import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/paypal")

public class PaypalController {

    @Autowired
    private PaypalService paypalService;

    @Autowired
    private TicketRepository ticketRepository;

    @GetMapping("/success")
    public ResponseEntity<String> PaymentSuccess(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId, @RequestParam("ticketId") String ticketId) {
        try {
            Payment payment = paypalService.executePayment(paymentId, payerId);

            if (payment.getState().equals("approved")) {
                Ticket ticket = ticketRepository.findById(ticketId)
                        .orElseThrow(() -> new CustomException("Ticket not found", HttpStatus.NOT_FOUND));
                String salesId=payment.getTransactions().get(0).getRelatedResources().get(0).getSale().getId();
                String paymentConfirmation=paypalService.confirmPayment(ticket,paymentId,payerId,salesId);

                return ResponseEntity.ok(paymentConfirmation);

            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment was not approved.");
            }
        } catch (Exception e) {
            throw new CustomException("Payment verification failed.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

