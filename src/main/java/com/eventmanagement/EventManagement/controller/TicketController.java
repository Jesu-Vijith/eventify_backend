package com.eventmanagement.EventManagement.controller;

import com.eventmanagement.EventManagement.model.dto.TicketDTO;
import com.eventmanagement.EventManagement.model.entity.Event;
import com.eventmanagement.EventManagement.model.entity.Ticket;
import com.eventmanagement.EventManagement.model.entity.User;
import com.eventmanagement.EventManagement.service.TicketService;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

@RestController
@RequestMapping("/api/ticket")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @PostMapping("/bookTicket")
    public ResponseEntity<String>bookTicket(@RequestBody TicketDTO ticket){
        String bookTicketUrl=ticketService.bookTicket(ticket);
        return ResponseEntity.ok(bookTicketUrl);
    }

    @GetMapping("/getAttendees/{eventId}")
    public ResponseEntity<List<User>> getAttendeesByEventId(@PathVariable String eventId){
        List<User>users=ticketService.getAttendeesByEventId(eventId);
        return ResponseEntity.ok(users);
    }


    //for fetching ticket details of attendee
    @GetMapping("/getAllTickets/{attendeeId}")
    public ResponseEntity<List<Ticket>> getAllTickets(@PathVariable String attendeeId){
        List<Ticket> tickets=ticketService.getAllTickets(attendeeId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/getTicketById/{ticketId}")
    public ResponseEntity<Ticket>getTicketById(@PathVariable String ticketId){
        Ticket ticket=ticketService.getTicketById(ticketId);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/getAllEvents")
    public ResponseEntity<List<Event>>getAllEvents(){
        List<Event>events=ticketService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @DeleteMapping("/cancelTicket/{ticketId}")
    public ResponseEntity<String>cancelTicket(@PathVariable String ticketId){
        ticketService.cancelTicket(ticketId);
        return ResponseEntity.ok("Ticket Cancelled successfully");
    }

}
