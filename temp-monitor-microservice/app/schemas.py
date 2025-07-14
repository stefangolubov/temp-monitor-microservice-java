from pydantic import BaseModel
from datetime import datetime

class LocationBase(BaseModel):
    name: str

class Location(LocationBase):
    id: int
    class Config:
        orm_mode = True

class ThermometerBase(BaseModel):
    name: str
    location_id: int

class Thermometer(ThermometerBase):
    id: int
    class Config:
        orm_mode = True

class TemperatureReadingBase(BaseModel):
    thermometer_id: int
    value: float

class TemperatureReadingCreate(TemperatureReadingBase):
    pass

class TemperatureReading(TemperatureReadingBase):
    id: int
    timestamp: datetime
    class Config:
        orm_mode = True

class ThermometerStats(BaseModel):
    thermometer_id: int
    min: float | None
    max: float | None
    avg: float | None
    count: int

class LocationStats(BaseModel):
    location_id: int
    min: float | None
    max: float | None
    avg: float | None
    count: int