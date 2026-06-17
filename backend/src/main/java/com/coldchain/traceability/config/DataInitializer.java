package com.coldchain.traceability.config;

import com.coldchain.traceability.service.MockDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!prod")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MockDataService mockDataService;

    @Override
    public void run(String... args) {
        log.info("Starting data initialization...");
        mockDataService.initializeMockData();
        log.info("Data initialization complete");
    }
}
