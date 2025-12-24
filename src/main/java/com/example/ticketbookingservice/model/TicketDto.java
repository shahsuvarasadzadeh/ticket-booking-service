package com.example.ticketbookingservice.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TicketDto {
    @NotNull
    Long eventId;
    @NotBlank
    String userId;
    @NotBlank
    @Email
    String userMail;
}
