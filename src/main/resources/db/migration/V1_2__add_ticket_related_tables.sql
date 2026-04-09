CREATE TABLE ticket_products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    valid_duration_minutes INT NOT NULL,
    active BOOLEAN DEFAULT TRUE,

    CONSTRAINT price_positive CHECK (price >= 0),
    CONSTRAINT duration_positive CHECK (valid_duration_minutes > 0)
);

CREATE TABLE ticket_purchases (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    product_id UUID NOT NULL,
    -- Deleted paidAmount cuz its useless
    status VARCHAR(50) NOT NULL, -- "active", "used", "expired"
    valid_until TIMESTAMP NOT NULL,
    purchase_time TIMESTAMP NOT NULL,

    CONSTRAINT fk_ticket_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_ticket_product
        FOREIGN KEY (product_id)
        REFERENCES ticket_products(id)
        ON DELETE CASCADE
);

CREATE TABLE payment_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_purchase_id UUID NOT NULL,
    provider VARCHAR(100) NOT NULL,
    provider_reference VARCHAR(255) NOT NULL, -- changed provider_ref to provider_reference for clarity
    payment_method VARCHAR(50) NOT NULL,
    processed_at TIMESTAMP NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL, -- "pending", "completed", "failed"

    CONSTRAINT fk_purchase
        FOREIGN KEY (ticket_purchase_id)
        REFERENCES ticket_purchases(id)
        ON DELETE CASCADE,

    CONSTRAINT processed_at_not_future CHECK (processed_at <= NOW()),
    CONSTRAINT amount_positive CHECK (amount >= 0)
);
