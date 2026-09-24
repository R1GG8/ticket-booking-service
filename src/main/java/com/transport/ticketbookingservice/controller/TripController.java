package com.transport.ticketbookingservice.controller;

import com.transport.ticketbookingservice.dto.SeatResponseDto;
import com.transport.ticketbookingservice.dto.TripResponseDto;
import com.transport.ticketbookingservice.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @GetMapping
    public List<TripResponseDto> searchTrips(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return tripService.searchTrips(from, to, date);
    }

    @GetMapping("/{tripId}/seats")
    public List<SeatResponseDto> getTripSeats(@PathVariable Long tripId) {
        return tripService.getSeatsForTrip(tripId);
    }
}