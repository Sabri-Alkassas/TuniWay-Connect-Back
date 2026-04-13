ALTER TABLE ticket_products
    ADD COLUMN IF NOT EXISTS product_code VARCHAR(100),
    ADD COLUMN IF NOT EXISTS fare_class VARCHAR(50) NOT NULL DEFAULT 'STANDARD',
    ADD COLUMN IF NOT EXISTS allowed_transport_code VARCHAR(255);

ALTER TABLE ticket_purchases
    ADD COLUMN IF NOT EXISTS from_stop_order INT,
    ADD COLUMN IF NOT EXISTS to_stop_order INT;

CREATE UNIQUE INDEX IF NOT EXISTS uk_ticket_products_product_code
    ON ticket_products (product_code)
    WHERE product_code IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_ticket_purchases_active_segments
    ON ticket_purchases (transport_id, status, valid_until, from_stop_order, to_stop_order);

CREATE TABLE IF NOT EXISTS transport_fare_profiles (
    transport_code VARCHAR(255) PRIMARY KEY,
    pricing_model VARCHAR(50) NOT NULL,
    max_sections INT NOT NULL,
    section_boundary_stop_order INT,
    notes TEXT,
    CONSTRAINT chk_transport_fare_profile_pricing_model
        CHECK (pricing_model IN ('INTERPOLATED_SECTION', 'TGM_SECTION_BOUNDARY')),
    CONSTRAINT chk_transport_fare_profile_max_sections
        CHECK (max_sections > 0),
    CONSTRAINT chk_transport_fare_profile_boundary
        CHECK (section_boundary_stop_order IS NULL OR section_boundary_stop_order > 0)
);

