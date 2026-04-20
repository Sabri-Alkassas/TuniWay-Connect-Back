-- Seed starter friend accounts for local/dev startup.
-- Note: current authentication compares password_hash as provided by the client.

INSERT INTO users (email, password_hash, role, status, created_at, updated_at)
SELECT 'friend.client@tuniway.test', 'Client123!', 'CLIENT', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'friend.client@tuniway.test'
);

INSERT INTO users (email, password_hash, role, status, created_at, updated_at)
SELECT 'friend.employee@tuniway.test', 'Employee123!', 'EMPLOYEE', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'friend.employee@tuniway.test'
);

INSERT INTO users (email, password_hash, role, status, created_at, updated_at)
SELECT 'friend.admin@tuniway.test', 'Admin123!', 'ADMIN', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'friend.admin@tuniway.test'
);

INSERT INTO client_profiles (user_id, username, first_name, last_name, phone, birth_date)
SELECT u.id, 'friend.client', 'Friend', 'Client', NULL, NULL
FROM users u
WHERE u.email = 'friend.client@tuniway.test'
  AND NOT EXISTS (
      SELECT 1
      FROM client_profiles cp
      WHERE cp.user_id = u.id
  );

INSERT INTO employee_profiles (
    user_id,
    employee_code,
    full_name,
    phone,
    license_number,
    two_factor_enabled,
    two_factor_secret_encrypted
)
SELECT
    u.id,
    'FRIEND-EMP-001',
    'Friend Employee',
    NULL,
    'FRIEND-LIC-001',
    TRUE,
    'JBSWY3DPEHPK3PXP'
FROM users u
WHERE u.email = 'friend.employee@tuniway.test'
  AND NOT EXISTS (
      SELECT 1
      FROM employee_profiles ep
      WHERE ep.user_id = u.id
  );

INSERT INTO admin_profiles (
    user_id,
    admin_code,
    full_name,
    two_factor_enabled,
    two_factor_secret_encrypted
)
SELECT
    u.id,
    'FRIEND-ADM-001',
    'Friend Admin',
    TRUE,
    'JBSWY3DPEHPK3PXP'
FROM users u
WHERE u.email = 'friend.admin@tuniway.test'
  AND NOT EXISTS (
      SELECT 1
      FROM admin_profiles ap
      WHERE ap.user_id = u.id
  );
