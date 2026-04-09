ALTER TABLE transportstop
    ADD COLUMN latitude DECIMAL(9,6),
    ADD COLUMN longitude DECIMAL(9,6);

ALTER TABLE transportstop
    ADD CONSTRAINT chk_transportstop_latitude
    CHECK (latitude IS NULL OR (latitude >= -90 AND latitude <= 90));

ALTER TABLE transportstop
    ADD CONSTRAINT chk_transportstop_longitude
    CHECK (longitude IS NULL OR (longitude >= -180 AND longitude <= 180));
