package com.tempmonitor.controller;


import com.tempmonitor.TempMonitorService;
import com.tempmonitor.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class TempMonitorController {
    private final TempMonitorService service;

    // --- DEMO DATA ---
    @PostMapping("/init-demo-data")
    @Operation(summary = "Create demo locations/thermometers")
    public String initDemoData() {
        service.initDemoData();
        return "{\"message\": \"Demo data created\"}";
    }

    @GetMapping("/locations")
    public List<LocationDto> getLocations() {
        return service.getLocations();
    }

    @GetMapping("/locations/{locationId}")
    public LocationDto getLocation(@PathVariable Integer locationId) {
        return service.getLocation(locationId);
    }

    @GetMapping("/thermometers")
    public List<ThermometerDto> getThermometers(@RequestParam(required = false) Integer locationId) {
        return service.getThermometers(locationId);
    }

    @GetMapping("/thermometers/{thermometerId}")
    public ThermometerDto getThermometer(@PathVariable Integer thermometerId) {
        return service.getThermometer(thermometerId);
    }

    @PostMapping("/readings")
    public TemperatureReadingDto addReading(@Valid @RequestBody TemperatureReadingCreateDto dto) {
        return service.addReading(dto);
    }

    @GetMapping("/readings/latest")
    public List<TemperatureReadingDto> getLatestReadings() {
        return service.getLatestReadings();
    }

    @GetMapping("/readings/{thermometerId}")
    public List<TemperatureReadingDto> getReadingsForThermometer(
            @PathVariable Integer thermometerId,
            @RequestParam(defaultValue = "10") int limit) {
        return service.getReadingsForThermometer(thermometerId, limit);
    }

    @GetMapping("/stats/thermometer/{thermometerId}")
    public ThermometerStatsDto statsForThermometer(@PathVariable Integer thermometerId) {
        return service.getThermometerStats(thermometerId);
    }

    @GetMapping("/stats/location/{locationId}")
    public LocationStatsDto statsForLocation(@PathVariable Integer locationId) {
        return service.getLocationStats(locationId);
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleNotFound() {
        // Returns 404 - Not Found with no body
    }
}