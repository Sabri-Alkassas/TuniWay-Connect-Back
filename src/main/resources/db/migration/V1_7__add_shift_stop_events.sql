CREATE TABLE shift_stop_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workshift_id UUID NOT NULL,
    stop_id UUID NOT NULL,
    stop_order INT NOT NULL, -- To maintain the order of stops in the route
    expected_departure_time TIMESTAMP NOT NULL,
    arrived_at TIMESTAMP,
    departed_at TIMESTAMP,
    status VARCHAR(50) NOT NULL, -- "pending", "arrived", "departed", "skipped"
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,   
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_workshift
        FOREIGN KEY (workshift_id)
        REFERENCES workshifts(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_stop
        FOREIGN KEY (stop_id)
        REFERENCES transportstop(id)
        ON DELETE CASCADE,

    CONSTRAINT stop_order_check
        CHECK (stop_order > 0)
);