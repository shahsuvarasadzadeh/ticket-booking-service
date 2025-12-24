package com.example.ticketbookingservice.service;

import com.example.ticketbookingservice.model.BookingMailEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    @KafkaListener(topics = "notification-topic", groupId = "notification-group")
    public void sendBookingConfirmationByEmil(BookingMailEvent event) {
        log.info("Email göndərilməsi başladıldı: Thread={}", Thread.currentThread());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("Email uğurla göndərildi! User={}", event.getUserId());
    }
}