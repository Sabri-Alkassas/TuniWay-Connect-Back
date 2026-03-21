CREATE TABLE workshifts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id UUID NOT NULL,
    transport_id UUID NOT NULL,
    schedule_start TIMESTAMP NOT NULL,
    schedule_end TIMESTAMP NOT NULL,
    status VARCHAR(50),
    actual_start TIMESTAMP,
    actual_end TIMESTAMP

    CONSTRAINT fk_employee
        FOREIGN KEY (employee_id)
        REFERENCES employee_profiles(user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_transport
        FOREIGN KEY (transport_id)
        REFERENCES transports(id)
        ON DELETE CASCADE
);


CREATE TABLE TransportDepartureSlots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transport_id UUID NOT NULL,
    stop_id UUID NOT NULL,
    day_of_week VARCHAR(20) NOT NULL, -- "Monday", "Tuesday", etc
    departure_time TIME NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    stop_order INT NOT NULL, -- New column to define the order of stops in a route

    CONSTRAINT fk_transport
        FOREIGN KEY (transport_id)
        REFERENCES transport(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_stop
        FOREIGN KEY (stop_id)
        REFERENCES transportstop(id)
        ON DELETE CASCADE
);
