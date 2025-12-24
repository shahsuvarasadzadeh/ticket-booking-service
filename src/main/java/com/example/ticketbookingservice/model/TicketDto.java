package com.example.ticketbookingservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TicketDto {
    Long eventId;
    String userId;
    String userMail;
}
