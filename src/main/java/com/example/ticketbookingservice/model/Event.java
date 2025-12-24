package com.example.ticketbookingservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "total_tickets")
    private Long totalTickets;

    @Column(name = "available_tickets")
    private Long availableTickets;

    private LocalDateTime date;

    @Version
    private Long version;
}
