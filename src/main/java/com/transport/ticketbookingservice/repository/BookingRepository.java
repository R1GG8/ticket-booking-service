package com.transport.ticketbookingservice.repository;

import com.transport.ticketbookingservice.entity.Booking;
import com.transport.ticketbookingservice.entity.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Найти все заказы пассажира по email
    List<Booking> findByPassengerEmail(String email);

    // Найти протухшие брони для фонового робота
    List<Booking> findByStatusAndExpiresAtBefore(BookingStatus status, OffsetDateTime time);
}