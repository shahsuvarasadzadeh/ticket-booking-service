package com.example.ticketbookingservice.service;

import com.example.ticketbookingservice.exception.EventNotFoundException;
import com.example.ticketbookingservice.model.Booking;
import com.example.ticketbookingservice.model.BookingMailEvent;
import com.example.ticketbookingservice.model.Event;
import com.example.ticketbookingservice.model.TicketDto;
import com.example.ticketbookingservice.repository.BookingRepository;
import com.example.ticketbookingservice.repository.EventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final StringRedisTemplate redisTemplate;
    private final KafkaTemplate<String, BookingMailEvent> kafkaTemplate;

    private static final String EVENT_TICKET_KEY = "event_tickets::";

    public Long getAvailableTickets(Long eventId) {
        String cacheKey = EVENT_TICKET_KEY + eventId;

        try {
            String cachedValue = redisTemplate.opsForValue().get(cacheKey);
            if (cachedValue != null) return Long.parseLong(cachedValue);
        } catch (Exception e) {
            log.error("Redis is down, fetching from DB directly", e);
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId));

        try {
            redisTemplate.opsForValue().set(cacheKey, String.valueOf(event.getAvailableTickets()), Duration.ofSeconds(5));
        } catch (Exception e) {
            log.warn("Could not save to Redis");
        }
        return event.getAvailableTickets();
    }

    @Transactional
    public Booking bookTicket(TicketDto dto) {

        int updatedRows = eventRepository.decrementTicket(dto.getEventId());

        if (updatedRows == 0) {
            throw new RuntimeException("Tickets are sold out or event not found!");
        }

        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + dto.getEventId()));

        Booking booking = Booking.builder()
                .userId(dto.getUserId())
                .event(event)
                .bookingTime(LocalDateTime.now())
                .build();

        bookingRepository.save(booking);

        BookingMailEvent mailEvent = new BookingMailEvent(dto.getUserId(), booking.getTicketCode(), dto.getUserMail());
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                kafkaTemplate.send("notification-topic", mailEvent);
                log.info("Message sent to Kafka after transaction commit");
            }
        });
        String cacheKey = EVENT_TICKET_KEY + dto.getEventId();
        try {
            redisTemplate.delete(cacheKey);
        } catch (Exception e) {
            log.warn("Could not delete Redis key, cache will expire naturally");
        }
        return booking;
    }
}