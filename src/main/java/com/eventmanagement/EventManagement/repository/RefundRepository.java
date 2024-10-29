package com.eventmanagement.EventManagement.repository;

import com.eventmanagement.EventManagement.model.entity.Refunds;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefundRepository extends JpaRepository<Refunds,String> {
}
