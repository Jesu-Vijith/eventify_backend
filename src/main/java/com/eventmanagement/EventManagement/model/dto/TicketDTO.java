package com.eventmanagement.EventManagement.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;



@Data
public class TicketDTO {

    private String eventId;
    private String attendeeId;
    private Long totalSeats;

    @JsonProperty("isVip")
    private boolean isVip;

    @JsonProperty("isEarlyBird")
    private boolean isEarlyBird;

    private Long totalEarlyBirdSeats;
    private Long totalVipSeats;
    private Long totalGeneralSeats;

}
