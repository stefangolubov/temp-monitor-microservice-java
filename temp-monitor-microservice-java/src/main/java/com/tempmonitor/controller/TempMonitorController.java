package com.tempmonitor.controller;

import com.tempmonitor.TempMonitorService;
import com.tempmonitor.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class TempMonitorController {
    private final TempMonitorService service;

    // --- DEMO DATA ---
    @PostMapping("/init-demo-data")
    @Operation(summary = "Create demo locations/thermometers")
    public String initDemoData() {
        log.info("Received request to initialize demo data");
        service.initDemoData();
        log.info("Demo data created");
        return "{\"message\": \"Demo data created\"}";
    }

    @GetMapping("/locations")
    public List<LocationDto> getLocations() {
        log.info("GET /locations called");
        return service.getLocations();
    }

    @GetMapping("/locations/{locationId}")
    public LocationDto getLocation(@PathVariable Integer locationId) {
        log.info("GET /locations/{} called", locationId);
        return service.getLocation(locationId);
    }

    @GetMapping("/thermometers")
    public List<ThermometerDto> getThermometers(@RequestParam(required = false) Integer locationId) {
        log.info("GET /thermometers called for locationId={}", locationId);
        return service.getThermometers(locationId);
    }

    @GetMapping("/thermometers/{thermometerId}")
    public ThermometerDto getThermometer(@PathVariable Integer thermometerId) {
        log.info("GET /thermometers/{} called", thermometerId);
        return service.getThermometer(thermometerId);
    }

    @PostMapping("/readings")
    public TemperatureReadingDto addReading(@Valid @RequestBody TemperatureReadingCreateDto dto) {
        log.info("POST /readings called for thermometerId={}", dto.getThermometerId());
        return service.addReading(dto);
    }

    @GetMapping("/readings/latest")
    public List<TemperatureReadingDto> getLatestReadings() {
        log.info("GET /readings/latest called");
        return service.getLatestReadings();
    }

    @GetMapping("/readings/{thermometerId}")
    public List<TemperatureReadingDto> getReadingsForThermometer(
            @PathVariable Integer thermometerId,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /readings/{} called with limit={}", thermometerId, limit);
        return service.getReadingsForThermometer(thermometerId, limit);
    }

    @GetMapping("/stats/thermometer/{thermometerId}")
    public ThermometerStatsDto statsForThermometer(@PathVariable Integer thermometerId) {
        log.info("GET /stats/thermometer/{} called", thermometerId);
        return service.getThermometerStats(thermometerId);
    }

    @GetMapping("/stats/location/{locationId}")
    public LocationStatsDto statsForLocation(@PathVariable Integer locationId) {
        log.info("GET /stats/location/{} called", locationId);
        return service.getLocationStats(locationId);
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleNotFound() {
        log.warn("Resource not found, returning 404");
        // Returns 404 - Not Found with no body
    }
}