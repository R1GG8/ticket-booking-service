package com.transport.ticketbookingservice.repository;

import com.transport.ticketbookingservice.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {
    Optional<Station> findByNameIgnoreCase(String name);
}