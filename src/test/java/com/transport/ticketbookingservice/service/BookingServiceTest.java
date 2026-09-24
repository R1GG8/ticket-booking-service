package com.transport.ticketbookingservice.service;

import com.transport.ticketbookingservice.dto.BookingResponseDto;
import com.transport.ticketbookingservice.dto.CreateBookingRequest;
import com.transport.ticketbookingservice.dto.PassengerDto;
import com.transport.ticketbookingservice.entity.Booking;
import com.transport.ticketbookingservice.entity.Passenger;
import com.transport.ticketbookingservice.entity.Seat;
import com.transport.ticketbookingservice.entity.Trip;
import com.transport.ticketbookingservice.entity.enums.BookingStatus;
import com.transport.ticketbookingservice.entity.enums.SeatClass;
import com.transport.ticketbookingservice.entity.enums.SeatStatus;
import com.transport.ticketbookingservice.repository.BookingRepository;
import com.transport.ticketbookingservice.repository.PassengerRepository;
import com.transport.ticketbookingservice.repository.SeatRepository;
import com.transport.ticketbookingservice.repository.TripRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Подключаем Mockito к JUnit 5
class BookingServiceTest {

    // Создаем фальшивые объекты (заглушки)
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private TripRepository tripRepository;
    @Mock
    private PassengerRepository passengerRepository;
    @Mock
    private PricingService pricingService;

    // Внедряем эти заглушки в тестируемый сервис
    @InjectMocks
    private BookingService bookingService;

    @Test
    @DisplayName("Успешное создание бронирования, если место свободно")
    void shouldSuccessfullyCreateBookingWhenSeatIsFree() {
        // 1. Arrange (Подготовка данных)
        Long tripId = 1L;
        Long seatId = 10L;

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setTripNumber("072A");

        Seat seat = new Seat();
        seat.setId(seatId);
        seat.setTrip(trip);
        seat.setStatus(SeatStatus.FREE);
        seat.setCarriageNumber(1);
        seat.setSeatNumber(5);
        seat.setSeatClass(SeatClass.ECONOMY);

        Passenger passenger = new Passenger();
        passenger.setFirstName("Иван");
        passenger.setLastName("Иванов");

        CreateBookingRequest request = new CreateBookingRequest(
                tripId,
                seatId,
                new PassengerDto("Иван", "Иванов", "ivan@test.com", "123456")
        );

        // Обучаем моки: что отвечать при вызове методов
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(seatRepository.findByIdWithLock(seatId)).thenReturn(Optional.of(seat));
        when(passengerRepository.findByDocumentNumber("123456")).thenReturn(Optional.of(passenger));
        when(seatRepository.findByTripId(tripId)).thenReturn(List.of(seat));
        when(pricingService.calculateSeatPrice(any(), any(), anyLong(), anyLong()))
                .thenReturn(new BigDecimal("2500.00"));

        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(999L);
            return b;
        });

        // 2. Act (Вызов реального метода)
        BookingResponseDto response = bookingService.createBooking(request);

        // 3. Assert (Проверка результата)
        assertThat(response).isNotNull();
        assertThat(response.bookingId()).isEqualTo(999L);
        assertThat(response.status()).isEqualTo(BookingStatus.PENDING);
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.RESERVED); // статус места должен был измениться!

        // Проверяем, что методы сохранения действительно вызывались
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(seatRepository, times(1)).save(seat);
    }

    @Test
    @DisplayName("Ошибка бронирования: если место уже занято, должно выброситься исключение")
    void shouldThrowExceptionWhenSeatIsAlreadyReserved() {
        // Arrange
        Long tripId = 1L;
        Long seatId = 10L;

        Trip trip = new Trip();
        trip.setId(tripId);

        Seat busySeat = new Seat();
        busySeat.setId(seatId);
        busySeat.setTrip(trip);
        busySeat.setStatus(SeatStatus.RESERVED); // МЕСТО УЖЕ ЗАНЯТО!

        CreateBookingRequest request = new CreateBookingRequest(
                tripId,
                seatId,
                new PassengerDto("Петр", "Петров", "petr@test.com", "999888")
        );

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(seatRepository.findByIdWithLock(seatId)).thenReturn(Optional.of(busySeat));

        // Act & Assert (Проверяем, что метод падает с ошибкой IllegalStateException)
        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Место уже занято или забронировано");

        // Убеждаемся, что бронь НЕ сохранилась в БД
        verify(bookingRepository, never()).save(any());
    }
}