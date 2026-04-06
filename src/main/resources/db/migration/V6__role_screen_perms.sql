CREATE TABLE role_screen_perms (
    perm_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id UUID NOT NULL REFERENCES roles(role_id),
    screen_id UUID NOT NULL REFERENCES screens(screen_id),
    can_access BOOLEAN NOT NULL DEFAULT FALSE,
    access_level VARCHAR(20) NOT NULL DEFAULT 'READ'
        CHECK (access_level IN ('FULL','MAKER','READ','NONE')),
    granted_by UUID NOT NULL,
    granted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE(role_id, screen_id)
);

-- Grant ADMIN role full access to all screens
INSERT INTO role_screen_perms (role_id, screen_id, can_access, access_level, granted_by)
SELECT r.role_id, s.screen_id, TRUE, 'FULL', r.role_id
FROM roles r, screens s
WHERE r.role_code = 'ADMIN';

-- Grant VIEWER role read access to non-admin screens
INSERT INTO role_screen_perms (role_id, screen_id, can_access, access_level, granted_by)
SELECT r.role_id, s.screen_id, TRUE, 'READ', r.role_id
FROM roles r, screens s
WHERE r.role_code = 'VIEWER'
  AND s.screen_code NOT IN ('SCR-USR-001','SCR-ROL-001','SCR-PRM-001','SCR-MCQ-001');

-- Grant MAKER role maker access to operational screens
INSERT INTO role_screen_perms (role_id, screen_id, can_access, access_level, granted_by)
SELECT r.role_id, s.screen_id, TRUE, 'MAKER', r.role_id
FROM roles r, screens s
WHERE r.role_code = 'MAKER'
  AND s.screen_code IN ('SCR-DASH-001','SCR-CUST-001','SCR-CUST-002','SCR-CUST-003',
                        'SCR-ACC-001','SCR-ACC-002','SCR-ACC-003',
                        'SCR-TXN-001','SCR-TXN-002',
                        'SCR-GL-001','SCR-GL-002','SCR-GL-003','SCR-GL-004',
                        'SCR-MAINT-001','SCR-MAINT-002','SCR-MAINT-003','SCR-MAINT-004');

-- Grant CHECKER role full access to operational + approval screens
INSERT INTO role_screen_perms (role_id, screen_id, can_access, access_level, granted_by)
SELECT r.role_id, s.screen_id, TRUE, 'FULL', r.role_id
FROM roles r, screens s
WHERE r.role_code = 'CHECKER'
  AND s.screen_code IN ('SCR-DASH-001','SCR-CUST-001','SCR-CUST-002',
                        'SCR-ACC-001','SCR-ACC-002',
                        'SCR-TXN-001','SCR-GL-001','SCR-GL-002','SCR-GL-003','SCR-GL-004',
                        'SCR-MCQ-001','SCR-AUDIT-001','SCR-RPT-001','SCR-RPT-002');
