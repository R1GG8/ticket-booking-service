package com.transport.ticketbookingservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CreateBookingRequest(
        @NotNull(message = "ID рейса обязателен")
        Long tripId,

        @NotNull(message = "ID места обязателен")
        Long seatId,

        @NotNull(message = "Данные пассажира обязательны")
        @Valid
        PassengerDto passenger
) {
}