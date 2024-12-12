package com.eventmanagement.EventManagement.service;

import com.eventmanagement.EventManagement.exception.CustomException;
import com.eventmanagement.EventManagement.model.dto.TicketDTO;
import com.eventmanagement.EventManagement.model.entity.*;
import com.eventmanagement.EventManagement.repository.*;

import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Service
public class TicketService extends HelperService{

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SeatingRepository seatingRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private PaypalService paypalService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    public String bookTicket(TicketDTO ticketReq) {
        try {

            if((ticketReq.getTotalEarlyBirdSeats()+ticketReq.getTotalVipSeats()+ticketReq.getTotalGeneralSeats())>ticketReq.getTotalSeats()
                    || (ticketReq.getTotalEarlyBirdSeats()+ticketReq.getTotalVipSeats()+ticketReq.getTotalGeneralSeats())<ticketReq.getTotalSeats()){
                throw new CustomException("Enter the seats count properly",HttpStatus.BAD_REQUEST);
            }
            Ticket ticket = new Ticket();
            Event event=eventRepository.findByEventId(ticketReq.getEventId())
                    .orElseThrow(()->new CustomException("Event Not Found",HttpStatus.NOT_FOUND));

            if(!event.getIsActive()){
                throw new CustomException("Event is Blocked.Try Again Later");
            }
            Seating seating=event.getSeating();
            if(seating.getTotalNumberOfSeatsAvailable()==0){
                throw new CustomException("Seats are full. Try another event",HttpStatus.NOT_FOUND);
            }
            if((ticketReq.getTotalSeats()>seating.getTotalNumberOfSeatsAvailable())){
                throw new CustomException("Only "+seating.getTotalNumberOfSeatsAvailable()+" seats are available",HttpStatus.BAD_REQUEST);
            }
            if((ticketReq.getTotalEarlyBirdSeats()>seating.getTotalNumberOfEarlyBirdsAvailable())){
                if(seating.getTotalNumberOfEarlyBirdsAvailable()==0){
                    throw new CustomException("Early Birds seats are full. Try other seats",HttpStatus.NOT_FOUND);
                }
                throw new CustomException("Only "+seating.getTotalNumberOfEarlyBirdsAvailable()+" Early Bird seats are available",HttpStatus.BAD_REQUEST);
            }
            if((ticketReq.getTotalVipSeats()>seating.getTotalNumberOfVipSeatsAvailable())){
                if(seating.getTotalNumberOfVipSeatsAvailable()==0){
                    throw new CustomException("Vip seats are full. Try other seats",HttpStatus.NOT_FOUND);
                }
                throw new CustomException("Only "+seating.getTotalNumberOfVipSeatsAvailable()+" Vip seats are available",HttpStatus.BAD_REQUEST);
            }
            if((ticketReq.getTotalGeneralSeats()>seating.getTotalNumberOfGeneralSeatsAvailable())){
                if(seating.getTotalNumberOfGeneralSeatsAvailable()==0){
                    throw new CustomException("General seats are full. Try other seats",HttpStatus.NOT_FOUND);
                }
                throw new CustomException("Only "+seating.getTotalNumberOfGeneralSeatsAvailable()+" General seats are available",HttpStatus.BAD_REQUEST);
            }

            ticket.setEventId(event.getEventId());
            ticket.setEventTitle(event.getEventTitle());
            ticket.setAttendeeId(ticketReq.getAttendeeId());
            String attendeeName = userRepository.findNameByUserId(ticket.getAttendeeId())
                    .orElseThrow(() -> new CustomException("Attendee Not Found", HttpStatus.NOT_FOUND));
            ticket.setAttendeeName(attendeeName);

            ticket.setTotalNumberOfSeatsBooked(ticketReq.getTotalSeats());

            ticket.setVip(ticketReq.isVip());
            ticket.setEarlyBird(ticketReq.isEarlyBird());
            long vipCost = seating.getCostOfVip();
            long earlyBirdCost = seating.getCostOfEarlyBird();
            long generalCost = seating.getCostOfGeneral();
            if (!ticket.isVip()||!seating.getIsVip()) {
                ticket.setVip(false);
                ticket.setTotalVipSeatsBooked(0L);
                vipCost = 0;
            } else {
                ticket.setTotalVipSeatsBooked(ticketReq.getTotalVipSeats());
                vipCost = ticket.getTotalVipSeatsBooked() * vipCost;
            }
            if (!ticket.isEarlyBird()||!seating.getIsEarlyBird()) {
                ticket.setEarlyBird(false);
                ticket.setTotalEarlyBirdSeatsBooked(0L);
                earlyBirdCost = 0;
            } else {
                ticket.setTotalEarlyBirdSeatsBooked(ticketReq.getTotalEarlyBirdSeats());
                earlyBirdCost = ticket.getTotalEarlyBirdSeatsBooked() * earlyBirdCost;
            }
            ticket.setTotalGeneralSeatsBooked(ticketReq.getTotalGeneralSeats());
            generalCost=ticket.getTotalGeneralSeatsBooked()*generalCost;
            System.out.println(vipCost+" "+earlyBirdCost+" "+generalCost);
            ticket.setCost(vipCost + earlyBirdCost + generalCost);
            ticket.setPaymentDone(false);
            ticket.setTicketActive(false);
            ticket.setDate(event.getDate());
            ticket.setBookedBy(attendeeName);
            ticket.setBookedOn(new Date());
            ticket.setTicketId(event.getEventId()+"_TKT_"+idGenerator());
            ticketRepository.save(ticket);

            String cancelUrl = "http://localhost:8080/api/paypal/cancel";
            String successUrl = "http://localhost:8080/api/paypal/success?ticketId=" + ticket.getTicketId();
            Payment payment = paypalService.createPayment(
                    (double) ticket.getCost(), "USD", "paypal", "sale",
                    "Event ticket booking", cancelUrl, successUrl);
            for (Links link : payment.getLinks()) {
                if (link.getRel().equals("approval_url")) {
                    return link.getHref();
                }
            }

        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            throw new CustomException("An unexpected error occurred while booking the ticket", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return "/payment/error";
    }


    public List<Ticket> getAllTickets(String attendeeId) {
        try {
            List<Ticket> tickets = ticketRepository.findAllByAttendeeIdAndIsTicketActive(attendeeId);
            return tickets;

        }
     catch (DataAccessException e) {
        throw new CustomException("Database access error occurred while fetching tickets", HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (Exception e) {
        throw new CustomException("An unexpected error occurred while retrieving tickets", HttpStatus.INTERNAL_SERVER_ERROR);
    }
    }

    public Ticket getTicketById(String ticketId) {
        Ticket ticket=ticketRepository.findById(ticketId)
                .orElseThrow(()->new CustomException("Ticket Not Found",HttpStatus.NOT_FOUND));
        return ticket;
    }

    public List<Event> getAllEvents() {
        List<Event>events=eventRepository.findAll();
        return events;
    }

    public void cancelTicket(String ticketId) {
        Ticket ticket=ticketRepository.findById(ticketId)
                .orElseThrow(()->new CustomException("Ticket Not Found",HttpStatus.NOT_FOUND));
        if(!ticket.isPaymentDone()){
            ticket.setTicketActive(false);
        }
        else{
        Event event=eventRepository.findByEventId(ticket.getEventId())
                .orElseThrow(()->new CustomException("Event Not Found",HttpStatus.NOT_FOUND));
        Seating seating=event.getSeating();

        seating.setTotalNumberOfSeatsBooked(seating.getTotalNumberOfSeatsBooked()
                -ticket.getTotalNumberOfSeatsBooked());
        seating.setTotalNumberOfGeneralSeatsBooked(seating.getTotalNumberOfGeneralSeatsBooked()
                -ticket.getTotalGeneralSeatsBooked());
        seating.setTotalNumberOfVipSeatsBooked(seating.getTotalNumberOfVipSeatsBooked()
                -ticket.getTotalVipSeatsBooked());
        seating.setTotalNumberOfEarlyBirdsBooked(seating.getTotalNumberOfEarlyBirdsBooked()
                -ticket.getTotalEarlyBirdSeatsBooked());

        seating.setTotalNumberOfSeatsAvailable(seating.getTotalNumberOfSeatsAvailable()
                +ticket.getTotalNumberOfSeatsBooked());
        seating.setTotalNumberOfGeneralSeatsAvailable(seating.getTotalNumberOfGeneralSeatsAvailable()
                +ticket.getTotalGeneralSeatsBooked());
        seating.setTotalNumberOfVipSeatsAvailable(seating.getTotalNumberOfVipSeatsAvailable()
                +ticket.getTotalVipSeatsBooked());
        seating.setTotalNumberOfEarlyBirdsAvailable(seating.getTotalNumberOfEarlyBirdsAvailable()
                +ticket.getTotalEarlyBirdSeatsBooked());
        seatingRepository.save(seating);
        Payments payment=paymentRepository.findByTicketId(ticketId)
                .orElseThrow(()->new CustomException("Payment Not Found"));
            System.out.println("Hii");
        ticket.setTicketActive(false);
        ticketRepository.save(ticket);
        paypalService.refundPayment(payment);
        }
    }

    public List<User> getAttendeesByEventId(String eventId) {
        List<String>attendeesId=ticketRepository.findAttendeesIdByEventId(eventId);
        List<User>users=new ArrayList<>();
        for(String attendeeId:attendeesId){
            System.out.println(attendeeId);
            User user=userRepository.findByUserId(attendeeId)
                    .orElseThrow(()->new CustomException("User Not Found"));
            users.add(user);
        }
        return users;
    }


}
