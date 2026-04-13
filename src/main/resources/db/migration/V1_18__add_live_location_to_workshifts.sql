ALTER TABLE workshifts
    ADD COLUMN IF NOT EXISTS current_latitude NUMERIC(9,6),
    ADD COLUMN IF NOT EXISTS current_longitude NUMERIC(9,6),
    ADD COLUMN IF NOT EXISTS current_location_updated_at TIMESTAMP;

ALTER TABLE workshifts
    DROP CONSTRAINT IF EXISTS chk_workshift_current_latitude;

ALTER TABLE workshifts
    ADD CONSTRAINT chk_workshift_current_latitude
    CHECK (current_latitude IS NULL OR current_latitude BETWEEN -90 AND 90);

ALTER TABLE workshifts
    DROP CONSTRAINT IF EXISTS chk_workshift_current_longitude;

ALTER TABLE workshifts
    ADD CONSTRAINT chk_workshift_current_longitude
    CHECK (current_longitude IS NULL OR current_longitude BETWEEN -180 AND 180);

ALTER TABLE workshifts
    DROP CONSTRAINT IF EXISTS chk_workshift_current_location_pair;

ALTER TABLE workshifts
    ADD CONSTRAINT chk_workshift_current_location_pair
    CHECK ((current_latitude IS NULL) = (current_longitude IS NULL));

CREATE INDEX IF NOT EXISTS idx_workshifts_transport_status_location_updated_at
    ON workshifts (transport_id, status, current_location_updated_at DESC);
