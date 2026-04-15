INSERT INTO transport (id, code, name, type, route_name, start_point, end_point, operating_zone, zone, active, capacity)
SELECT *
FROM (
    VALUES
        ('51000000-0000-0000-0000-000000000001'::uuid, 'BUS-L5', 'Ligne 5 - Lac vers Bardo', 'BUS', 'Lac 1 -> Bardo', 'Lac 1', 'Bardo', 'Grand Tunis', 'A', TRUE, 90),
        ('51000000-0000-0000-0000-000000000002'::uuid, 'METRO-M2', 'Metro 2 - Ariana', 'METRO', 'Centre-ville -> Ariana', 'Place de la Republique', 'Ariana', 'Grand Tunis', 'B', TRUE, 220),
        ('51000000-0000-0000-0000-000000000003'::uuid, 'TRAIN-T1', 'Train Banlieue - Tunis Marine vers La Goulette', 'TRAIN', 'Tunis Marine -> La Goulette', 'Tunis Marine', 'La Goulette', 'Grand Tunis', 'C', TRUE, 260)
) AS seed(id, code, name, type, route_name, start_point, end_point, operating_zone, zone, active, capacity)
WHERE NOT EXISTS (
    SELECT 1
    FROM transport existing
    WHERE existing.code = seed.code
);

INSERT INTO transportstop (id, stop_name, zone, active, latitude, longitude)
SELECT *
FROM (
    VALUES
        ('52000000-0000-0000-0000-000000000001'::uuid, 'Lac 1', 'A', TRUE, 36.842100, 10.272700),
        ('52000000-0000-0000-0000-000000000002'::uuid, 'Place Pasteur', 'A', TRUE, 36.806500, 10.180900),
        ('52000000-0000-0000-0000-000000000003'::uuid, 'Bardo', 'A', TRUE, 36.809400, 10.141100),
        ('52000000-0000-0000-0000-000000000004'::uuid, 'Place de la Republique', 'B', TRUE, 36.806900, 10.181800),
        ('52000000-0000-0000-0000-000000000005'::uuid, '10 Decembre 1948', 'B', TRUE, 36.825500, 10.164100),
        ('52000000-0000-0000-0000-000000000006'::uuid, 'Ariana', 'B', TRUE, 36.862500, 10.193800),
        ('52000000-0000-0000-0000-000000000007'::uuid, 'Tunis Marine', 'C', TRUE, 36.799700, 10.194100),
        ('52000000-0000-0000-0000-000000000008'::uuid, 'La Goulette Casino', 'C', TRUE, 36.818800, 10.305300),
        ('52000000-0000-0000-0000-000000000009'::uuid, 'La Goulette', 'C', TRUE, 36.824700, 10.303300)
) AS seed(id, stop_name, zone, active, latitude, longitude)
WHERE NOT EXISTS (
    SELECT 1
    FROM transportstop existing
    WHERE existing.id = seed.id
);

INSERT INTO transport_route_stops (id, transport_id, stop_id, stop_order, active)
SELECT *
FROM (
    VALUES
        ('53000000-0000-0000-0000-000000000001'::uuid, '51000000-0000-0000-0000-000000000001'::uuid, '52000000-0000-0000-0000-000000000001'::uuid, 1, TRUE),
        ('53000000-0000-0000-0000-000000000002'::uuid, '51000000-0000-0000-0000-000000000001'::uuid, '52000000-0000-0000-0000-000000000002'::uuid, 2, TRUE),
        ('53000000-0000-0000-0000-000000000003'::uuid, '51000000-0000-0000-0000-000000000001'::uuid, '52000000-0000-0000-0000-000000000003'::uuid, 3, TRUE),
        ('53000000-0000-0000-0000-000000000004'::uuid, '51000000-0000-0000-0000-000000000002'::uuid, '52000000-0000-0000-0000-000000000004'::uuid, 1, TRUE),
        ('53000000-0000-0000-0000-000000000005'::uuid, '51000000-0000-0000-0000-000000000002'::uuid, '52000000-0000-0000-0000-000000000005'::uuid, 2, TRUE),
        ('53000000-0000-0000-0000-000000000006'::uuid, '51000000-0000-0000-0000-000000000002'::uuid, '52000000-0000-0000-0000-000000000006'::uuid, 3, TRUE),
        ('53000000-0000-0000-0000-000000000007'::uuid, '51000000-0000-0000-0000-000000000003'::uuid, '52000000-0000-0000-0000-000000000007'::uuid, 1, TRUE),
        ('53000000-0000-0000-0000-000000000008'::uuid, '51000000-0000-0000-0000-000000000003'::uuid, '52000000-0000-0000-0000-000000000008'::uuid, 2, TRUE),
        ('53000000-0000-0000-0000-000000000009'::uuid, '51000000-0000-0000-0000-000000000003'::uuid, '52000000-0000-0000-0000-000000000009'::uuid, 3, TRUE)
) AS seed(id, transport_id, stop_id, stop_order, active)
WHERE NOT EXISTS (
    SELECT 1
    FROM transport_route_stops existing
    WHERE existing.id = seed.id
);

INSERT INTO transportdepartureslots (id, transport_id, stop_id, day_of_week, departure_time, active, stop_order)
SELECT *
FROM (
    VALUES
        ('54000000-0000-0000-0000-000000000001'::uuid, '51000000-0000-0000-0000-000000000001'::uuid, '52000000-0000-0000-0000-000000000001'::uuid, 'Monday', '06:00'::time, TRUE, 1),
        ('54000000-0000-0000-0000-000000000002'::uuid, '51000000-0000-0000-0000-000000000001'::uuid, '52000000-0000-0000-0000-000000000002'::uuid, 'Monday', '06:15'::time, TRUE, 2),
        ('54000000-0000-0000-0000-000000000003'::uuid, '51000000-0000-0000-0000-000000000001'::uuid, '52000000-0000-0000-0000-000000000003'::uuid, 'Monday', '06:30'::time, TRUE, 3),
        ('54000000-0000-0000-0000-000000000004'::uuid, '51000000-0000-0000-0000-000000000002'::uuid, '52000000-0000-0000-0000-000000000004'::uuid, 'Monday', '07:00'::time, TRUE, 1),
        ('54000000-0000-0000-0000-000000000005'::uuid, '51000000-0000-0000-0000-000000000002'::uuid, '52000000-0000-0000-0000-000000000005'::uuid, 'Monday', '07:18'::time, TRUE, 2),
        ('54000000-0000-0000-0000-000000000006'::uuid, '51000000-0000-0000-0000-000000000002'::uuid, '52000000-0000-0000-0000-000000000006'::uuid, 'Monday', '07:34'::time, TRUE, 3),
        ('54000000-0000-0000-0000-000000000007'::uuid, '51000000-0000-0000-0000-000000000003'::uuid, '52000000-0000-0000-0000-000000000007'::uuid, 'Monday', '08:00'::time, TRUE, 1),
        ('54000000-0000-0000-0000-000000000008'::uuid, '51000000-0000-0000-0000-000000000003'::uuid, '52000000-0000-0000-0000-000000000008'::uuid, 'Monday', '08:16'::time, TRUE, 2),
        ('54000000-0000-0000-0000-000000000009'::uuid, '51000000-0000-0000-0000-000000000003'::uuid, '52000000-0000-0000-0000-000000000009'::uuid, 'Monday', '08:28'::time, TRUE, 3)
) AS seed(id, transport_id, stop_id, day_of_week, departure_time, active, stop_order)
WHERE NOT EXISTS (
    SELECT 1
    FROM transportdepartureslots existing
    WHERE existing.id = seed.id
);
