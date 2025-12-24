package com.example.ticketbookingservice.configuration;

import com.example.ticketbookingservice.model.Event;
import com.example.ticketbookingservice.repository.EventRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(EventRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                Event event = Event.builder()
                        .name("Coldplay Concert - Baku")
                        .totalTickets(100L)
                        .availableTickets(100L)
                        .date(LocalDateTime.now().plusDays(30))
                        .build();
                repository.save(event);
                System.out.println("TEST DATA LOADED: Coldplay Concert (ID=1)");
            }
        };
    }
}
