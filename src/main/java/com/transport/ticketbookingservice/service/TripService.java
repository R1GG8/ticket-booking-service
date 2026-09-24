package com.transport.ticketbookingservice.service;

import com.transport.ticketbookingservice.dto.SeatResponseDto;
import com.transport.ticketbookingservice.dto.TripResponseDto;
import com.transport.ticketbookingservice.entity.Seat;
import com.transport.ticketbookingservice.entity.Trip;
import com.transport.ticketbookingservice.entity.enums.SeatStatus;
import com.transport.ticketbookingservice.repository.SeatRepository;
import com.transport.ticketbookingservice.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final SeatRepository seatRepository;
    private final PricingService pricingService;

    @Transactional(readOnly = true)
    public List<TripResponseDto> searchTrips(String fromCity, String toCity, LocalDate date) {
        OffsetDateTime startOfDay = date.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endOfDay = date.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        List<Trip> trips = tripRepository.findTripsByCitiesAndDate(fromCity, toCity, startOfDay, endOfDay);

        return trips.stream().map(trip -> {
            long freeSeats = seatRepository.countByTripIdAndStatus(trip.getId(), SeatStatus.FREE);

            return new TripResponseDto(
                    trip.getId(),
                    trip.getTripNumber(),
                    trip.getRoute().getDepartureStation().getName(),
                    trip.getRoute().getDepartureStation().getCity(),
                    trip.getRoute().getDestinationStation().getName(),
                    trip.getRoute().getDestinationStation().getCity(),
                    trip.getDepartureTime(),
                    trip.getArrivalTime(),
                    trip.getBasePrice(),
                    freeSeats
            );
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<SeatResponseDto> getSeatsForTrip(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Рейс с ID " + tripId + " не найден"));

        List<Seat> seats = seatRepository.findByTripId(tripId);
        long freeSeatsCount = seats.stream().filter(s -> s.getStatus() == SeatStatus.FREE).count();
        long totalSeatsCount = seats.size();

        return seats.stream().map(seat -> {
            BigDecimal price = pricingService.calculateSeatPrice(trip, seat, freeSeatsCount, totalSeatsCount);

            return new SeatResponseDto(
                    seat.getId(),
                    seat.getCarriageNumber(),
                    seat.getSeatNumber(),
                    seat.getSeatClass(),
                    seat.getStatus(),
                    price
            );
        }).toList();
    }
}