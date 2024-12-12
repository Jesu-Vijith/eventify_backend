package com.eventmanagement.EventManagement.repository;

import com.eventmanagement.EventManagement.model.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event,String> {

    @Query("SELECT e FROM Event e WHERE e.organizerId = :organizerId AND e.isPaymentDone = true")
    List<Event> findAllByOrganizerIdAndPaymentDone(@Param("organizerId") String organizerId);

    @Query("SELECT e FROM Event e WHERE e.organizerId = :organizerId AND e.isPaymentDone = false")
    List<Event> findAllByOrganizerIdAndPaymentNotDone(@Param("organizerId") String userId);


    @Query("SELECT e FROM Event e WHERE e.organizerId = :organizerId AND e.eventId = :eventId")
    Event findByOrganizerIDAndEventId(@Param("organizerId") String organizerId, @Param("eventId") String eventId);

    @Query("SELECT e FROM Event e WHERE e.eventId = :eventId")
    Optional <Event> findByEventId(@Param("eventId")String eventId);


    @Query("SELECT COUNT(e) FROM Event e WHERE e.isActive = true")
    Long countActiveEvents();



    long count();

//    @Query("SELECT s.event.eventId FROM Seating s WHERE s.seatingId = :seatingId")
//    Optional<String> findEventIdBySeatingId(@Param("seatingId") String seatingId);
//
}
