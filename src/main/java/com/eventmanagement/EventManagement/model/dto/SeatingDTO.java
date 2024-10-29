package com.eventmanagement.EventManagement.model.dto;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatingDTO {

    private Long totalNoOfSeats;
    private Long numberOfGeneralSeats;

    private Boolean isVip;
    private Boolean isEarlyBird;

    private Long numberOfVipSeats;
    private Long numberOfEarlyBird;


    private Long costOfVip;
    private Long costOfEarlyBird;
    private Long costOfGeneral;

}