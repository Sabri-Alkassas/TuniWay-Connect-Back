CREATE TABLE transport_route_stops (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transport_id UUID NOT NULL,
    stop_id UUID NOT NULL,
    stop_order INT NOT NULL,
    active BOOLEAN DEFAULT TRUE,

    CONSTRAINT fk_transport_route_stop_transport
        FOREIGN KEY (transport_id)
        REFERENCES transport(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_transport_route_stop_stop
        FOREIGN KEY (stop_id)
        REFERENCES transportstop(id)
        ON DELETE CASCADE,
    CONSTRAINT uq_transport_route_stop_order
        UNIQUE (transport_id, stop_order),
    CONSTRAINT uq_transport_route_stop_stop
        UNIQUE (transport_id, stop_id),
    CONSTRAINT chk_transport_route_stop_order
        CHECK (stop_order > 0)
);
