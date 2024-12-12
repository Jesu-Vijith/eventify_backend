package com.eventmanagement.EventManagement.repository;

import com.eventmanagement.EventManagement.model.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket,String> {

    @Query("SELECT t FROM Ticket t WHERE t.attendeeId = :attendeeId AND t.isTicketActive = true")
    List<Ticket> findAllByAttendeeIdAndIsTicketActive(@Param("attendeeId") String attendeeId);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.isPaymentDone = true")
    Long countTicketsWithPaymentDone();

    @Query("SELECT t FROM Ticket t WHERE t.eventId = :eventId AND t.isTicketActive = true")
    List<Ticket> findAllByEventIdAndIsTicketActive(String eventId);

    @Query("SELECT t.attendeeId FROM Ticket t WHERE t.eventId = :eventId")
    List<String> findAttendeesIdByEventId(@Param("eventId") String eventId);

}
