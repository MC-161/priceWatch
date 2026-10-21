package dev.pricewatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PriceWatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(PriceWatchApplication.class, args);
    }
}
