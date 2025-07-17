package com.tempmonitor.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "temperature_readings")
public class TemperatureReading {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "thermometer_id", nullable = false)
    private Thermometer thermometer;

    @Column(name = "\"value\"", nullable = false)
    private Double value;

    @Column(nullable = false)
    private Instant timestamp;
}