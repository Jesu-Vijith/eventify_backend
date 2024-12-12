package com.eventmanagement.EventManagement.model.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Time;
import java.time.LocalTime;
import java.util.Date;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    @Id
    private String eventId;


    private String organizerId;
    private String eventTitle;
    private String eventType;

    @Column(length = 1000)
    private String description;
    private String location;
    @JsonFormat(shape = JsonFormat.Shape.STRING , pattern = "yyyy-MM-dd")
    private Date date;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime time;



    private String organizerName;
    private String organizerEmail;

    private Boolean isActive;

    private Boolean isPaymentDone;

    //private boolean EventExpiration;
    @JsonFormat(shape = JsonFormat.Shape.STRING , pattern = "yyyy-MM-dd")
    private Date createdOn;
    private String createdBy;
    @JsonFormat(shape = JsonFormat.Shape.STRING , pattern = "yyyy-MM-dd")
    private Date updatedOn;
    private String updatedBy;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "seating_id", referencedColumnName = "seatingId")
    private Seating seating;

}
