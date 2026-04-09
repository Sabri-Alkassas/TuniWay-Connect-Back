-- Backfill and enforce non-null active flags to match JPA mappings.

UPDATE transportdepartureslots
SET active = TRUE
WHERE active IS NULL;

ALTER TABLE transportdepartureslots
    ALTER COLUMN active SET DEFAULT TRUE,
    ALTER COLUMN active SET NOT NULL;

UPDATE transport_route_stops
SET active = TRUE
WHERE active IS NULL;

ALTER TABLE transport_route_stops
    ALTER COLUMN active SET DEFAULT TRUE,
    ALTER COLUMN active SET NOT NULL;