CREATE TABLE IF NOT EXISTS ticket_fare_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transport_code VARCHAR(255),
    transport_type VARCHAR(50) NOT NULL,
    fare_class VARCHAR(50) NOT NULL,
    min_sections INT NOT NULL,
    max_sections INT NOT NULL,
    price DECIMAL(10, 3) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'TND',
    notes TEXT,
    CONSTRAINT chk_ticket_fare_rule_sections
        CHECK (min_sections > 0 AND max_sections >= min_sections),
    CONSTRAINT chk_ticket_fare_rule_price
        CHECK (price >= 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_ticket_fare_rules_identity
    ON ticket_fare_rules (COALESCE(transport_code, ''), transport_type, fare_class, min_sections, max_sections);

UPDATE ticket_purchases tp
SET from_stop_order = from_route.stop_order,
    to_stop_order = to_route.stop_order
FROM transport_route_stops from_route
JOIN transport_route_stops to_route
    ON to_route.transport_id = from_route.transport_id
WHERE tp.transport_id = from_route.transport_id
  AND tp.from_stop_id = from_route.stop_id
  AND tp.to_stop_id = to_route.stop_id
  AND (tp.from_stop_order IS NULL OR tp.to_stop_order IS NULL);

INSERT INTO ticket_products (id, product_code, name, description, price, valid_duration_minutes, active, fare_class, allowed_transport_code)
VALUES
    (
        '10000000-0000-0000-0000-000000000001',
        'STANDARD_JOURNEY',
        'Standard Journey Ticket',
        'Standard second-class journey ticket priced from the official TRANSTU fare structure.',
        0.000,
        120,
        TRUE,
        'STANDARD',
        NULL
    ),
    (
        '10000000-0000-0000-0000-000000000003',
        'TGM_FIRST_CLASS_JOURNEY',
        'TGM First Class Journey',
        'First-class TGM journey ticket priced from the official TRANSTU fare structure.',
        0.000,
        120,
        TRUE,
        'FIRST_CLASS',
        'TGM'
    )
ON CONFLICT (id) DO UPDATE
SET product_code = EXCLUDED.product_code,
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    price = EXCLUDED.price,
    valid_duration_minutes = EXCLUDED.valid_duration_minutes,
    active = EXCLUDED.active,
    fare_class = EXCLUDED.fare_class,
    allowed_transport_code = EXCLUDED.allowed_transport_code;

UPDATE ticket_products
SET product_code = 'STANDARD_JOURNEY_BACKUP',
    fare_class = 'STANDARD',
    allowed_transport_code = NULL,
    active = FALSE
WHERE id = '10000000-0000-0000-0000-000000000002';

UPDATE ticket_products
SET product_code = 'LEGACY_FRIEND_JOURNEY_60M',
    fare_class = 'STANDARD',
    allowed_transport_code = NULL,
    active = FALSE
WHERE id = '20000000-0000-0000-0000-000000010001';

UPDATE ticket_products
SET product_code = 'LEGACY_FRIEND_JOURNEY_120M',
    fare_class = 'STANDARD',
    allowed_transport_code = NULL,
    active = FALSE
WHERE id = '20000000-0000-0000-0000-000000010002';

INSERT INTO transport_fare_profiles (transport_code, pricing_model, max_sections, section_boundary_stop_order, notes)
VALUES
    ('METRO-L1', 'INTERPOLATED_SECTION', 3, NULL, 'Workbook band 1-3 sections.'),
    ('METRO-L2', 'INTERPOLATED_SECTION', 3, NULL, 'Workbook band 1-3 sections.'),
    ('METRO-L3', 'INTERPOLATED_SECTION', 3, NULL, 'Workbook band 1-3 sections.'),
    ('METRO-L4', 'INTERPOLATED_SECTION', 6, NULL, 'Workbook band 4-6 sections.'),
    ('METRO-L5', 'INTERPOLATED_SECTION', 4, NULL, 'Workbook band 3-4 sections.'),
    ('METRO-L6', 'INTERPOLATED_SECTION', 5, NULL, 'Workbook band 4-5 sections.'),
    ('TGM', 'TGM_SECTION_BOUNDARY', 2, 6, 'Workbook section boundary at La Goulette Casino.'),
    ('BUS-10', 'INTERPOLATED_SECTION', 6, NULL, 'Workbook band 4-6 sections.'),
    ('BUS-19', 'INTERPOLATED_SECTION', 4, NULL, 'Workbook band 3-4 sections.'),
    ('BUS-19A', 'INTERPOLATED_SECTION', 4, NULL, 'Workbook band 3-4 sections.'),
    ('BUS-19D', 'INTERPOLATED_SECTION', 6, NULL, 'Workbook band 4-6 sections.'),
    ('BUS-19E', 'INTERPOLATED_SECTION', 4, NULL, 'Workbook band 3-4 sections.'),
    ('BUS-48', 'INTERPOLATED_SECTION', 7, NULL, 'Workbook band 6-7 sections.'),
    ('BUS-53', 'INTERPOLATED_SECTION', 6, NULL, 'Workbook band 4-6 sections.'),
    ('BUS-27', 'INTERPOLATED_SECTION', 4, NULL, 'Workbook band 3-4 sections.'),
    ('BUS-27E', 'INTERPOLATED_SECTION', 4, NULL, 'Workbook band 3-4 sections.'),
    ('BUS-44', 'INTERPOLATED_SECTION', 2, NULL, 'Workbook band 1-2 sections.'),
    ('BUS-44A', 'INTERPOLATED_SECTION', 2, NULL, 'Workbook band 1-2 sections.'),
    ('BUS-44E', 'INTERPOLATED_SECTION', 5, NULL, 'Workbook band 4-5 sections.'),
    ('BUS-62A', 'INTERPOLATED_SECTION', 4, NULL, 'Workbook band 3-4 sections.'),
    ('BUS-77', 'INTERPOLATED_SECTION', 3, NULL, 'Workbook band 2-3 sections.'),
    ('BUS-527', 'INTERPOLATED_SECTION', 5, NULL, 'Workbook band 4-5 sections.'),
    ('BUS-527B', 'INTERPOLATED_SECTION', 5, NULL, 'Workbook band 4-5 sections.'),
    ('BUS-5', 'INTERPOLATED_SECTION', 5, NULL, 'Workbook band 4-5 sections.'),
    ('BUS-5A', 'INTERPOLATED_SECTION', 6, NULL, 'Workbook band 5-6 sections.'),
    ('BUS-23', 'INTERPOLATED_SECTION', 5, NULL, 'Workbook band 3-5 sections.'),
    ('BUS-29', 'INTERPOLATED_SECTION', 4, NULL, 'Workbook band 3-4 sections.'),
    ('BUS-40', 'INTERPOLATED_SECTION', 5, NULL, 'Workbook band 4-5 sections.'),
    ('BUS-3', 'INTERPOLATED_SECTION', 3, NULL, 'Workbook band 2-3 sections.'),
    ('BUS-22', 'INTERPOLATED_SECTION', 5, NULL, 'Workbook band 4-5 sections.'),
    ('BUS-47', 'INTERPOLATED_SECTION', 10, NULL, 'Workbook band 7-10 sections.'),
    ('BUS-50', 'INTERPOLATED_SECTION', 3, NULL, 'Workbook band 2-3 sections.'),
    ('BUS-35', 'INTERPOLATED_SECTION', 3, NULL, 'Workbook band 2-3 sections.'),
    ('BUS-9', 'INTERPOLATED_SECTION', 2, NULL, 'Workbook band 1-2 sections.')
ON CONFLICT (transport_code) DO UPDATE
SET pricing_model = EXCLUDED.pricing_model,
    max_sections = EXCLUDED.max_sections,
    section_boundary_stop_order = EXCLUDED.section_boundary_stop_order,
    notes = EXCLUDED.notes;

INSERT INTO ticket_fare_rules (id, transport_code, transport_type, fare_class, min_sections, max_sections, price, currency, notes)
VALUES
    ('30000000-0000-0000-0000-000000000001', NULL, 'BUS', 'STANDARD', 1, 3, 0.500, 'TND', 'Workbook weekday urban bus fare band.'),
    ('30000000-0000-0000-0000-000000000002', NULL, 'BUS', 'STANDARD', 4, 6, 1.000, 'TND', 'Workbook weekday urban bus fare band.'),
    ('30000000-0000-0000-0000-000000000003', NULL, 'BUS', 'STANDARD', 7, 10, 1.500, 'TND', 'Workbook weekday urban bus fare band.'),
    ('30000000-0000-0000-0000-000000000004', NULL, 'METRO', 'STANDARD', 1, 3, 0.500, 'TND', 'Workbook metro fare band.'),
    ('30000000-0000-0000-0000-000000000005', NULL, 'METRO', 'STANDARD', 4, 6, 1.000, 'TND', 'Workbook metro fare band.'),
    ('30000000-0000-0000-0000-000000000006', 'TGM', 'METRO', 'STANDARD', 1, 1, 0.500, 'TND', 'Workbook TGM second class fare.'),
    ('30000000-0000-0000-0000-000000000007', 'TGM', 'METRO', 'STANDARD', 2, 2, 0.800, 'TND', 'Workbook TGM second class fare.'),
    ('30000000-0000-0000-0000-000000000008', 'TGM', 'METRO', 'FIRST_CLASS', 1, 1, 0.900, 'TND', 'Workbook TGM first class fare.'),
    ('30000000-0000-0000-0000-000000000009', 'TGM', 'METRO', 'FIRST_CLASS', 2, 2, 1.250, 'TND', 'Workbook TGM first class fare.')
ON CONFLICT (id) DO UPDATE
SET transport_code = EXCLUDED.transport_code,
    transport_type = EXCLUDED.transport_type,
    fare_class = EXCLUDED.fare_class,
    min_sections = EXCLUDED.min_sections,
    max_sections = EXCLUDED.max_sections,
    price = EXCLUDED.price,
    currency = EXCLUDED.currency,
    notes = EXCLUDED.notes;

INSERT INTO transportdepartureslots (id, transport_id, stop_id, day_of_week, departure_time, active, stop_order)
SELECT seed.id::uuid, t.id, s.id, seed.day_of_week, seed.departure_time::time, seed.active, seed.stop_order
FROM (
    VALUES
        ('dddd0108-0001-4000-8000-000000000001', 'BUS-27E', 'Ariana Est', 'Monday', '05:50:00', TRUE, 1),
        ('dddd0108-0001-4000-8000-000000000002', 'BUS-27E', 'Ariana Est', 'Monday', '07:10:00', TRUE, 1),
        ('dddd0108-0001-4000-8000-000000000003', 'BUS-27E', 'Ariana Est', 'Monday', '12:10:00', TRUE, 1),
        ('dddd0108-0001-4000-8000-000000000004', 'BUS-27E', 'Ariana Est', 'Monday', '17:10:00', TRUE, 1),
        ('dddd0108-0001-4000-8000-000000000005', 'BUS-27E', 'Ariana Est', 'Monday', '21:10:00', TRUE, 1),
        ('dddd0108-0001-4000-8000-000000000006', 'BUS-27E', 'Ariana Est', 'Sunday', '07:40:00', TRUE, 1),
        ('dddd0108-0001-4000-8000-000000000007', 'BUS-27E', 'Ariana Est', 'Sunday', '20:10:00', TRUE, 1),
        ('dddd0109-0001-4000-8000-000000000001', 'BUS-44', '10 Décembre 1948', 'Monday', '06:00:00', TRUE, 1),
        ('dddd0109-0001-4000-8000-000000000002', 'BUS-44', '10 Décembre 1948', 'Monday', '08:00:00', TRUE, 1),
        ('dddd0109-0001-4000-8000-000000000003', 'BUS-44', '10 Décembre 1948', 'Monday', '12:00:00', TRUE, 1),
        ('dddd0109-0001-4000-8000-000000000004', 'BUS-44', '10 Décembre 1948', 'Monday', '17:00:00', TRUE, 1),
        ('dddd0109-0001-4000-8000-000000000005', 'BUS-44', '10 Décembre 1948', 'Monday', '20:00:00', TRUE, 1),
        ('dddd0109-0001-4000-8000-000000000006', 'BUS-44', '10 Décembre 1948', 'Sunday', '08:00:00', TRUE, 1),
        ('dddd0109-0001-4000-8000-000000000007', 'BUS-44', '10 Décembre 1948', 'Sunday', '19:00:00', TRUE, 1),
        ('dddd0110-0001-4000-8000-000000000001', 'BUS-44A', '10 Décembre 1948', 'Monday', '06:15:00', TRUE, 1),
        ('dddd0110-0001-4000-8000-000000000002', 'BUS-44A', '10 Décembre 1948', 'Monday', '08:15:00', TRUE, 1),
        ('dddd0110-0001-4000-8000-000000000003', 'BUS-44A', '10 Décembre 1948', 'Monday', '12:15:00', TRUE, 1),
        ('dddd0110-0001-4000-8000-000000000004', 'BUS-44A', '10 Décembre 1948', 'Monday', '17:15:00', TRUE, 1),
        ('dddd0110-0001-4000-8000-000000000005', 'BUS-44A', '10 Décembre 1948', 'Monday', '20:15:00', TRUE, 1),
        ('dddd0110-0001-4000-8000-000000000006', 'BUS-44A', '10 Décembre 1948', 'Sunday', '08:15:00', TRUE, 1),
        ('dddd0110-0001-4000-8000-000000000007', 'BUS-44A', '10 Décembre 1948', 'Sunday', '19:15:00', TRUE, 1)
) AS seed(id, transport_code, stop_name, day_of_week, departure_time, active, stop_order)
JOIN transport t
    ON t.code = seed.transport_code
JOIN transportstop s
    ON s.stop_name = seed.stop_name
ON CONFLICT (id) DO NOTHING;
