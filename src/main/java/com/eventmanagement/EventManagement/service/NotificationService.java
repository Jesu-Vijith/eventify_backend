package com.eventmanagement.EventManagement.service;

import com.eventmanagement.EventManagement.exception.CustomException;
import com.eventmanagement.EventManagement.model.entity.Event;
import com.eventmanagement.EventManagement.model.entity.Ticket;
import com.eventmanagement.EventManagement.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class NotificationService {
    private final EmailService emailService;


    private final EventRepository eventRepository;

    public void notifyAttendee(Ticket ticket,String email) {
        Event event=eventRepository.findByEventId(ticket.getEventId())
                .orElseThrow(()->new CustomException("Event Not Found"));

        String subject = "Your ticket has been successfully booked for the event: "
                +event.getEventTitle();

        String message = "Your ticketId for the event " + "'"+event.getEventTitle()+"'" +
                " is " + ticket.getTicketId() + ". Never forget to attend the event on " + event.getDate()+"."+"\n"
                +"THANK YOU !!";
        emailService.sendEmail(email, subject, message);
    }

    public void notifyOrganizer(Event event, String organizerMail) {
        String subject = "Event Booking Success!";

        String message= "Hey "+event.getOrganizerName() +", Your Payment for Event "+event.getEventId()+" is completed successfully!" +
                " Your event is now open for ticket Booking!"+"\n"+"THANK YOU!!";

        emailService.sendEmail(organizerMail,subject,message);
    }
}
