package com.transport.ticketbookingservice.service;

import com.transport.ticketbookingservice.dto.BookingResponseDto;
import com.transport.ticketbookingservice.dto.CreateBookingRequest;
import com.transport.ticketbookingservice.dto.PassengerDto;
import com.transport.ticketbookingservice.entity.Booking;
import com.transport.ticketbookingservice.entity.Passenger;
import com.transport.ticketbookingservice.entity.Seat;
import com.transport.ticketbookingservice.entity.Trip;
import com.transport.ticketbookingservice.entity.enums.BookingStatus;
import com.transport.ticketbookingservice.entity.enums.SeatStatus;
import com.transport.ticketbookingservice.repository.BookingRepository;
import com.transport.ticketbookingservice.repository.PassengerRepository;
import com.transport.ticketbookingservice.repository.SeatRepository;
import com.transport.ticketbookingservice.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final TripRepository tripRepository;
    private final PassengerRepository passengerRepository;
    private final PricingService pricingService;

    @Transactional
    public BookingResponseDto createBooking(CreateBookingRequest request) {
        Trip trip = tripRepository.findById(request.tripId())
                .orElseThrow(() -> new IllegalArgumentException("Рейс не найден"));

        Seat seat = seatRepository.findByIdWithLock(request.seatId())
                .orElseThrow(() -> new IllegalArgumentException("Место не найдено"));

        if (!seat.getTrip().getId().equals(trip.getId())) {
            throw new IllegalArgumentException("Место не принадлежит указанному рейсу");
        }

        if (seat.getStatus() != SeatStatus.FREE) {
            throw new IllegalStateException("Место уже занято или забронировано другим пассажиром!");
        }

        PassengerDto pDto = request.passenger();
        Passenger passenger = passengerRepository.findByDocumentNumber(pDto.documentNumber())
                .orElseGet(() -> {
                    Passenger newPassenger = new Passenger();
                    newPassenger.setFirstName(pDto.firstName());
                    newPassenger.setLastName(pDto.lastName());
                    newPassenger.setEmail(pDto.email());
                    newPassenger.setDocumentNumber(pDto.documentNumber());
                    return passengerRepository.save(newPassenger);
                });

        List<Seat> allSeats = seatRepository.findByTripId(trip.getId());
        long freeSeatsCount = allSeats.stream().filter(s -> s.getStatus() == SeatStatus.FREE).count();
        BigDecimal finalPrice = pricingService.calculateSeatPrice(trip, seat, freeSeatsCount, allSeats.size());

        Booking booking = new Booking();
        booking.setTrip(trip);
        booking.setPassenger(passenger);
        booking.setTotalPrice(finalPrice);
        booking.setStatus(BookingStatus.PENDING);
        booking.setExpiresAt(OffsetDateTime.now().plusMinutes(15));
        booking = bookingRepository.save(booking);

        seat.setStatus(SeatStatus.RESERVED);
        seat.setBooking(booking);
        seatRepository.save(seat);

        return new BookingResponseDto(
                booking.getId(),
                trip.getId(),
                trip.getTripNumber(),
                seat.getCarriageNumber(),
                seat.getSeatNumber(),
                passenger.getFirstName() + " " + passenger.getLastName(),
                booking.getTotalPrice(),
                booking.getStatus(),
                booking.getExpiresAt()
        );
    }

    @Transactional
    public BookingResponseDto confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Бронь с ID " + bookingId + " не найдена"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Бронь не может быть подтверждена (текущий статус: " + booking.getStatus() + ")");
        }

        if (OffsetDateTime.now().isAfter(booking.getExpiresAt())) {
            booking.setStatus(BookingStatus.EXPIRED);
            throw new IllegalStateException("Время на оплату брони истекло!");
        }

        booking.setStatus(BookingStatus.CONFIRMED);

        List<Seat> seats = seatRepository.findByTripId(booking.getTrip().getId());
        seats.stream()
                .filter(s -> booking.equals(s.getBooking()))
                .findFirst()
                .ifPresent(seat -> seat.setStatus(SeatStatus.SOLD));

        Seat seat = seats.stream().filter(s -> booking.equals(s.getBooking())).findFirst().orElseThrow();

        return new BookingResponseDto(
                booking.getId(),
                booking.getTrip().getId(),
                booking.getTrip().getTripNumber(),
                seat.getCarriageNumber(),
                seat.getSeatNumber(),
                booking.getPassenger().getFirstName() + " " + booking.getPassenger().getLastName(),
                booking.getTotalPrice(),
                booking.getStatus(),
                booking.getExpiresAt()
        );
    }

    @Transactional(readOnly = true)
    public BookingResponseDto getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Бронь с ID " + bookingId + " не найдена"));

        Seat seat = seatRepository.findByTripId(booking.getTrip().getId()).stream()
                .filter(s -> booking.equals(s.getBooking()))
                .findFirst()
                .orElse(null);

        return new BookingResponseDto(
                booking.getId(),
                booking.getTrip().getId(),
                booking.getTrip().getTripNumber(),
                seat != null ? seat.getCarriageNumber() : 0,
                seat != null ? seat.getSeatNumber() : 0,
                booking.getPassenger().getFirstName() + " " + booking.getPassenger().getLastName(),
                booking.getTotalPrice(),
                booking.getStatus(),
                booking.getExpiresAt()
        );
    }

    @Transactional
    public BookingResponseDto cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Бронь с ID " + bookingId + " не найдена"));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Бронь уже была отменена ранее");
        }

        booking.setStatus(BookingStatus.CANCELLED);

        List<Seat> seats = seatRepository.findByTripId(booking.getTrip().getId());
        Seat releasedSeat = null;
        for (Seat seat : seats) {
            if (booking.equals(seat.getBooking())) {
                seat.setStatus(SeatStatus.FREE);
                seat.setBooking(null); // отвязываем от брони
                seatRepository.save(seat);
                releasedSeat = seat;
                break;
            }
        }

        return new BookingResponseDto(
                booking.getId(),
                booking.getTrip().getId(),
                booking.getTrip().getTripNumber(),
                releasedSeat != null ? releasedSeat.getCarriageNumber() : 0,
                releasedSeat != null ? releasedSeat.getSeatNumber() : 0,
                booking.getPassenger().getFirstName() + " " + booking.getPassenger().getLastName(),
                booking.getTotalPrice(),
                booking.getStatus(),
                booking.getExpiresAt()
        );
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDto> getBookingsByEmail(String email) {
        List<Booking> bookings = bookingRepository.findByPassengerEmail(email);

        return bookings.stream().map(b -> {
            Seat seat = seatRepository.findByTripId(b.getTrip().getId()).stream()
                    .filter(s -> b.equals(s.getBooking()))
                    .findFirst()
                    .orElse(null);

            return new BookingResponseDto(
                    b.getId(),
                    b.getTrip().getId(),
                    b.getTrip().getTripNumber(),
                    seat != null ? seat.getCarriageNumber() : 0,
                    seat != null ? seat.getSeatNumber() : 0,
                    b.getPassenger().getFirstName() + " " + b.getPassenger().getLastName(),
                    b.getTotalPrice(),
                    b.getStatus(),
                    b.getExpiresAt()
            );
        }).toList();
    }
}