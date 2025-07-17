package com.tempmonitor.repo;

import com.tempmonitor.entity.TemperatureReading;
import com.tempmonitor.entity.Thermometer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TemperatureReadingRepository extends JpaRepository<TemperatureReading, Integer> {
    List<TemperatureReading> findTop10ByThermometerOrderByTimestampDesc(Thermometer thermometer);

    @Query("""
                SELECT tr FROM TemperatureReading tr
                WHERE (tr.thermometer.id, tr.timestamp) IN (
                    SELECT t.id, MAX(tr2.timestamp)
                    FROM Thermometer t
                    JOIN TemperatureReading tr2 ON tr2.thermometer.id = t.id
                    GROUP BY t.id
                )
            """)
    List<TemperatureReading> findLatestForAllThermometers();

    @Query("SELECT MIN(tr.value), MAX(tr.value), AVG(tr.value), COUNT(tr.id) FROM TemperatureReading tr WHERE tr.thermometer = :thermometer")
    List<Object[]> statsForThermometer(Thermometer thermometer);

    @Query("""
                SELECT MIN(tr.value), MAX(tr.value), AVG(tr.value), COUNT(tr.id)
                FROM TemperatureReading tr
                WHERE tr.thermometer.id IN :thermometerIds
            """)
    List<Object[]>  statsForLocation(List<Integer> thermometerIds);
}