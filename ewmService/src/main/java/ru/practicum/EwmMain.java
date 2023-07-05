package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = {"ru.practicum"})
@Import({WebClientService.class})
public class EwmMain {
    public static void main(String[] args) {
        SpringApplication.run(EwmMain.class, args);
    }
}