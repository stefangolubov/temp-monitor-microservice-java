package com.tempmonitor.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class TemperatureReadingDto {
    private Integer id;
    private Integer thermometerId;
    private Double value;
    private Instant timestamp;
}