package com.transport.ticketbookingservice.service;

import com.transport.ticketbookingservice.entity.Seat;
import com.transport.ticketbookingservice.entity.Trip;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PricingService {

    private static final BigDecimal ECONOMY_COEFFICIENT = BigDecimal.valueOf(1.0);
    private static final BigDecimal COUPE_COEFFICIENT = BigDecimal.valueOf(1.6);
    private static final BigDecimal BUSINESS_COEFFICIENT = BigDecimal.valueOf(2.5);

    private static final BigDecimal HIGH_DEMAND_SURGE = BigDecimal.valueOf(1.20);

    public BigDecimal calculateSeatPrice(Trip trip, Seat seat, long freeSeatsCount, long totalSeatsCount) {
        BigDecimal basePrice = trip.getBasePrice();

        BigDecimal classCoefficient = switch (seat.getSeatClass()) {
            case ECONOMY -> ECONOMY_COEFFICIENT;
            case COUPE -> COUPE_COEFFICIENT;
            case BUSINESS -> BUSINESS_COEFFICIENT;
        };

        BigDecimal price = basePrice.multiply(classCoefficient);

        if (totalSeatsCount > 0 && ((double) freeSeatsCount / totalSeatsCount) < 0.5) {
            price = price.multiply(HIGH_DEMAND_SURGE);
        }

        return price.setScale(2, RoundingMode.HALF_UP);
    }
}