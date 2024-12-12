package com.eventmanagement.EventManagement.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Time;
import java.time.LocalTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventDTO {


    private String eventTitle;
    private String eventType;
    private String description;

    private String location;
    private Date date;
    private LocalTime time;

}
