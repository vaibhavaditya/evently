package com.evently.evtbff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class EvtBffApplication {

    public static void main(String[] args) {
        SpringApplication.run(EvtBffApplication.class, args);
    }

}
