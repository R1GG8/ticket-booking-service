package com.transport.ticketbookingservice.dto;

import com.transport.ticketbookingservice.entity.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record BookingResponseDto(
        Long bookingId,
        Long tripId,
        String tripNumber,
        Integer carriageNumber,
        Integer seatNumber,
        String passengerFullName,
        BigDecimal totalPrice,
        BookingStatus status,
        OffsetDateTime expiresAt
) {
}