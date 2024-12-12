package com.eventmanagement.EventManagement.controller;

import com.beust.ah.A;
import com.eventmanagement.EventManagement.model.entity.Event;
import com.eventmanagement.EventManagement.model.entity.User;
import com.eventmanagement.EventManagement.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {


    private final AdminService service;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Long>> adminDashboard() {
        Map<String, Long> dashboardList = service.adminDashboard();
        return ResponseEntity.ok(dashboardList);
    }

    //Get all events in ticketService

    @GetMapping("/eventDetailsById/{eventId}")
    public ResponseEntity<Map<String, Long>> eventDetailsById(@PathVariable String eventId) {
        Map<String, Long> eventDetailsById = service.eventDetailsById(eventId);
        return ResponseEntity.ok(eventDetailsById);
    }

    @GetMapping("/getAllAttendee")
    public ResponseEntity<List<User>> getAllAttendee() {
        List<User> getAllAttendee = service.getAllAttendee();
        return ResponseEntity.ok(getAllAttendee);
    }


    @GetMapping("/organizers/individual")
    public List<User> getIndividualOrganizers() {
        return service.getAllIndividualOrganizers();
    }

    // Endpoint to get all company organizers
    @GetMapping("/organizers/company")
    public List<User> getCompanyOrganizers() {
        return service.getAllCompanyOrganizers();
    }


    //Both organizer and attendee
    @GetMapping("/getUserById/{userId}")
    public ResponseEntity<User>getAttendeeById(String userId){
        User user=service.findById(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/blockUser/{userId}")
    public ResponseEntity<String> blockUser(@PathVariable String userId){
        String block=service.blockUser(userId);
        return ResponseEntity.ok(block);
    }

    @PutMapping("/blockEvent/{eventId}")
    public ResponseEntity<String> blockEvent(@PathVariable String eventId){
        String block=service.blockEvent(eventId);
        return ResponseEntity.ok(block);
    }

    @PutMapping("/unblockUser/{userId}")
    public ResponseEntity<String>unblockUser(@PathVariable String userId){
        String block=service.unblockUser(userId);
        return ResponseEntity.ok(block);
    }

    @PutMapping("/unblockEvent/{eventId}")
    public ResponseEntity<String> unblockEvent(@PathVariable String eventId){
        String block=service.unblockEvent(eventId);
        return ResponseEntity.ok(block);
    }
}
