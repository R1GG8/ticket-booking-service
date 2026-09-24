-- Добавим станции
INSERT INTO stations (id, name, city, code)
VALUES (1, 'Ленинградский вокзал', 'Москва', 'MOW'),
       (2, 'Московский вокзал', 'Санкт-Петербург', 'LED'),
       (3, 'Главный вокзал', 'Казань', 'KZN');

-- Маршрут Москва - Санкт-Петербург (650 км)
INSERT INTO routes (id, departure_station_id, destination_station_id, distance_km)
VALUES (1, 1, 2, 650);

-- Рейс на завтра (072А "Сапсан") с базовой ценой 2500.00
INSERT INTO trips (id, route_id, trip_number, departure_time, arrival_time, base_price, status)
VALUES (1, 1, '072А', NOW() + INTERVAL '1 day', NOW() + INTERVAL '1 day 4 hours', 2500.00, 'SCHEDULED');

-- Добавим места в рейс 1 (Вагон 1 - ECONOMY, Вагон 2 - COUPE)
INSERT INTO seats (trip_id, carriage_number, seat_number, seat_class, status)
VALUES (1, 1, 1, 'ECONOMY', 'FREE'),
       (1, 1, 2, 'ECONOMY', 'FREE'),
       (1, 1, 3, 'ECONOMY', 'FREE'),
       (1, 1, 4, 'ECONOMY', 'FREE'),
       (1, 2, 1, 'COUPE', 'FREE'),
       (1, 2, 2, 'COUPE', 'FREE');