CREATE TABLE roles (
    role_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_code VARCHAR(30) NOT NULL UNIQUE,
    role_name VARCHAR(100) NOT NULL,
    role_level VARCHAR(20) NOT NULL CHECK (role_level IN ('ADMIN','CHECKER','MAKER','VIEWER')),
    description TEXT,
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    max_transaction_amt DECIMAL(18,2),
    requires_mfa BOOLEAN NOT NULL DEFAULT FALSE,
    session_timeout_min INTEGER NOT NULL DEFAULT 480,
    ip_allowlist JSONB,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO roles (role_code, role_name, role_level, is_system, requires_mfa, session_timeout_min, description) VALUES
    ('ADMIN',    'System Administrator', 'ADMIN',   TRUE, TRUE,  60,  'Full system control. Manages users, roles, permissions.'),
    ('CHECKER',  'Operations Checker',   'CHECKER', TRUE, TRUE,  480, 'Reviews and approves maker submissions.'),
    ('MAKER',    'Operations Officer',   'MAKER',   TRUE, FALSE, 480, 'Initiates create/update/delete operations.'),
    ('VIEWER',   'Read-Only Auditor',    'VIEWER',  TRUE, FALSE, 480, 'Read-only access to permitted screens.');
