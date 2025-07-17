package com.tempmonitor.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TemperatureReadingCreateDto {
    @NotNull
    private Integer thermometerId;

    @NotNull
    private Double value;
}