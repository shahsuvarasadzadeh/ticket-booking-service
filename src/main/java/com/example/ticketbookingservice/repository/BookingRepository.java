package com.example.ticketbookingservice.repository;

import com.example.ticketbookingservice.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}
