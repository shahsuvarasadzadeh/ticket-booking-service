package com.example.ticketbookingservice;

import com.example.ticketbookingservice.model.Event;
import com.example.ticketbookingservice.model.TicketDto;
import com.example.ticketbookingservice.repository.EventRepository;
import com.example.ticketbookingservice.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class TicketConcurrencyTest {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private EventRepository eventRepository;

    @MockitoBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    public void testConcurrentBooking() throws InterruptedException {
        Event event = Event.builder()
                .name("Test Concert")
                .totalTickets(50)
                .availableTickets(50)
                .date(LocalDateTime.now().plusDays(10))
                .build();
        event = eventRepository.save(event);
        Long eventId = event.getId();

        int numberOfThreads = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            String userId = "User-" + i;
            String userMail="test@gmail.com";
            executorService.submit(() -> {
                try {
                    ticketService.bookTicket(new TicketDto(eventId,userId,userMail));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        System.out.println("Uğurlu Satış: " + successCount.get());
        System.out.println("Uğursuz Cəhd: " + failCount.get());

        assertEquals(50, successCount.get(), "50 bilet satılmalı idi!");
        assertEquals(50, failCount.get(), "50 nəfər bilet ala bilməməli idi!");

        Event finalEvent = eventRepository.findById(eventId).orElseThrow();
        assertEquals(0, finalEvent.getAvailableTickets(), "Bazada bilet sayı 0 olmalıdır, mənfi yox!");
    }
}
