CREATE TABLE screen_fields (
    field_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    screen_id UUID NOT NULL REFERENCES screens(screen_id),
    field_code VARCHAR(50) NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    field_type VARCHAR(20) NOT NULL CHECK (field_type IN ('TEXT','NUMBER','CURRENCY','DATE','DATETIME','BOOLEAN','ENUM','FILE','REFERENCE')),
    is_sensitive BOOLEAN NOT NULL DEFAULT FALSE,
    default_visible BOOLEAN NOT NULL DEFAULT TRUE,
    default_editable BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    UNIQUE(screen_id, field_code)
);

-- Account Detail screen fields
INSERT INTO screen_fields (screen_id, field_code, field_name, field_type, is_sensitive, default_visible, default_editable, sort_order)
SELECT s.screen_id, f.field_code, f.field_name, f.field_type::VARCHAR, f.is_sensitive, f.default_visible, f.default_editable, f.sort_order
FROM screens s,
(VALUES
    ('account_number',     'Account Number',      'TEXT',     TRUE,  TRUE,  FALSE, 1),
    ('account_holder_name','Account Holder Name', 'TEXT',     FALSE, TRUE,  FALSE, 2),
    ('balance_available',  'Available Balance',   'CURRENCY', TRUE,  TRUE,  FALSE, 3),
    ('balance_ledger',     'Ledger Balance',      'CURRENCY', TRUE,  TRUE,  FALSE, 4),
    ('overdraft_limit',    'Overdraft Limit',     'CURRENCY', FALSE, TRUE,  FALSE, 5),
    ('account_status',     'Account Status',      'ENUM',     FALSE, TRUE,  FALSE, 6),
    ('interest_rate',      'Interest Rate',       'NUMBER',   FALSE, TRUE,  FALSE, 7),
    ('risk_category',      'Risk Category',       'ENUM',     TRUE,  TRUE,  FALSE, 8),
    ('internal_notes',     'Internal Notes',      'TEXT',     TRUE,  FALSE, FALSE, 9),
    ('close_reason',       'Close Reason',        'TEXT',     FALSE, FALSE, FALSE, 10)
) AS f(field_code, field_name, field_type, is_sensitive, default_visible, default_editable, sort_order)
WHERE s.screen_code = 'SCR-ACC-002';

-- User Management screen fields
INSERT INTO screen_fields (screen_id, field_code, field_name, field_type, is_sensitive, default_visible, default_editable, sort_order)
SELECT s.screen_id, f.field_code, f.field_name, f.field_type::VARCHAR, f.is_sensitive, f.default_visible, f.default_editable, f.sort_order
FROM screens s,
(VALUES
    ('username',      'Username',       'TEXT',  FALSE, TRUE,  FALSE, 1),
    ('email',         'Email',          'TEXT',  TRUE,  TRUE,  FALSE, 2),
    ('full_name',     'Full Name',      'TEXT',  FALSE, TRUE,  FALSE, 3),
    ('status',        'Status',         'ENUM',  FALSE, TRUE,  FALSE, 4),
    ('branch_code',   'Branch Code',    'TEXT',  FALSE, TRUE,  FALSE, 5),
    ('department',    'Department',     'TEXT',  FALSE, TRUE,  FALSE, 6),
    ('last_login_at', 'Last Login',     'DATETIME', FALSE, TRUE, FALSE, 7),
    ('mfa_enabled',   'MFA Enabled',   'BOOLEAN', FALSE, TRUE, FALSE, 8)
) AS f(field_code, field_name, field_type, is_sensitive, default_visible, default_editable, sort_order)
WHERE s.screen_code = 'SCR-USR-001';
