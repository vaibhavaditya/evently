package com.evently.evtnotificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class EvtNotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                EvtNotificationServiceApplication.class,
                args
        );
    }
}