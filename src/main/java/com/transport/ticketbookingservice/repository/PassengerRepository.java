package com.transport.ticketbookingservice.repository;

import com.transport.ticketbookingservice.entity.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {
    Optional<Passenger> findByDocumentNumber(String documentNumber);
}