ALTER TABLE ticket_purchases
    ADD COLUMN IF NOT EXISTS transport_id UUID,
    ADD COLUMN IF NOT EXISTS from_stop_id UUID,
    ADD COLUMN IF NOT EXISTS to_stop_id UUID,
    ADD COLUMN IF NOT EXISTS stop_count INT;

ALTER TABLE ticket_purchases
    ADD CONSTRAINT fk_ticket_purchase_transport
        FOREIGN KEY (transport_id)
        REFERENCES transport(id)
        ON DELETE SET NULL;

ALTER TABLE ticket_purchases
    ADD CONSTRAINT fk_ticket_purchase_from_stop
        FOREIGN KEY (from_stop_id)
        REFERENCES transportstop(id)
        ON DELETE SET NULL;

ALTER TABLE ticket_purchases
    ADD CONSTRAINT fk_ticket_purchase_to_stop
        FOREIGN KEY (to_stop_id)
        REFERENCES transportstop(id)
        ON DELETE SET NULL;

ALTER TABLE ticket_purchases
    ADD CONSTRAINT chk_ticket_purchase_distinct_stops
        CHECK (from_stop_id IS NULL OR to_stop_id IS NULL OR from_stop_id <> to_stop_id);

ALTER TABLE ticket_purchases
    ADD CONSTRAINT chk_ticket_purchase_stop_count_positive
        CHECK (stop_count IS NULL OR stop_count > 0);

CREATE INDEX IF NOT EXISTS idx_ticket_purchases_transport ON ticket_purchases (transport_id);
CREATE INDEX IF NOT EXISTS idx_ticket_purchases_from_stop ON ticket_purchases (from_stop_id);
CREATE INDEX IF NOT EXISTS idx_ticket_purchases_to_stop ON ticket_purchases (to_stop_id);
