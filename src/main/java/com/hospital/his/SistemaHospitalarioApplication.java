package com.hospital.his;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class SistemaHospitalarioApplication {

    public static void main(String[] args) {

        SpringApplication.run(
                SistemaHospitalarioApplication.class,
                args
        );
    }
}