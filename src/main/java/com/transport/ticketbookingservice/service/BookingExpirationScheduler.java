package com.transport.ticketbookingservice.service;

import com.transport.ticketbookingservice.entity.Booking;
import com.transport.ticketbookingservice.entity.Seat;
import com.transport.ticketbookingservice.entity.enums.BookingStatus;
import com.transport.ticketbookingservice.entity.enums.SeatStatus;
import com.transport.ticketbookingservice.repository.BookingRepository;
import com.transport.ticketbookingservice.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingExpirationScheduler {

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void expireOldBookings() {
        OffsetDateTime now = OffsetDateTime.now();
        List<Booking> expiredBookings = bookingRepository.findByStatusAndExpiresAtBefore(BookingStatus.PENDING, now);

        if (!expiredBookings.isEmpty()) {
            log.info("Найдено протухших броней: {}", expiredBookings.size());

            for (Booking booking : expiredBookings) {
                booking.setStatus(BookingStatus.EXPIRED);

                List<Seat> seats = seatRepository.findByTripId(booking.getTrip().getId());
                for (Seat seat : seats) {
                    if (booking.equals(seat.getBooking())) {
                        seat.setStatus(SeatStatus.FREE);
                        seat.setBooking(null);
                        seatRepository.save(seat);
                        log.info("Место №{} в вагоне №{} освобождено", seat.getSeatNumber(), seat.getCarriageNumber());
                    }
                }
            }
        }
    }
}