DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'refresh_tokens'
          AND column_name = 'rovoked'
    ) THEN
        ALTER TABLE refresh_tokens RENAME COLUMN rovoked TO revoked;
    END IF;
END
$$;
