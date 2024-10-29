package com.eventmanagement.EventManagement.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Seating {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String seatingId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "event_id", referencedColumnName = "eventId")
    private Event event;

    //fixed and can only be updated by organizer
    private Long totalNoOfSeats;
    private Long numberOfGeneralSeats;
    private Boolean isVip;
    private Boolean isEarlyBird;
    private Long numberOfVipSeats;
    private Long numberOfEarlyBird;
    private Long costOfVip;
    private Long costOfEarlyBird;
    private Long costOfGeneral;

    //dynamic and changes whenever attendee book tickets and also appear on Organizer dashboard
    private Long totalNumberOfSeatsBooked;
    private Long totalNumberOfGeneralSeatsBooked;
    private Long totalNumberOfVipSeatsBooked;
    private Long totalNumberOfEarlyBirdsBooked;

    private Long totalNumberOfSeatsAvailable;
    private Long totalNumberOfGeneralSeatsAvailable;
    private Long totalNumberOfVipSeatsAvailable;
    private Long totalNumberOfEarlyBirdsAvailable;

//    @PrePersist
//    @PreUpdate

}
