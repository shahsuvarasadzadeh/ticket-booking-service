package com.example.ticketbookingservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingMailEvent {
    String userId;
    String ticketCode;
    String email;
}
