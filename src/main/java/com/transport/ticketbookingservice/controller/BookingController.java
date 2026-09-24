package com.transport.ticketbookingservice.controller;

import com.transport.ticketbookingservice.dto.BookingResponseDto;
import com.transport.ticketbookingservice.dto.CreateBookingRequest;
import com.transport.ticketbookingservice.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponseDto createBooking(@Valid @RequestBody CreateBookingRequest request) {
        return bookingService.createBooking(request);
    }

    @PostMapping("/{bookingId}/confirm")
    public BookingResponseDto confirmBooking(@PathVariable Long bookingId) {
        return bookingService.confirmBooking(bookingId);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getBookingById(@PathVariable Long bookingId) {
        return bookingService.getBookingById(bookingId);
    }

    @PostMapping("/{bookingId}/cancel")
    public BookingResponseDto cancelBooking(@PathVariable Long bookingId) {
        return bookingService.cancelBooking(bookingId);
    }

    @GetMapping("/my")
    public List<BookingResponseDto> getMyBookings(@RequestParam String email) {
        return bookingService.getBookingsByEmail(email);
    }
}