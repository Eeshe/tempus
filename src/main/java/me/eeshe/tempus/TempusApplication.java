package me.eeshe.tempus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TempusApplication {

    public static void main(String[] args) {
        try {
            Files.createDirectories(Path.of("data"));
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't create directory 'data': " + e.getMessage(), e);
        }
        SpringApplication.run(TempusApplication.class, args);
    }
}
