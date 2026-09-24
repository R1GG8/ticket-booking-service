package com.transport.ticketbookingservice.dto;

import java.time.OffsetDateTime;

public record ErrorResponseDto(
        int statusCode,
        String error,
        String message,
        OffsetDateTime timestamp
) {
}