package com.eventmanagement.EventManagement.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
//import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.BitMatrix;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor

public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String ticketId;
    private String eventId;
    private String attendeeId;
    private String attendeeName;

//    @OneToMany(mappedBy = "ticketId",cascade = CascadeType.ALL)
//    private List<Seat>seat;

    private Long totalNumberOfSeatsBooked;
    private boolean isVip;
    private boolean isEarlyBird;

    private Long totalEarlyBirdSeatsBooked;
    private Long totalVipSeatsBooked;
    private Long totalGeneralSeatsBooked;

    private List<String>seatNames;

    @Lob
    private byte[] ticketQr;
    private Long cost;

    private boolean isPaymentDone;
    private boolean isTicketActive;

    @JsonFormat(shape = JsonFormat.Shape.STRING , pattern = "yyyy-MM-dd")
    private Date bookedOn;
    private String bookedBy;

    private Date cancelledOn;
    private String cancelledBy;

}
