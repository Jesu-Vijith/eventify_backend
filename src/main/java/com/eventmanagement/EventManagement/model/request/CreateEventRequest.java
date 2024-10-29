package com.eventmanagement.EventManagement.model.request;

import com.eventmanagement.EventManagement.model.dto.EventDTO;
import com.eventmanagement.EventManagement.model.dto.SeatingDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateEventRequest {
    public String organizerId;
    public EventDTO event;
    public SeatingDTO seating;
}
