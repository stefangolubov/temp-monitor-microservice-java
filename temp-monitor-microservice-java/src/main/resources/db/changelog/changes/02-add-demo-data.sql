--liquibase formatted sql

--changeset stefangolubov:2
INSERT INTO locations (name) VALUES ('Living Room')
  ON CONFLICT (name) DO NOTHING;
INSERT INTO locations (name) VALUES ('Kitchen')
  ON CONFLICT (name) DO NOTHING;
INSERT INTO locations (name) VALUES ('Basement')
  ON CONFLICT (name) DO NOTHING;