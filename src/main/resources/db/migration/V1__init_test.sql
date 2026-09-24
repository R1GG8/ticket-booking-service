-- Тестовая таблица для проверки работы Flyway
CREATE TABLE test_connection
(
    id      BIGSERIAL PRIMARY KEY,
    message VARCHAR(255) NOT NULL
);