ALTER TABLE transport
    ADD COLUMN IF NOT EXISTS zone VARCHAR(255);

UPDATE transport
SET zone = COALESCE(
    NULLIF(btrim(zone), ''),
    NULLIF(btrim(operating_zone), ''),
    'N/A'
);

ALTER TABLE transport
    ALTER COLUMN zone SET NOT NULL;
