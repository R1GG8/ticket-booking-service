package com.transport.ticketbookingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Включаем фоновые задачи Spring
public class TicketBookingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketBookingServiceApplication.class, args);
    }
}
