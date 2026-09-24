package com.transport.ticketbookingservice.service;

import com.transport.ticketbookingservice.entity.Seat;
import com.transport.ticketbookingservice.entity.Trip;
import com.transport.ticketbookingservice.entity.enums.SeatClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PricingServiceTest {

    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        // Создаем сервис напрямую через new, без Spring!
        pricingService = new PricingService();
    }

    @Test
    @DisplayName("Эконом-класс при низком спросе должен стоить ровно базовую цену")
    void shouldCalculatePriceForEconomyWithoutSurge() {
        // Given (Дано)
        Trip trip = new Trip();
        trip.setBasePrice(new BigDecimal("2000.00"));

        Seat seat = new Seat();
        seat.setSeatClass(SeatClass.ECONOMY);

        long freeSeats = 8;
        long totalSeats = 10; // 80% мест свободно -> спрос низкий

        // When (Действие)
        BigDecimal actualPrice = pricingService.calculateSeatPrice(trip, seat, freeSeats, totalSeats);

        // Then (Проверка)
        // 2000 * 1.0 = 2000.00
        assertThat(actualPrice).isEqualByComparingTo("2000.00");
    }

    @Test
    @DisplayName("Купе при низком спросе должно стоить дороже на коэффициент 1.6")
    void shouldCalculatePriceForCoupeWithoutSurge() {
        // Given
        Trip trip = new Trip();
        trip.setBasePrice(new BigDecimal("2000.00"));

        Seat seat = new Seat();
        seat.setSeatClass(SeatClass.COUPE);

        long freeSeats = 6;
        long totalSeats = 10; // 60% свободно (> 50%, наценки нет)

        // When
        BigDecimal actualPrice = pricingService.calculateSeatPrice(trip, seat, freeSeats, totalSeats);

        // Then
        // 2000 * 1.6 = 3200.00
        assertThat(actualPrice).isEqualByComparingTo("3200.00");
    }

    @Test
    @DisplayName("При высоком спросе (< 50% мест) должна применяться наценка 20%")
    void shouldApplySurgePricingWhenDemandIsHigh() {
        // Given
        Trip trip = new Trip();
        trip.setBasePrice(new BigDecimal("2000.00"));

        Seat seat = new Seat();
        seat.setSeatClass(SeatClass.COUPE);

        long freeSeats = 3;
        long totalSeats = 10; // 30% свободно (< 50% -> высокий спрос!)

        // When
        BigDecimal actualPrice = pricingService.calculateSeatPrice(trip, seat, freeSeats, totalSeats);

        // Then
        // 2000 * 1.6 (купе) * 1.2 (наценка спроса) = 3840.00
        assertThat(actualPrice).isEqualByComparingTo("3840.00");
    }
}