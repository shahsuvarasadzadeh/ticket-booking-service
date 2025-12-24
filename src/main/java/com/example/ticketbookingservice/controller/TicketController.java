package com.example.ticketbookingservice.controller;

import com.example.ticketbookingservice.model.Booking;
import com.example.ticketbookingservice.model.TicketDto;
import com.example.ticketbookingservice.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping("/{eventId}/availability")
    public ResponseEntity<Integer> checkAvailability(@PathVariable Long eventId) {
        int availableTickets = ticketService.getAvailableTickets(eventId);
        return ResponseEntity.ok(availableTickets);
    }

    @PostMapping("/{eventId}/book")
    public ResponseEntity<?> bookTicket(@Validated TicketDto dto) {
        try {
            Booking booking = ticketService.bookTicket(dto);
            return ResponseEntity.ok("Bilet uğurla alındı! Kod: " + booking.getTicketCode());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}