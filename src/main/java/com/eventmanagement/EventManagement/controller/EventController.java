package com.eventmanagement.EventManagement.controller;


import com.eventmanagement.EventManagement.model.entity.Event;
import com.eventmanagement.EventManagement.model.request.CreateEventRequest;
import com.eventmanagement.EventManagement.model.request.UpdateEventRequest;
import com.eventmanagement.EventManagement.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/event")
public class EventController {

    @Autowired
    private EventService eventService;

    @PostMapping("/create")
    public ResponseEntity<String> createEvent(@RequestBody CreateEventRequest request) {
        eventService.createEvent(request);
        return ResponseEntity.ok("Event is created");
    }

    @GetMapping("/getAllEvents/{userId}")
    public ResponseEntity<List<Event>> getAllEvents(@PathVariable String userId){
        List<Event>getAllEvents=eventService.getAllEvents(userId);
        return ResponseEntity.ok(getAllEvents);
    }

    @GetMapping("/getAllEventsPaymentFalse/{userId}")
    public ResponseEntity<List<Event>> getAllEventsPaymentFalse(@PathVariable String userId){
        List<Event>getAllEventPaymentFalse=eventService.getAllEventsPaymentFalse(userId);
        return ResponseEntity.ok(getAllEventPaymentFalse);
    }

    @GetMapping("/getEventById/{userId}/{eventId}")
    public ResponseEntity<Event> getEventById(@PathVariable String userId,
                                              @PathVariable String eventId){
        Event event=eventService.getEventById(userId,eventId);
        return ResponseEntity.ok(event);
    }

    @PutMapping("/update/{eventId}")
    public ResponseEntity<Event> updateEvent(@PathVariable String eventId,
                                             @RequestBody UpdateEventRequest request){
        Event event= eventService.updateEvent(eventId,request);
        return ResponseEntity.ok(event);
    }

    @DeleteMapping("/delete/{eventId}")
    public ResponseEntity<String>deleteEvent(@PathVariable String eventId){
        eventService.deleteEvent(eventId);
        return ResponseEntity.ok("Event is deleted");
    }

    @GetMapping("/payment/{eventId}")
    public ResponseEntity<String>eventPayment(@PathVariable String eventId){
        String paymentLink=eventService.getPaymentLink(eventId);
        return ResponseEntity.ok(paymentLink);
    }
}
