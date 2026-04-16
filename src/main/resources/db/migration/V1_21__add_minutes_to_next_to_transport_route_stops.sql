ALTER TABLE transport_route_stops
    ADD COLUMN IF NOT EXISTS minutes_to_next INT;

ALTER TABLE transport_route_stops
    DROP CONSTRAINT IF EXISTS chk_transport_route_stop_minutes_to_next;

ALTER TABLE transport_route_stops
    ADD CONSTRAINT chk_transport_route_stop_minutes_to_next
        CHECK (minutes_to_next IS NULL OR minutes_to_next > 0);
