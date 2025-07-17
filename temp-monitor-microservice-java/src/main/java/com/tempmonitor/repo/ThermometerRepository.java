package com.tempmonitor.repo;

import com.tempmonitor.entity.Location;
import com.tempmonitor.entity.Thermometer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ThermometerRepository extends JpaRepository<Thermometer, Integer> {
    List<Thermometer> findByLocation(Location location);
}