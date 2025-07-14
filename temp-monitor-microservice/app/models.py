from sqlalchemy import Column, Integer, String, Float, ForeignKey, DateTime
from sqlalchemy.orm import relationship
from datetime import datetime
from .database import Base

class Location(Base):
    __tablename__ = "locations"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, unique=True, index=True)

    thermometers = relationship("Thermometer", back_populates="location")

class Thermometer(Base):
    __tablename__ = "thermometers"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, index=True)
    location_id = Column(Integer, ForeignKey("locations.id"))

    location = relationship("Location", back_populates="thermometers")
    readings = relationship("TemperatureReading", back_populates="thermometer")

class TemperatureReading(Base):
    __tablename__ = "temperature_readings"
    id = Column(Integer, primary_key=True, index=True)
    thermometer_id = Column(Integer, ForeignKey("thermometers.id"))
    timestamp = Column(DateTime, default=datetime.utcnow)
    value = Column(Float)

    thermometer = relationship("Thermometer", back_populates="readings")