package com.example.ticketbookingservice.service;

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

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;
    private final StringRedisTemplate redisTemplate;
    private final KafkaTemplate<String, BookingMailEvent> kafkaTemplate;

    private static final String EVENT_TICKET_KEY = "event_tickets::";

    /**
     * READ FLOW: Yüksək Trafik üçün [cite: 11]
     * Bilet sayını əvvəl Redis-dən oxuyur. Yoxdursa Bazadan oxuyub Redis-ə yazır.
     */
    public int getAvailableTickets(Long eventId) {
        String cacheKey = EVENT_TICKET_KEY + eventId;

        String cachedValue = redisTemplate.opsForValue().get(cacheKey);
        if (cachedValue != null) {
            return Integer.parseInt(cachedValue);
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Tədbir tapılmadı"));

        redisTemplate.opsForValue().set(cacheKey, String.valueOf(event.getAvailableTickets()), Duration.ofSeconds(5));

        return event.getAvailableTickets();
    }

    @Transactional
    public Booking bookTicket(TicketDto dto) {

        int updatedRows = eventRepository.decrementTicket(dto.getEventId());

        if (updatedRows == 0) {
            throw new RuntimeException("Bilet tükənib və ya tədbir tapılmadı!");
        }

        Event event = eventRepository.findById(dto.getEventId()).orElseThrow();

        Booking booking = Booking.builder()
                .userId(dto.getUserId())
                .event(event)
                .bookingTime(LocalDateTime.now())
                .build();

        bookingRepository.save(booking);

        BookingMailEvent mailEvent = new BookingMailEvent(dto.getUserId(), booking.getTicketCode(), dto.getUserMail());
        kafkaTemplate.send("notification-topic", mailEvent);
        log.info("Kafka-ya mesaj göndərildi: {}", mailEvent);

        String cacheKey = EVENT_TICKET_KEY + dto.getEventId();
        redisTemplate.delete(cacheKey);

        return booking;
    }
}