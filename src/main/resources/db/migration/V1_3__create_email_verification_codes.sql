CREATE TABLE email_verification_codes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(225) NOT NULL,
    code VARCHAR(100) NOT NULL,
    expires_at TIMESTAMP,
    consumed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
