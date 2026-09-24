package com.transport.ticketbookingservice.repository;

import com.transport.ticketbookingservice.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    @Query("""
                SELECT t FROM Trip t
                JOIN FETCH t.route r
                JOIN FETCH r.departureStation dep
                JOIN FETCH r.destinationStation dest
                WHERE LOWER(dep.city) = LOWER(:fromCity)
                  AND LOWER(dest.city) = LOWER(:toCity)
                  AND t.departureTime >= :startTime
                  AND t.departureTime < :endTime
                  AND t.status = 'SCHEDULED'
            """)
    List<Trip> findTripsByCitiesAndDate(
            @Param("fromCity") String fromCity,
            @Param("toCity") String toCity,
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime
    );
}