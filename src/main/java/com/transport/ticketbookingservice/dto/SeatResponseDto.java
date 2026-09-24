package com.transport.ticketbookingservice.dto;

import com.transport.ticketbookingservice.entity.enums.SeatClass;
import com.transport.ticketbookingservice.entity.enums.SeatStatus;

import java.math.BigDecimal;

public record SeatResponseDto(
        Long seatId,
        Integer carriageNumber,
        Integer seatNumber,
        SeatClass seatClass,
        SeatStatus status,
        BigDecimal price
) {
}