package com.eventmanagement.EventManagement.repository;

import com.eventmanagement.EventManagement.model.entity.PaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentEventRepository extends JpaRepository<PaymentEvent,String> {
}
