package com.eventmanagement.EventManagement.service;

import com.eventmanagement.EventManagement.exception.CustomException;
import com.eventmanagement.EventManagement.model.entity.*;
import com.eventmanagement.EventManagement.model.entity.Event;
import com.eventmanagement.EventManagement.repository.*;
import com.eventmanagement.EventManagement.utils.QrCodeGenerator;
import com.paypal.api.payments.*;
import com.paypal.base.rest.PayPalRESTException;
import com.paypal.base.rest.APIContext;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Service
public class PaypalService {

    @Autowired
    private APIContext apiContext;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private  SeatingRepository seatingRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notifyService;

    @Autowired
    private RefundRepository refundRepository;

    public Payment createPayment(
            Double total,
            String currency,
            String method,
            String intent,
            String description,
            String cancelUrl,
            String successUrl
    ) throws PayPalRESTException {
        Amount amount = new Amount();
        amount.setCurrency(currency);
        amount.setTotal(String.format(Locale.forLanguageTag(currency), "%.2f", total));

        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod(method);

        Payment payment = new Payment();
        payment.setIntent(intent);
        payment.setPayer(payer);

        payment.setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(successUrl);
        payment.setRedirectUrls(redirectUrls);
        return payment.create(apiContext);
    }

    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        Payment payment = new Payment();
        payment.setId(paymentId);
        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);
        return payment.execute(apiContext, paymentExecution);

    }

    public String confirmPayment(Ticket ticket,String paymentId,String payerId,String salesId) {
        try {
            Payments payments=new Payments();
            payments.setTicket(ticket);
            payments.setPaymentMethod("paypal");
            payments.setAmount(ticket.getCost());
            payments.setPaymentStatus("SUCCESS");
            payments.setPaymentDate(new Date());
            payments.setPaypalPaymentId(paymentId);
            payments.setPayerId(payerId);
            payments.setSaleId(salesId);
            paymentRepository.save(payments);

            Event event = eventRepository.findByEventId(ticket.getEventId())
                    .orElseThrow(() -> new CustomException("Event Not Found"));

            Seating seating = seatingRepository.findBySeatingId(event.getSeating().getSeatingId())
                    .orElseThrow(() -> new CustomException("Seating Not Found for the given event"));

            User user=userRepository.findByUserId(ticket.getAttendeeId())
                    .orElseThrow(()->new CustomException("User Not Found"));
            String attendeeMail=user.getEmail();
            List<String> ticketNames = new ArrayList<>();
            for (int i = 0; i < ticket.getTotalEarlyBirdSeatsBooked(); i++) {
                String ticketName = SeatEnum.EARLY_BIRD + "_" + UUID.randomUUID();
                ticketNames.add(ticketName);
            }
            for (int i = 0; i < ticket.getTotalVipSeatsBooked(); i++) {
                String ticketName = SeatEnum.VIP + "_" + UUID.randomUUID();
                ticketNames.add(ticketName);
            }
            for (int i = 0; i < ticket.getTotalGeneralSeatsBooked(); i++) {
                String ticketName = SeatEnum.GENERAL + "_" + UUID.randomUUID();
                ticketNames.add(ticketName);
            }

            ticket.setSeatNames(ticketNames);
            byte[] ticketQr = QrCodeGenerator.generateQRCode(ticket);
            ticket.setTicketQr(ticketQr);
            ticket.setPaymentDone(true);
            ticket.setTicketActive(true);
            seating.setTotalNumberOfSeatsBooked(seating.getTotalNumberOfSeatsBooked() + ticket.getTotalNumberOfSeatsBooked());
            seating.setTotalNumberOfGeneralSeatsBooked(seating.getTotalNumberOfGeneralSeatsBooked() + ticket.getTotalGeneralSeatsBooked());
            seating.setTotalNumberOfVipSeatsBooked(seating.getTotalNumberOfVipSeatsBooked() + ticket.getTotalVipSeatsBooked());
            seating.setTotalNumberOfEarlyBirdsBooked(seating.getTotalNumberOfEarlyBirdsBooked() + ticket.getTotalEarlyBirdSeatsBooked());

            seating.setTotalNumberOfSeatsAvailable(seating.getTotalNumberOfSeatsAvailable() - ticket.getTotalNumberOfSeatsBooked());
            seating.setTotalNumberOfGeneralSeatsAvailable(seating.getTotalNumberOfGeneralSeatsAvailable() - ticket.getTotalGeneralSeatsBooked());
            seating.setTotalNumberOfVipSeatsAvailable(seating.getTotalNumberOfVipSeatsAvailable() - ticket.getTotalVipSeatsBooked());
            seating.setTotalNumberOfEarlyBirdsAvailable(seating.getTotalNumberOfEarlyBirdsAvailable() - ticket.getTotalEarlyBirdSeatsBooked());

            seatingRepository.save(seating);
            ticketRepository.save(ticket);
            notifyService.notifyAttendee(ticket,attendeeMail);
            return "Payment successful! Ticket booking confirmed.";
        }
        catch (Exception e) {
            throw new CustomException("Payment cancelled or failed");
        }
    }

    public void refundPayment(Payments payment) {
        if (payment.getPaypalPaymentId() == null || payment.getPaypalPaymentId().isEmpty()) {
            throw new CustomException("Payment ID is missing. Cannot initiate refund.", HttpStatus.BAD_REQUEST);
        }
        if (!payment.getPaymentStatus().equals("SUCCESS")) {
            throw new CustomException("Payment is not successful. Refund cannot be processed.", HttpStatus.BAD_REQUEST);
        }

        Sale sale = new Sale();
        sale.setId(payment.getSaleId());

        RefundRequest refundRequest = new RefundRequest();
        Amount refundAmount = new Amount();
        refundAmount.setCurrency("USD");
        refundAmount.setTotal(String.valueOf(payment.getAmount()));
        refundRequest.setAmount(refundAmount);

        try {
            Refund refund = sale.refund(apiContext, refundRequest);
            Refunds refunds = new Refunds();
            refunds.setPayment(payment);
            refunds.setRefundId(refund.getId());
            refunds.setRefundAmount(payment.getAmount());
            refunds.setRefundStatus(refund.getState());
            refunds.setRefundDate(new Date());

            refundRepository.save(refunds);
            payment.setPaymentStatus("REFUNDED");
            paymentRepository.save(payment);

        } catch (PayPalRESTException e) {
            throw new CustomException("An error occurred while processing the refund: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}


