ALTER TABLE transport
    ADD COLUMN IF NOT EXISTS capacity INT NOT NULL DEFAULT 100;

ALTER TABLE transport
    ADD CONSTRAINT chk_transport_capacity_positive
        CHECK (capacity > 0);
