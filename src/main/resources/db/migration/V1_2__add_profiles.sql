CREATE TABLE admin_profiles (
    user_id UUID PRIMARY KEY,
    admin_code VARCHAR(100) UNIQUE,
    full_name VARCHAR(255),
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    two_factor_secret_encrypted VARCHAR(255),

    CONSTRAINT fk_admin_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE employee_profiles (
    user_id UUID PRIMARY KEY,
    employee_code VARCHAR(100) UNIQUE,
    full_name VARCHAR(255),
    phone VARCHAR(50),
    license_number VARCHAR(100),
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    two_factor_secret_encrypted VARCHAR(255),

    CONSTRAINT fk_employee_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);