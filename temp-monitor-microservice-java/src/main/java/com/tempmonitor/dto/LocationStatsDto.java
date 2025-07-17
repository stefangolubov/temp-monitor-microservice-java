package com.tempmonitor.dto;

import lombok.Data;

@Data
public class LocationStatsDto {
    private Integer locationId;
    private Double min;
    private Double max;
    private Double avg;
    private Long count;
}