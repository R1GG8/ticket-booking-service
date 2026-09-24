-- 1. Станции
CREATE TABLE stations
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    code VARCHAR(10)  NOT NULL UNIQUE
);

-- 2. Маршруты
CREATE TABLE routes
(
    id                     BIGSERIAL PRIMARY KEY,
    departure_station_id   BIGINT NOT NULL REFERENCES stations (id),
    destination_station_id BIGINT NOT NULL REFERENCES stations (id),
    distance_km            INT    NOT NULL,
    CONSTRAINT check_different_stations CHECK (departure_station_id <> destination_station_id)
);

-- 3. Рейсы (конкретная поездка по расписанию)
CREATE TABLE trips
(
    id             BIGSERIAL PRIMARY KEY,
    route_id       BIGINT                   NOT NULL REFERENCES routes (id),
    trip_number    VARCHAR(20)              NOT NULL,                    -- например, "072А" или "BUS-104"
    departure_time TIMESTAMP WITH TIME ZONE NOT NULL,
    arrival_time   TIMESTAMP WITH TIME ZONE NOT NULL,
    base_price     NUMERIC(10, 2)           NOT NULL,                    -- базовая цена рейса
    status         VARCHAR(20)              NOT NULL DEFAULT 'SCHEDULED' -- SCHEDULED, CANCELLED, COMPLETED
);

-- 4. Пассажиры
CREATE TABLE passengers
(
    id              BIGSERIAL PRIMARY KEY,
    first_name      VARCHAR(50)  NOT NULL,
    last_name       VARCHAR(50)  NOT NULL,
    email           VARCHAR(100) NOT NULL,
    document_number VARCHAR(50)  NOT NULL
);

-- 5. Бронирования / Заказы
CREATE TABLE bookings
(
    id           BIGSERIAL PRIMARY KEY,
    trip_id      BIGINT                   NOT NULL REFERENCES trips (id),
    passenger_id BIGINT                   NOT NULL REFERENCES passengers (id),
    total_price  NUMERIC(10, 2)           NOT NULL,
    status       VARCHAR(20)              NOT NULL DEFAULT 'PENDING', -- PENDING, CONFIRMED, CANCELLED, EXPIRED
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    expires_at   TIMESTAMP WITH TIME ZONE NOT NULL                    -- время, до которого бронь должна быть оплачена (например +15 мин)
);

-- 6. Места на рейс
CREATE TABLE seats
(
    id              BIGSERIAL PRIMARY KEY,
    trip_id         BIGINT      NOT NULL REFERENCES trips (id) ON DELETE CASCADE,
    carriage_number INT         NOT NULL,                -- номер вагона
    seat_number     INT         NOT NULL,                -- номер места
    seat_class      VARCHAR(20) NOT NULL,                -- ECONOMY, COUPE, BUSINESS
    status          VARCHAR(20) NOT NULL DEFAULT 'FREE', -- FREE, RESERVED, SOLD
    booking_id      BIGINT      REFERENCES bookings (id) ON DELETE SET NULL,
    CONSTRAINT uk_trip_carriage_seat UNIQUE (trip_id, carriage_number, seat_number)
);

-- Индексы для быстрого поиска рейсов и свободных мест
CREATE INDEX idx_trips_route_departure ON trips (route_id, departure_time);
CREATE INDEX idx_seats_trip_status ON seats (trip_id, status);
CREATE INDEX idx_bookings_expires_status ON bookings (expires_at, status);