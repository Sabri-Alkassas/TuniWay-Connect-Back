CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(225) NOT NULL,
    password_hash VARCHAR(255),
    role VARCHAR(50),
    status VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    last_login_at TIMESTAMP
);

CREATE TABLE transport (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50),
    route_name VARCHAR(255),
    start_point VARCHAR(255),
    end_point VARCHAR(255),
    operating_zone VARCHAR(255),
    active BOOLEAN DEFAULT TRUE
);


CREATE TABLE transportstop (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    stop_name VARCHAR(255) NOT NULL,
    zone VARCHAR(100),
    active BOOLEAN DEFAULT TRUE
);

CREATE TABLE client_profiles (
    user_id UUID PRIMARY KEY,
    username VARCHAR(100) UNIQUE,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(50),
    birth_date TIMESTAMP,

    CONSTRAINT fk_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

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