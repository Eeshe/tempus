package me.eeshe.tempus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TempusApplication {

    public static void main(String[] args) {
        SpringApplication.run(TempusApplication.class, args);
    }

}
