package com.eventmanagement.EventManagement.repository;

import com.eventmanagement.EventManagement.model.entity.Seating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeatingRepository extends JpaRepository<Seating,String> {

    Optional<Seating> findBySeatingId(String seatingId);
}
