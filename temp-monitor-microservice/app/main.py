from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy.orm import Session
from . import models, schemas, crud
from .database import SessionLocal, engine, Base

Base.metadata.create_all(bind=engine)

app = FastAPI(title="Temperature Monitoring Microservice")

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

# --- INIT DATA ENDPOINT (optional, for demo/testing) ---
@app.post("/init-demo-data", summary="Create demo locations/thermometers")
def init_demo_data(db: Session = Depends(get_db)):
    names = ["Living Room", "Kitchen", "Basement"]
    for loc_name in names:
        loc = crud.create_location(db, loc_name)
        for tnum in range(1, 4):
            crud.create_thermometer(db, f"{loc_name} Thermometer {tnum}", loc.id)
    return {"message": "Demo data created"}

# --- LOCATIONS ---
@app.get("/locations", response_model=list[schemas.Location])
def get_locations(db: Session = Depends(get_db)):
    return crud.get_locations(db)

@app.get("/locations/{location_id}", response_model=schemas.Location)
def get_location(location_id: int, db: Session = Depends(get_db)):
    loc = crud.get_location(db, location_id)
    if not loc:
        raise HTTPException(status_code=404, detail="Location not found")
    return loc

# --- THERMOMETERS ---
@app.get("/thermometers", response_model=list[schemas.Thermometer])
def get_thermometers(location_id: int | None = None, db: Session = Depends(get_db)):
    return crud.get_thermometers(db, location_id)

@app.get("/thermometers/{thermometer_id}", response_model=schemas.Thermometer)
def get_thermometer(thermometer_id: int, db: Session = Depends(get_db)):
    therm = crud.get_thermometer(db, thermometer_id)
    if not therm:
        raise HTTPException(status_code=404, detail="Thermometer not found")
    return therm

# --- TEMPERATURE READINGS ---
@app.post("/readings/", response_model=schemas.TemperatureReading)
def add_reading(reading: schemas.TemperatureReadingCreate, db: Session = Depends(get_db)):
    therm = crud.get_thermometer(db, reading.thermometer_id)
    if not therm:
        raise HTTPException(status_code=404, detail="Thermometer not found")
    return crud.add_reading(db, reading.thermometer_id, reading.value)

@app.get("/readings/latest", response_model=list[schemas.TemperatureReading])
def get_latest_readings(db: Session = Depends(get_db)):
    return crud.get_latest_readings(db)

@app.get("/readings/{thermometer_id}", response_model=list[schemas.TemperatureReading])
def get_readings_for_thermometer(thermometer_id: int, limit: int = 10, db: Session = Depends(get_db)):
    therm = crud.get_thermometer(db, thermometer_id)
    if not therm:
        raise HTTPException(status_code=404, detail="Thermometer not found")
    return crud.get_readings_for_thermometer(db, thermometer_id, limit)

# --- STATS ---
@app.get("/stats/thermometer/{thermometer_id}", response_model=schemas.ThermometerStats)
def stats_for_thermometer(thermometer_id: int, db: Session = Depends(get_db)):
    therm = crud.get_thermometer(db, thermometer_id)
    if not therm:
        raise HTTPException(status_code=404, detail="Thermometer not found")
    return crud.thermometer_stats(db, thermometer_id)

@app.get("/stats/location/{location_id}", response_model=schemas.LocationStats)
def stats_for_location(location_id: int, db: Session = Depends(get_db)):
    loc = crud.get_location(db, location_id)
    if not loc:
        raise HTTPException(status_code=404, detail="Location not found")
    return crud.location_stats(db, location_id)