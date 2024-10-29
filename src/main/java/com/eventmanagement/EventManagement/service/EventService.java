package com.eventmanagement.EventManagement.service;

import com.eventmanagement.EventManagement.exception.CustomException;
import com.eventmanagement.EventManagement.model.entity.*;
import com.eventmanagement.EventManagement.model.request.CreateEventRequest;
import com.eventmanagement.EventManagement.model.request.UpdateEventRequest;
import com.eventmanagement.EventManagement.repository.EventRepository;
import com.eventmanagement.EventManagement.repository.PaymentRepository;
import com.eventmanagement.EventManagement.repository.TicketRepository;
import com.eventmanagement.EventManagement.repository.UserRepository;
import com.paypal.api.payments.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private PaypalService paypalService;

    public void createEvent(CreateEventRequest request) {
        Event event=new Event();

        String organizerName=userRepository.findNameByUserId(request.getOrganizerId())
                        .orElseThrow(()->new CustomException("Organizer Not Found", HttpStatus.NOT_FOUND));

        event.setOrganizerName(organizerName);
        event.setOrganizerId(request.getOrganizerId());

        event.setEventTitle(request.getEvent().getEventTitle());
        event.setEventType(request.getEvent().getEventType());
        event.setDescription(request.getEvent().getDescription());
        event.setLocation(request.getEvent().getLocation());
        event.setDate(request.getEvent().getDate());
        event.setTime(request.getEvent().getTime());

        Seating seating=new Seating();

        seating.setTotalNoOfSeats(request.getSeating().getTotalNoOfSeats());

        boolean isVipSeats=request.getSeating().getIsVip();
        seating.setIsVip(isVipSeats);
        seating.setNumberOfVipSeats(request.getSeating().getNumberOfVipSeats());
        seating.setCostOfVip(request.getSeating().getCostOfVip());
        if(!isVipSeats){
            seating.setNumberOfVipSeats(0L);
            seating.setCostOfVip(0L);
        }

        boolean isEarlyBird= request.getSeating().getIsEarlyBird();
        seating.setIsEarlyBird(isEarlyBird);
        seating.setNumberOfEarlyBird(request.getSeating().getNumberOfEarlyBird());
        seating.setCostOfEarlyBird(request.getSeating().getCostOfEarlyBird());
        if(!isEarlyBird){
            seating.setNumberOfEarlyBird(0L);
            seating.setCostOfEarlyBird(0L);
        }
        seating.setCostOfGeneral(request.getSeating().getCostOfGeneral());
        seating.setNumberOfGeneralSeats(seating.getTotalNoOfSeats()-(seating.getNumberOfVipSeats()+ seating.getNumberOfEarlyBird()));

        seating.setTotalNumberOfSeatsAvailable(seating.getTotalNoOfSeats());
        seating.setTotalNumberOfGeneralSeatsAvailable(seating.getNumberOfGeneralSeats());
        seating.setTotalNumberOfVipSeatsAvailable(seating.getNumberOfVipSeats());
        seating.setTotalNumberOfEarlyBirdsAvailable(seating.getNumberOfEarlyBird());

        seating.setTotalNumberOfSeatsBooked(0L);
        seating.setTotalNumberOfGeneralSeatsBooked(0L);
        seating.setTotalNumberOfVipSeatsBooked(0L);
        seating.setTotalNumberOfEarlyBirdsBooked(0L);

        event.setSeating(seating);
        event.setCreatedOn(new Date());
        event.setCreatedBy(organizerName);
        event.setUpdatedOn(new Date());
        event.setUpdatedBy(organizerName);

        eventRepository.save(event);
    }

    //Organizer Perspective
    public List<Event> getAllEvents(String userId) {
        User user=userRepository.findByUserId(userId)
                .orElseThrow(()->new CustomException("Organizer Not Found",HttpStatus.NOT_FOUND));
        List<Event>events=eventRepository.findAllByOrganizerId(userId);
        if(events.isEmpty()){
            throw new CustomException("No events registered for the given organizer",HttpStatus.NOT_FOUND);
        }
        return events;
    }


    public Event getEventById(String userId, String eventId) {
        User user=userRepository.findByUserId(userId)
                .orElseThrow(()->new CustomException("Organizer Not Found",HttpStatus.NOT_FOUND));
        Event event=eventRepository.findByOrganizerIDAndEventId(userId,eventId);
        if(event==null){
            throw new CustomException("Event Not Found",HttpStatus.NOT_FOUND);
        }
        return event;
    }

    public void deleteEvent(String eventId) {
        Event event=eventRepository.findByEventId(eventId)
                        .orElseThrow(()->new CustomException("Event not Found",HttpStatus.NOT_FOUND));
        List<Ticket>tickets=ticketRepository.findAllByEventId(eventId);
        for(Ticket ticket:tickets){
            Payments payment=paymentRepository.findByTicketId(ticket.getTicketId())
                    .orElseThrow(()->new CustomException("Payment Not Found"));
            paypalService.refundPayment(payment);
            ticketRepository.delete(ticket);
        }
        eventRepository.delete(event);
    }

    public Event updateEvent(String eventId, UpdateEventRequest request) {
        Event event=eventRepository.findByEventId(eventId)
                .orElseThrow(()->new CustomException("Event not Found",HttpStatus.NOT_FOUND));

        event.setEventTitle(request.getEvent().getEventTitle());
        event.setEventType(request.getEvent().getEventType());
        event.setDescription(request.getEvent().getDescription());
        event.setLocation(request.getEvent().getLocation());
        event.setDate(request.getEvent().getDate());
        event.setTime(request.getEvent().getTime());
        Seating seating=new Seating();

        seating.setTotalNoOfSeats(request.getSeating().getTotalNoOfSeats());

        boolean isVipSeats=request.getSeating().getIsVip();
        seating.setIsVip(isVipSeats);
        seating.setNumberOfVipSeats(request.getSeating().getNumberOfVipSeats());
        seating.setCostOfVip(request.getSeating().getCostOfVip());
        if(!isVipSeats){
            seating.setNumberOfVipSeats(0L);
            seating.setCostOfVip(0L);
        }

        boolean isEarlyBird= request.getSeating().getIsEarlyBird();
        seating.setIsEarlyBird(isEarlyBird);
        seating.setNumberOfEarlyBird(request.getSeating().getNumberOfEarlyBird());
        seating.setCostOfEarlyBird(request.getSeating().getCostOfEarlyBird());
        if(!isEarlyBird){
            seating.setNumberOfEarlyBird(0L);
            seating.setCostOfEarlyBird(0L);
        }
        seating.setCostOfGeneral(request.getSeating().getCostOfGeneral());
        seating.setNumberOfGeneralSeats(seating.getTotalNoOfSeats()-(seating.getNumberOfVipSeats()+ seating.getNumberOfEarlyBird()));
        event.setSeating(seating);

        seating.setTotalNumberOfSeatsAvailable(seating.getTotalNoOfSeats());
        seating.setTotalNumberOfGeneralSeatsAvailable(seating.getNumberOfGeneralSeats());
        seating.setTotalNumberOfVipSeatsAvailable(seating.getNumberOfVipSeats());
        seating.setTotalNumberOfEarlyBirdsAvailable(seating.getNumberOfEarlyBird());

        seating.setTotalNumberOfSeatsBooked(0L);
        seating.setTotalNumberOfGeneralSeatsBooked(0L);
        seating.setTotalNumberOfVipSeatsBooked(0L);
        seating.setTotalNumberOfEarlyBirdsBooked(0L);

        event.setUpdatedOn(new Date());
        event.setUpdatedBy(event.getOrganizerName());

        eventRepository.save(event);
        return event;
    }
}
