UPDATE transport
SET type = CASE
    WHEN type IS NULL OR btrim(type) = '' THEN NULL
    WHEN upper(regexp_replace(btrim(type), '[^A-Za-z0-9]+', '_', 'g')) IN ('BUS') THEN 'BUS'
    WHEN upper(regexp_replace(btrim(type), '[^A-Za-z0-9]+', '_', 'g')) IN ('TRAIN', 'RAIL') THEN 'TRAIN'
    WHEN upper(regexp_replace(btrim(type), '[^A-Za-z0-9]+', '_', 'g')) IN ('METRO', 'TRAM', 'TRAMWAY', 'SUBWAY', 'LIGHT_RAIL', 'LIGHTRAIL', 'METRO_LEGER', 'TGM') THEN 'METRO'
    ELSE type
END;

ALTER TABLE transport
    DROP CONSTRAINT IF EXISTS chk_transport_type_valid;

ALTER TABLE transport
    ADD CONSTRAINT chk_transport_type_valid
    CHECK (type IS NULL OR type IN ('BUS', 'TRAIN', 'METRO')) NOT VALID;
