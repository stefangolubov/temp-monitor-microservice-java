package com.tempmonitor.service;

import com.tempmonitor.TempMonitorService;
import com.tempmonitor.dto.*;
import com.tempmonitor.entity.Location;
import com.tempmonitor.entity.TemperatureReading;
import com.tempmonitor.entity.Thermometer;
import com.tempmonitor.repo.LocationRepository;
import com.tempmonitor.repo.TemperatureReadingRepository;
import com.tempmonitor.repo.ThermometerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class TempMonitorServiceTest {

    private LocationRepository locationRepo;
    private ThermometerRepository thermometerRepo;
    private TemperatureReadingRepository readingRepo;
    private TempMonitorService service;

    @BeforeEach
    void setup() {
        locationRepo = mock(LocationRepository.class);
        thermometerRepo = mock(ThermometerRepository.class);
        readingRepo = mock(TemperatureReadingRepository.class);
        service = new TempMonitorService(locationRepo, thermometerRepo, readingRepo);
    }

    @Test
    void getLocations_returnsDtos() {
        Location loc = Location.builder().id(1).name("A").build();
        when(locationRepo.findAll()).thenReturn(List.of(loc));
        List<LocationDto> dtos = service.getLocations();
        assertThat(dtos).hasSize(1);
        assertThat(dtos.getFirst().getName()).isEqualTo("A");
    }

    @Test
    void getLocation_success() {
        Location loc = Location.builder().id(5).name("B").build();
        when(locationRepo.findById(5)).thenReturn(Optional.of(loc));
        LocationDto dto = service.getLocation(5);
        assertThat(dto.getId()).isEqualTo(5);
        assertThat(dto.getName()).isEqualTo("B");
    }

    @Test
    void getLocation_notFound() {
        when(locationRepo.findById(42)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getLocation(42))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Location not found");
    }

    @Test
    void createLocation_savesAndReturnsDto() {
        Location loc = Location.builder().id(10).name("C").build();
        when(locationRepo.save(any())).thenReturn(loc);
        LocationDto dto = service.createLocation("C");
        assertThat(dto.getId()).isEqualTo(10);
        assertThat(dto.getName()).isEqualTo("C");
    }

    @Test
    void getThermometers_withLocationId() {
        Location location = Location.builder().id(2).name("L").build();
        Thermometer t = Thermometer.builder().id(1).name("T").location(location).build();
        when(thermometerRepo.findByLocation(location)).thenReturn(List.of(t));
        when(locationRepo.getReferenceById(2)).thenReturn(location);
        List<ThermometerDto> list = service.getThermometers(2);
        assertThat(list).hasSize(1);
        assertThat(list.getFirst().getLocationId()).isEqualTo(2);
    }

    @Test
    void getThermometer_notFound() {
        when(thermometerRepo.findById(99)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getThermometer(99))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void addReading_success() {
        Thermometer therm = Thermometer.builder().id(3).name("T").location(Location.builder().id(1).build()).build();
        TemperatureReading reading = TemperatureReading.builder().id(9).thermometer(therm).value(22.5).timestamp(Instant.now()).build();
        TemperatureReadingCreateDto dto = new TemperatureReadingCreateDto();
        dto.setThermometerId(3);
        dto.setValue(22.5);
        when(thermometerRepo.findById(3)).thenReturn(Optional.of(therm));
        when(readingRepo.save(any())).thenReturn(reading);
        TemperatureReadingDto result = service.addReading(dto);
        assertThat(result.getId()).isEqualTo(9);
        assertThat(result.getThermometerId()).isEqualTo(3);
        assertThat(result.getValue()).isEqualTo(22.5);
    }

    @Test
    void getLatestReadings_delegates() {
        TemperatureReading reading = TemperatureReading.builder().id(1).thermometer(Thermometer.builder().id(1).location(Location.builder().id(2).build()).build()).value(11.2).timestamp(Instant.now()).build();
        when(readingRepo.findLatestForAllThermometers()).thenReturn(List.of(reading));
        List<TemperatureReadingDto> list = service.getLatestReadings();
        assertThat(list).hasSize(1);
        assertThat(list.getFirst().getValue()).isEqualTo(11.2);
    }

    @Test
    void getReadingsForThermometer_success() {
        Thermometer therm = Thermometer.builder().id(1).location(Location.builder().id(2).build()).build();
        TemperatureReading reading = TemperatureReading.builder().id(1).thermometer(therm).value(1.1).timestamp(Instant.now()).build();
        when(thermometerRepo.findById(1)).thenReturn(Optional.of(therm));
        when(readingRepo.findTop10ByThermometerOrderByTimestampDesc(therm)).thenReturn(List.of(reading));
        List<TemperatureReadingDto> res = service.getReadingsForThermometer(1, 10);
        assertThat(res).hasSize(1);
        assertThat(res.getFirst().getValue()).isEqualTo(1.1);
    }

    @Test
    void getThermometerStats_success() {
        Thermometer therm = Thermometer.builder().id(1).location(Location.builder().id(2).build()).build();
        when(thermometerRepo.findById(1)).thenReturn(Optional.of(therm));
        when(readingRepo.statsForThermometer(therm)).thenReturn(List.<Object[]>of(new Object[]{1.0, 5.0, 3.0, 3L}));
        ThermometerStatsDto dto = service.getThermometerStats(1);
        assertThat(dto.getMin()).isEqualTo(1.0);
        assertThat(dto.getMax()).isEqualTo(5.0);
        assertThat(dto.getAvg()).isEqualTo(3.0);
        assertThat(dto.getCount()).isEqualTo(3L);
    }

    @Test
    void getLocationStats_noThermometers() {
        Location loc = Location.builder().id(1).thermometers(Collections.emptyList()).build();
        when(locationRepo.findById(1)).thenReturn(Optional.of(loc));
        LocationStatsDto dto = service.getLocationStats(1);
        assertThat(dto.getLocationId()).isEqualTo(1);
        assertThat(dto.getMin()).isNull();
        assertThat(dto.getMax()).isNull();
        assertThat(dto.getAvg()).isNull();
        assertThat(dto.getCount()).isNull();
    }

    @Test
    void getLocationStats_success() {
        Location loc = Location.builder().id(2).thermometers(List.of(Thermometer.builder().id(11).build())).build();
        when(locationRepo.findById(2)).thenReturn(Optional.of(loc));
        when(readingRepo.statsForLocation(List.of(11))).thenReturn(List.<Object[]>of(new Object[]{2.0, 7.0, 4.5, 4L}));
        LocationStatsDto dto = service.getLocationStats(2);
        assertThat(dto.getLocationId()).isEqualTo(2);
        assertThat(dto.getMin()).isEqualTo(2.0);
        assertThat(dto.getMax()).isEqualTo(7.0);
        assertThat(dto.getAvg()).isEqualTo(4.5);
        assertThat(dto.getCount()).isEqualTo(4L);
    }

    @Test
    void getThermometers_locationNotFound_shouldThrow() {
        when(locationRepo.getReferenceById(42)).thenThrow(new NoSuchElementException("Location not found"));
        assertThatThrownBy(() -> service.getThermometers(42))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Location not found");
    }

    @Test
    void getThermometer_notFound_throws() {
        when(thermometerRepo.findById(5)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getThermometer(5))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Thermometer not found");
    }

    @Test
    void addReading_thermometerNotFound_shouldThrow() {
        TemperatureReadingCreateDto dto = new TemperatureReadingCreateDto();
        dto.setThermometerId(123);
        dto.setValue(25.0);
        when(thermometerRepo.findById(123)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.addReading(dto))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Thermometer not found");
    }

    @Test
    void getReadingsForThermometer_thermometerNotFound_shouldThrow() {
        when(thermometerRepo.findById(999)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getReadingsForThermometer(999, 10))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Thermometer not found");
    }

    @Test
    void getThermometerStats_thermometerNotFound_shouldThrow() {
        when(thermometerRepo.findById(888)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getThermometerStats(888))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Thermometer not found");
    }

    @Test
    void getLocationStats_locationNotFound_shouldThrow() {
        when(locationRepo.findById(777)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getLocationStats(777))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Location not found");
    }
}