package com.tempmonitor.dto;

import lombok.Data;

@Data
public class ThermometerStatsDto {
    private Integer thermometerId;
    private Double min;
    private Double max;
    private Double avg;
    private Long count;
}