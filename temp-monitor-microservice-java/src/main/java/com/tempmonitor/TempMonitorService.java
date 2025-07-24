package com.tempmonitor;

import com.tempmonitor.dto.*;
import com.tempmonitor.entity.Location;
import com.tempmonitor.entity.TemperatureReading;
import com.tempmonitor.entity.Thermometer;
import com.tempmonitor.repo.LocationRepository;
import com.tempmonitor.repo.TemperatureReadingRepository;
import com.tempmonitor.repo.ThermometerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TempMonitorService {

    private static final String LOCATION_NOT_FOUND = "Location not found";
    private static final String LOCATION_NOT_FOUND_WITH_ID = "Location not found with id={}";
    private static final String THERMOMETER_NOT_FOUND = "Thermometer not found";
    private static final String THERMOMETER_NOT_FOUND_WITH_ID = "Thermometer not  with id={}";
    private final LocationRepository locationRepo;
    private final ThermometerRepository thermometerRepo;
    private final TemperatureReadingRepository readingRepo;

    public List<LocationDto> getLocations() {
        log.info("Fetching all locations");
        return locationRepo.findAll().stream().map(DtoFactory::createLocationDto).toList();
    }

    public LocationDto getLocation(Integer id) {
        log.info("Fetching location with id {}", id);
        Location loc = locationRepo.findById(id).orElseThrow(() -> {
            log.warn(LOCATION_NOT_FOUND_WITH_ID, id);
            return new NoSuchElementException(LOCATION_NOT_FOUND);
        });
        return DtoFactory.createLocationDto(loc);
    }

    public LocationDto createLocation(String name) {
        log.info("Creating location: {}", name);
        Location loc = Location.builder().name(name).build();
        return DtoFactory.createLocationDto(locationRepo.save(loc));
    }

    public List<ThermometerDto> getThermometers(Integer locationId) {
        log.info("Fetching thermometers for location: {}", locationId);
        List<Thermometer> therms = (locationId == null) ? thermometerRepo.findAll()
                : thermometerRepo.findByLocation(locationRepo.getReferenceById(locationId));
        return therms.stream().map(DtoFactory::createThermometerDto).toList();
    }

    public ThermometerDto getThermometer(Integer id) {
        log.info("Fetching thermometer with id {}", id);
        Thermometer therm = thermometerRepo.findById(id).orElseThrow(() -> {
            log.warn(THERMOMETER_NOT_FOUND_WITH_ID, id);
            return new NoSuchElementException(THERMOMETER_NOT_FOUND);
        });
        return DtoFactory.createThermometerDto(therm);
    }

    public ThermometerDto createThermometer(String name, Integer locationId) {
        log.info("Creating thermometer '{}' for location {}", name, locationId);
        Location loc = locationRepo.findById(locationId).orElseThrow(() -> {
            log.warn(LOCATION_NOT_FOUND_WITH_ID, locationId);
            return new NoSuchElementException(LOCATION_NOT_FOUND);
        });
        Thermometer therm = Thermometer.builder().name(name).location(loc).build();
        return DtoFactory.createThermometerDto(thermometerRepo.save(therm));
    }

    public TemperatureReadingDto addReading(TemperatureReadingCreateDto dto) {
        log.info("Adding temperature reading for thermometer {}", dto.getThermometerId());
        Thermometer therm = thermometerRepo.findById(dto.getThermometerId()).orElseThrow(() -> {
            log.warn(THERMOMETER_NOT_FOUND_WITH_ID, dto.getThermometerId());
            return new NoSuchElementException(THERMOMETER_NOT_FOUND);
        });
        TemperatureReading tr = TemperatureReading.builder()
                .thermometer(therm)
                .value(dto.getValue())
                .timestamp(Instant.now())
                .build();
        return DtoFactory.createTemperatureReadingDto(readingRepo.save(tr));
    }

    public List<TemperatureReadingDto> getLatestReadings() {
        log.info("Fetching latest temperature readings for all thermometers");
        return readingRepo.findLatestForAllThermometers().stream().map(DtoFactory::createTemperatureReadingDto).toList();
    }

    public List<TemperatureReadingDto> getReadingsForThermometer(Integer thermometerId, int limit) {
        log.info("Fetching latest {} readings for thermometer {}", limit, thermometerId);
        Thermometer therm = thermometerRepo.findById(thermometerId).orElseThrow(() -> {
            log.warn(THERMOMETER_NOT_FOUND_WITH_ID, thermometerId);
            return new NoSuchElementException(THERMOMETER_NOT_FOUND);
        });
        return readingRepo.findTop10ByThermometerOrderByTimestampDesc(therm).stream().limit(limit).map(DtoFactory::createTemperatureReadingDto).toList();
    }

    public ThermometerStatsDto getThermometerStats(Integer thermometerId) {
        log.info("Fetching stats for thermometer {}", thermometerId);
        Thermometer therm = thermometerRepo.findById(thermometerId)
                .orElseThrow(() -> {
                    log.warn(THERMOMETER_NOT_FOUND_WITH_ID, thermometerId);
                    return new NoSuchElementException(THERMOMETER_NOT_FOUND);
                });
        List<Object[]> statsList = readingRepo.statsForThermometer(therm);
        Object[] stats = statsList.isEmpty() ? new Object[4] : statsList.getFirst();

        ThermometerStatsDto dto = new ThermometerStatsDto();
        dto.setThermometerId(thermometerId);
        dto.setMin(stats[0] != null ? ((Number) stats[0]).doubleValue() : null);
        dto.setMax(stats[1] != null ? ((Number) stats[1]).doubleValue() : null);
        dto.setAvg(stats[2] != null ? ((Number) stats[2]).doubleValue() : null);
        dto.setCount(stats[3] != null ? ((Number) stats[3]).longValue() : null);
        return dto;
    }

    public LocationStatsDto getLocationStats(Integer locationId) {
        log.info("Fetching stats for location {}", locationId);
        Location loc = locationRepo.findById(locationId)
                .orElseThrow(() -> {
                    log.warn(LOCATION_NOT_FOUND_WITH_ID, locationId);
                    return new NoSuchElementException(LOCATION_NOT_FOUND);
                });
        List<Integer> thermIds = loc.getThermometers().stream().map(Thermometer::getId).toList();
        if (thermIds.isEmpty()) {
            log.info("No thermometers found for location {}", locationId);
            LocationStatsDto dto = new LocationStatsDto();
            dto.setLocationId(locationId);
            return dto;
        }
        List<Object[]> statsList = readingRepo.statsForLocation(thermIds);
        Object[] stats = statsList.isEmpty() ? new Object[4] : statsList.getFirst();
        LocationStatsDto dto = new LocationStatsDto();
        dto.setLocationId(locationId);
        dto.setMin(stats[0] != null ? ((Number) stats[0]).doubleValue() : null);
        dto.setMax(stats[1] != null ? ((Number) stats[1]).doubleValue() : null);
        dto.setAvg(stats[2] != null ? ((Number) stats[2]).doubleValue() : null);
        dto.setCount(stats[3] != null ? ((Number) stats[3]).longValue() : null);
        return dto;
    }

    public void initDemoData() {
        log.info("Initializing demo data");
        List<String> names = List.of("Living Room", "Kitchen", "Basement");
        for (String locName : names) {
            LocationDto loc = createLocation(locName);
            for (int tnum = 1; tnum <= 3; tnum++) {
                createThermometer(locName + " Thermometer " + tnum, loc.getId());
            }
        }
        log.info("Demo data initialization complete");
    }
}