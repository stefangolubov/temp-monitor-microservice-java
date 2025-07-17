package com.tempmonitor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tempmonitor.entity.Location;
import com.tempmonitor.entity.Thermometer;
import com.tempmonitor.repo.LocationRepository;
import com.tempmonitor.repo.ThermometerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class TempMonitorIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.2")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ThermometerRepository thermometerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        thermometerRepository.deleteAll();
        locationRepository.deleteAll();
    }

    @Test
    void createAndGetLocation() throws Exception {
        Location loc = Location.builder().name("Lab").build();
        loc = locationRepository.save(loc);

        mockMvc.perform(get("/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Lab"));

        mockMvc.perform(get("/locations/" + loc.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lab"));
    }

    @Test
    void createAndGetThermometer() throws Exception {
        Location loc = locationRepository.save(Location.builder().name("Office").build());
        Thermometer th = Thermometer.builder().name("TH1").location(loc).build();
        th = thermometerRepository.save(th);

        mockMvc.perform(get("/thermometers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("TH1"));

        mockMvc.perform(get("/thermometers/" + th.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("TH1"));
    }
}