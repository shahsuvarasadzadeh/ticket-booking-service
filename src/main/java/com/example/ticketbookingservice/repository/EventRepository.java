package com.example.ticketbookingservice.repository;

import com.example.ticketbookingservice.model.Event;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Modifying
    @Query("UPDATE Event e SET e.availableTickets = e.availableTickets - 1 WHERE e.id = :id AND e.availableTickets > 0")
    Long decrementTicket(@Param("id") Long id);
}
