package com.tempmonitor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tempmonitor.TempMonitorService;
import com.tempmonitor.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TempMonitorController.class)
class TempMonitorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TempMonitorService service;

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testInitDemoData() throws Exception {
        doNothing().when(service).initDemoData();
        mockMvc.perform(post("/init-demo-data"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\": \"Demo data created\"}"));
        verify(service).initDemoData();
    }

    @Test
    void testGetLocations() throws Exception {
        LocationDto dto = new LocationDto();
        dto.setId(1);
        dto.setName("Room1");
        when(service.getLocations()).thenReturn(List.of(dto));
        mockMvc.perform(get("/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Room1"));
    }

    @Test
    void testGetLocation() throws Exception {
        LocationDto dto = new LocationDto();
        dto.setId(1);
        dto.setName("Room1");
        when(service.getLocation(1)).thenReturn(dto);
        mockMvc.perform(get("/locations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Room1"));
    }

    @Test
    void testGetThermometers() throws Exception {
        ThermometerDto dto = new ThermometerDto();
        dto.setId(2);
        dto.setName("Thermo1");
        dto.setLocationId(1);
        when(service.getThermometers(null)).thenReturn(List.of(dto));
        mockMvc.perform(get("/thermometers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    void testGetThermometer() throws Exception {
        ThermometerDto dto = new ThermometerDto();
        dto.setId(2);
        dto.setName("Thermo1");
        dto.setLocationId(1);
        when(service.getThermometer(2)).thenReturn(dto);
        mockMvc.perform(get("/thermometers/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void testAddReading() throws Exception {
        TemperatureReadingCreateDto createDto = new TemperatureReadingCreateDto();
        createDto.setThermometerId(1);
        createDto.setValue(23.5);
        TemperatureReadingDto dto = new TemperatureReadingDto();
        dto.setId(3);
        dto.setThermometerId(1);
        dto.setValue(23.5);
        dto.setTimestamp(Instant.now());
        when(service.addReading(any())).thenReturn(dto);
        mockMvc.perform(post("/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.thermometerId").value(1))
                .andExpect(jsonPath("$.value").value(23.5));
    }

    @Test
    void testGetLatestReadings() throws Exception {
        TemperatureReadingDto dto = new TemperatureReadingDto();
        dto.setId(4);
        dto.setThermometerId(2);
        dto.setValue(18.0);
        dto.setTimestamp(Instant.now());
        when(service.getLatestReadings()).thenReturn(List.of(dto));
        mockMvc.perform(get("/readings/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(4))
                .andExpect(jsonPath("$[0].value").value(18.0));
    }

    @Test
    void testGetReadingsForThermometer() throws Exception {
        TemperatureReadingDto dto = new TemperatureReadingDto();
        dto.setId(5);
        dto.setThermometerId(3);
        dto.setValue(20.1);
        dto.setTimestamp(Instant.now());
        when(service.getReadingsForThermometer(3, 10)).thenReturn(List.of(dto));
        mockMvc.perform(get("/readings/3?limit=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].value").value(20.1));
    }

    @Test
    void testStatsForThermometer() throws Exception {
        ThermometerStatsDto dto = new ThermometerStatsDto();
        dto.setThermometerId(2);
        dto.setMin(10.0);
        dto.setMax(30.0);
        dto.setAvg(20.0);
        dto.setCount(5L);
        when(service.getThermometerStats(2)).thenReturn(dto);
        mockMvc.perform(get("/stats/thermometer/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thermometerId").value(2))
                .andExpect(jsonPath("$.min").value(10.0))
                .andExpect(jsonPath("$.max").value(30.0))
                .andExpect(jsonPath("$.avg").value(20.0))
                .andExpect(jsonPath("$.count").value(5));
    }

    @Test
    void testStatsForLocation() throws Exception {
        LocationStatsDto dto = new LocationStatsDto();
        dto.setLocationId(1);
        dto.setMin(9.0);
        dto.setMax(33.0);
        dto.setAvg(17.0);
        dto.setCount(12L);
        when(service.getLocationStats(1)).thenReturn(dto);
        mockMvc.perform(get("/stats/location/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locationId").value(1))
                .andExpect(jsonPath("$.min").value(9.0))
                .andExpect(jsonPath("$.max").value(33.0))
                .andExpect(jsonPath("$.avg").value(17.0))
                .andExpect(jsonPath("$.count").value(12));
    }

    @Test
    void testGetLocation_notFound_returns404() throws Exception {
        when(service.getLocation(404)).thenThrow(new NoSuchElementException("Location not found"));
        mockMvc.perform(get("/locations/404"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetThermometer_notFound_returns404() throws Exception {
        when(service.getThermometer(123)).thenThrow(new NoSuchElementException("Thermometer not found"));
        mockMvc.perform(get("/thermometers/123"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAddReading_invalidPayload_returns400() throws Exception {
        mockMvc.perform(post("/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddReading_thermometerNotFound_returns404() throws Exception {
        TemperatureReadingCreateDto createDto = new TemperatureReadingCreateDto();
        createDto.setThermometerId(321);
        createDto.setValue(35.2);
        when(service.addReading(any())).thenThrow(new NoSuchElementException("Thermometer not found"));

        mockMvc.perform(post("/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetReadingsForThermometer_notFound_returns404() throws Exception {
        when(service.getReadingsForThermometer(999, 10)).thenThrow(new NoSuchElementException("Thermometer not found"));
        mockMvc.perform(get("/readings/999?limit=10"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testStatsForThermometer_notFound_returns404() throws Exception {
        when(service.getThermometerStats(101)).thenThrow(new NoSuchElementException("Thermometer not found"));
        mockMvc.perform(get("/stats/thermometer/101"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testStatsForLocation_notFound_returns404() throws Exception {
        when(service.getLocationStats(202)).thenThrow(new NoSuchElementException("Location not found"));
        mockMvc.perform(get("/stats/location/202"))
                .andExpect(status().isNotFound());
    }
}