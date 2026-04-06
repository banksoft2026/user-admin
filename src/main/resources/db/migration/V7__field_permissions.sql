CREATE TABLE field_permissions (
    field_perm_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id UUID NOT NULL REFERENCES roles(role_id),
    screen_id UUID NOT NULL REFERENCES screens(screen_id),
    field_id UUID NOT NULL REFERENCES screen_fields(field_id),
    visibility VARCHAR(20) NOT NULL DEFAULT 'VISIBLE'
        CHECK (visibility IN ('VISIBLE','MASKED','HIDDEN')),
    editability VARCHAR(20) NOT NULL DEFAULT 'READ_ONLY'
        CHECK (editability IN ('EDITABLE','MAKER_ONLY','READ_ONLY','HIDDEN')),
    mask_pattern VARCHAR(50),
    granted_by UUID NOT NULL,
    granted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE(role_id, screen_id, field_id)
);

-- VIEWER: mask sensitive fields on account detail
INSERT INTO field_permissions (role_id, screen_id, field_id, visibility, editability, mask_pattern, granted_by)
SELECT r.role_id, sf.screen_id, sf.field_id, 'MASKED', 'READ_ONLY', '****{last4}', r.role_id
FROM roles r
JOIN screens s ON s.screen_code = 'SCR-ACC-002'
JOIN screen_fields sf ON sf.screen_id = s.screen_id AND sf.field_code IN ('balance_available','balance_ledger','account_number')
WHERE r.role_code = 'VIEWER';

-- MAKER: editable fields on account detail go to maker-checker queue
INSERT INTO field_permissions (role_id, screen_id, field_id, visibility, editability, granted_by)
SELECT r.role_id, sf.screen_id, sf.field_id, 'VISIBLE', 'MAKER_ONLY', r.role_id
FROM roles r
JOIN screens s ON s.screen_code = 'SCR-ACC-002'
JOIN screen_fields sf ON sf.screen_id = s.screen_id AND sf.field_code IN ('overdraft_limit','interest_rate','account_status')
WHERE r.role_code = 'MAKER';

-- VIEWER: hide internal notes on account detail
INSERT INTO field_permissions (role_id, screen_id, field_id, visibility, editability, granted_by)
SELECT r.role_id, sf.screen_id, sf.field_id, 'HIDDEN', 'HIDDEN', r.role_id
FROM roles r
JOIN screens s ON s.screen_code = 'SCR-ACC-002'
JOIN screen_fields sf ON sf.screen_id = s.screen_id AND sf.field_code = 'internal_notes'
WHERE r.role_code = 'VIEWER';
