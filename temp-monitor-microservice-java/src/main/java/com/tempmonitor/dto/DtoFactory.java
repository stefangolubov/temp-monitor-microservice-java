package com.tempmonitor.dto;

import com.tempmonitor.entity.Location;
import com.tempmonitor.entity.TemperatureReading;
import com.tempmonitor.entity.Thermometer;

public class DtoFactory {

    private DtoFactory() {
        throw new UnsupportedOperationException("DtoFactory class");
    }

    public static LocationDto createLocationDto(Location loc) {
        if (loc == null) return null;
        LocationDto dto = new LocationDto();
        dto.setId(loc.getId());
        dto.setName(loc.getName());
        return dto;
    }

    public static ThermometerDto createThermometerDto(Thermometer t) {
        if (t == null) return null;
        ThermometerDto dto = new ThermometerDto();
        dto.setId(t.getId());
        dto.setName(t.getName());
        dto.setLocationId(t.getLocation().getId());
        return dto;
    }

    public static TemperatureReadingDto createTemperatureReadingDto(TemperatureReading tr) {
        if (tr == null) return null;
        TemperatureReadingDto dto = new TemperatureReadingDto();
        dto.setId(tr.getId());
        dto.setThermometerId(tr.getThermometer().getId());
        dto.setValue(tr.getValue());
        dto.setTimestamp(tr.getTimestamp());
        return dto;
    }
}