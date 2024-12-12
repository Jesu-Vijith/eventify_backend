package com.eventmanagement.EventManagement.service;

import com.eventmanagement.EventManagement.exception.CustomException;
import com.eventmanagement.EventManagement.model.entity.Event;
import com.eventmanagement.EventManagement.model.entity.RoleEnum;
import com.eventmanagement.EventManagement.model.entity.Seating;
import com.eventmanagement.EventManagement.model.entity.User;
import com.eventmanagement.EventManagement.repository.EventRepository;
import com.eventmanagement.EventManagement.repository.SeatingRepository;
import com.eventmanagement.EventManagement.repository.TicketRepository;
import com.eventmanagement.EventManagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private SeatingRepository seatingRepository;

    public Map<String,Long> adminDashboard() {
        Map<String,Long>overview=new HashMap<>();
        Long activeEvents=eventRepository.countActiveEvents();
        Long totalEvents=eventRepository.count();
        Long totalActiveOrganizers=userRepository.countActiveUsersByRole(RoleEnum.ORGANIZER);
        Long totalActiveAttendees=userRepository.countActiveUsersByRole(RoleEnum.ATTENDEE);
        Long totalTicketsSold=ticketRepository.countTicketsWithPaymentDone();
        overview.put("Active Events",activeEvents);
        overview.put("Total Events",totalEvents);
        overview.put("Total Active Organizers",totalActiveOrganizers);
        overview.put("Total Active Attendees",totalActiveAttendees);
        overview.put("Total Tickets Sold",totalTicketsSold);
        return overview;
    }


    public Map<String, Long> eventDetailsById(String eventId) {
        Event event=eventRepository.findByEventId(eventId)
                .orElseThrow(()-> new CustomException("Event Not Found"));
        Seating seating=seatingRepository.findBySeatingId(event.getSeating().getSeatingId())
                .orElseThrow(()->new CustomException("Seating Not Found"));
        Map<String,Long>eventDetails= new HashMap<>();
        eventDetails.put("Total Number of Seats",seating.getTotalNoOfSeats());
        eventDetails.put("Total Number of Early Bird Seats",seating.getNumberOfEarlyBird());
        eventDetails.put("Total Number of Vip Seats",seating.getNumberOfVipSeats());
        eventDetails.put("Total Number of General Seats",seating.getNumberOfGeneralSeats());
        eventDetails.put("Total Number of Seats Booked",seating.getTotalNumberOfSeatsBooked());
        eventDetails.put("Total Number of Early Bird Seats Booked",seating.getTotalNumberOfEarlyBirdsBooked());
        eventDetails.put("Total Number of General Seats Booked",seating.getTotalNumberOfGeneralSeatsBooked());
        eventDetails.put("Total Number of Vip Seats Booked",seating.getTotalNumberOfVipSeatsBooked());
        eventDetails.put("Total Number of Seats Available",seating.getTotalNumberOfSeatsAvailable());
        eventDetails.put("Total Number of Early Bird Seats Available",seating.getTotalNumberOfEarlyBirdsAvailable());
        eventDetails.put("Total Number of General Seats Available",seating.getTotalNumberOfGeneralSeatsAvailable());
        eventDetails.put("Total Number of Vip Seats Available",seating.getTotalNumberOfVipSeatsAvailable());
        return eventDetails;
    }


    public List<User> getAllAttendee() {
       List<User>users= userRepository.findUsersByRole(RoleEnum.ATTENDEE);
       return users;
    }

//    public List<User> getAllOrganizer() {
//        List<User>users= userRepository.findUsersByRole(RoleEnum.ORGANIZER);
//        return users;
//    }

    public User findById(String userId) {
        User user=userRepository.findById(userId)
                .orElseThrow(()->new CustomException("User Not Found"));
        return user;
    }


    public String blockUser(String userId) {
        User user=userRepository.findByUserId(userId)
                .orElseThrow(()->new CustomException("User Not Found"));
        user.setIsActive(false);
        userRepository.save(user);
        return "User is Blocked Successfully!";
    }

    public String unblockUser(String userId) {
        User user=userRepository.findByUserId(userId)
                .orElseThrow(()->new CustomException("User Not Found"));
        user.setIsActive(true);
        userRepository.save(user);
        return "User is Unblocked Successfully!";
    }

    public String blockEvent(String eventId) {
        Event event=eventRepository.findByEventId(eventId)
                .orElseThrow(()->new CustomException("Event Not Found"));
        event.setIsActive(false);
        eventRepository.save(event);
        return "Event is Blocked Successfully!";
    }

    public String unblockEvent(String eventId) {
        Event event=eventRepository.findByEventId(eventId)
                .orElseThrow(()->new CustomException("Event Not Found"));
        event.setIsActive(true);
        eventRepository.save(event);
        return "Event is Unblocked Successfully!";
    }

    public List<User> getAllIndividualOrganizers() {
        return userRepository.findByOrganizerType(User.OrganizerType.INDIVIDUAL);
    }

    // Fetch all company organizers
    public List<User> getAllCompanyOrganizers() {
        return userRepository.findByOrganizerType(User.OrganizerType.COMPANY);
    }
}
