package com.example.ticketbookingservice.repository;

import com.example.ticketbookingservice.model.Event;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Event e WHERE e.id = :id")
    Optional<Event> findByIdWithLock(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Event e SET e.availableTickets = e.availableTickets - 1 WHERE e.id = :id AND e.availableTickets > 0")
    int decrementTicket(@Param("id") Long id);
}
