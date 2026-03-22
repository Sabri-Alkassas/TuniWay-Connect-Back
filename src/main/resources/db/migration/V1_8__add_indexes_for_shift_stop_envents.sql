CREATE INDEX idx_shift_stop_events_workshift_id_stop_order
    ON shift_stop_events (workshift_id, stop_order);

CREATE INDEX idx_shift_stop_events_workshift_id_stop_id
    ON shift_stop_events (workshift_id, stop_id);

CREATE INDEX idx_shift_stop_events_workshift_id_status
    ON shift_stop_events (workshift_id, status);