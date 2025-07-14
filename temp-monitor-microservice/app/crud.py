from sqlalchemy.orm import Session
from sqlalchemy import func
from . import models, schemas
from datetime import datetime

# LOCATIONS
def get_locations(db: Session):
    return db.query(models.Location).all()

def get_location(db: Session, location_id: int):
    return db.query(models.Location).filter(models.Location.id == location_id).first()

def create_location(db: Session, name: str):
    loc = models.Location(name=name)
    db.add(loc)
    db.commit()
    db.refresh(loc)
    return loc

# THERMOMETERS
def get_thermometers(db: Session, location_id: int = None):
    query = db.query(models.Thermometer)
    if location_id:
        query = query.filter(models.Thermometer.location_id == location_id)
    return query.all()

def get_thermometer(db: Session, thermometer_id: int):
    return db.query(models.Thermometer).filter(models.Thermometer.id == thermometer_id).first()

def create_thermometer(db: Session, name: str, location_id: int):
    therm = models.Thermometer(name=name, location_id=location_id)
    db.add(therm)
    db.commit()
    db.refresh(therm)
    return therm

# READINGS
def add_reading(db: Session, thermometer_id: int, value: float):
    reading = models.TemperatureReading(thermometer_id=thermometer_id, value=value, timestamp=datetime.utcnow())
    db.add(reading)
    db.commit()
    db.refresh(reading)
    return reading

def get_latest_readings(db: Session):
    # Get latest reading per thermometer
    subq = db.query(
        models.TemperatureReading.thermometer_id,
        func.max(models.TemperatureReading.timestamp).label("max_ts")
    ).group_by(models.TemperatureReading.thermometer_id).subquery()

    query = db.query(models.TemperatureReading).join(
        subq,
        (models.TemperatureReading.thermometer_id == subq.c.thermometer_id) &
        (models.TemperatureReading.timestamp == subq.c.max_ts)
    )
    return query.all()

def get_readings_for_thermometer(db: Session, thermometer_id: int, limit: int = 10):
    return (
        db.query(models.TemperatureReading)
        .filter(models.TemperatureReading.thermometer_id == thermometer_id)
        .order_by(models.TemperatureReading.timestamp.desc())
        .limit(limit)
        .all()
    )

# STATS
def thermometer_stats(db: Session, thermometer_id: int):
    q = db.query(
        func.min(models.TemperatureReading.value),
        func.max(models.TemperatureReading.value),
        func.avg(models.TemperatureReading.value),
        func.count(models.TemperatureReading.value)
    ).filter(models.TemperatureReading.thermometer_id == thermometer_id)
    min_, max_, avg_, count_ = q.one()
    return schemas.ThermometerStats(
        thermometer_id=thermometer_id,
        min=min_,
        max=max_,
        avg=avg_,
        count=count_
    )

def location_stats(db: Session, location_id: int):
    # Get all thermometer ids for location
    therms = db.query(models.Thermometer.id).filter(models.Thermometer.location_id == location_id).subquery()
    q = db.query(
        func.min(models.TemperatureReading.value),
        func.max(models.TemperatureReading.value),
        func.avg(models.TemperatureReading.value),
        func.count(models.TemperatureReading.value)
    ).filter(models.TemperatureReading.thermometer_id.in_(therms))
    min_, max_, avg_, count_ = q.one()
    return schemas.LocationStats(
        location_id=location_id,
        min=min_,
        max=max_,
        avg=avg_,
        count=count_
    )