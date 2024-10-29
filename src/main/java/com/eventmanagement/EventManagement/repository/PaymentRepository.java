package com.eventmanagement.EventManagement.repository;

import com.eventmanagement.EventManagement.model.entity.Payments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payments, String> {

    @Query("SELECT p FROM Payments p WHERE p.ticket.ticketId = :ticketId")
    Optional<Payments> findByTicketId(@Param("ticketId") String ticketId);
}
