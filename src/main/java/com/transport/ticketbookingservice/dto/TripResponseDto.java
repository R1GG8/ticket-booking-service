package com.transport.ticketbookingservice.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TripResponseDto(
        Long tripId,
        String tripNumber,
        String fromStation,
        String fromCity,
        String toStation,
        String toCity,
        OffsetDateTime departureTime,
        OffsetDateTime arrivalTime,
        BigDecimal basePrice,
        long availableSeatsCount
) {
}