ALTER TABLE transport_route_stops
    DROP CONSTRAINT IF EXISTS chk_transport_route_stop_minutes_to_next;

ALTER TABLE transport_route_stops
    DROP COLUMN IF EXISTS minutes_to_next;
