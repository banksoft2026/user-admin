CREATE TABLE modules (
    module_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    module_code VARCHAR(30) NOT NULL UNIQUE,
    module_name VARCHAR(100) NOT NULL,
    parent_module_id UUID REFERENCES modules(module_id),
    route_prefix VARCHAR(100),
    icon_name VARCHAR(50),
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE screens (
    screen_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    screen_code VARCHAR(30) NOT NULL UNIQUE,
    screen_name VARCHAR(100) NOT NULL,
    module_id UUID NOT NULL REFERENCES modules(module_id),
    route_path VARCHAR(200) NOT NULL UNIQUE,
    screen_type VARCHAR(20) NOT NULL CHECK (screen_type IN ('LIST','DETAIL','FORM','REPORT','DASHBOARD','ADMIN')),
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO modules (module_code, module_name, icon_name, sort_order) VALUES
    ('MOD-DASH',  'Dashboard',             'LayoutDashboard', 1),
    ('MOD-CUST',  'Customer Management',   'Users',           2),
    ('MOD-ACC',   'Account Management',    'Wallet',          3),
    ('MOD-TXN',   'Transaction Management','ArrowLeftRight',  4),
    ('MOD-MAINT', 'CBS Maintenance',       'Settings',        5),
    ('MOD-GL',    'General Ledger',        'BookOpen',        6),
    ('MOD-RPT',   'Reports',               'BarChart3',       7),
    ('MOD-AUDIT', 'Audit Log',             'FileSearch',      8),
    ('MOD-ADMIN', 'Administration',        'ShieldCheck',     9);

INSERT INTO screens (screen_code, screen_name, module_id, route_path, screen_type, sort_order)
SELECT sc.screen_code, sc.screen_name, m.module_id, sc.route_path, sc.screen_type::VARCHAR, sc.sort_order
FROM (VALUES
    ('SCR-DASH-001','Dashboard',           'MOD-DASH', '/dashboard',                    'DASHBOARD', 1),
    ('SCR-CUST-001','Customer List',       'MOD-CUST', '/customers',                    'LIST',      1),
    ('SCR-CUST-002','Customer Detail',     'MOD-CUST', '/customers/:id',                'DETAIL',    2),
    ('SCR-CUST-003','New Customer',        'MOD-CUST', '/customers/new',                'FORM',      3),
    ('SCR-ACC-001', 'Account List',        'MOD-ACC',  '/accounts',                     'LIST',      1),
    ('SCR-ACC-002', 'Account Detail',      'MOD-ACC',  '/accounts/:id',                 'DETAIL',    2),
    ('SCR-ACC-003', 'Open Account',        'MOD-ACC',  '/accounts/new',                 'FORM',      3),
    ('SCR-TXN-001', 'Transaction Ledger',  'MOD-TXN',  '/transactions',                 'LIST',      1),
    ('SCR-TXN-002', 'Manual Transaction',  'MOD-TXN',  '/transactions/manual',          'FORM',      2),
    ('SCR-MAINT-001','Institution',        'MOD-MAINT','/maintenance/institution',       'FORM',      1),
    ('SCR-MAINT-002','Currencies',         'MOD-MAINT','/maintenance/currencies',        'LIST',      2),
    ('SCR-MAINT-003','Branches',           'MOD-MAINT','/maintenance/branches',          'LIST',      3),
    ('SCR-MAINT-004','Calendar',           'MOD-MAINT','/maintenance/calendar',          'LIST',      4),
    ('SCR-GL-001',  'Chart of Accounts',   'MOD-GL',   '/gl/accounts',                  'LIST',      1),
    ('SCR-GL-002',  'GL Periods',          'MOD-GL',   '/gl/periods',                   'LIST',      2),
    ('SCR-GL-003',  'Trial Balance',       'MOD-GL',   '/gl/trial-balance',             'REPORT',    3),
    ('SCR-GL-004',  'Transaction Codes',   'MOD-GL',   '/gl/transaction-codes',         'LIST',      4),
    ('SCR-RPT-001', 'Balance Report',      'MOD-RPT',  '/reports/balance',              'REPORT',    1),
    ('SCR-RPT-002', 'Transaction Volume',  'MOD-RPT',  '/reports/transactions',         'REPORT',    2),
    ('SCR-AUDIT-001','Audit Log',          'MOD-AUDIT','/audit',                        'LIST',      1),
    ('SCR-USR-001', 'User Management',     'MOD-ADMIN','/admin/users',                  'ADMIN',     1),
    ('SCR-ROL-001', 'Role Management',     'MOD-ADMIN','/admin/roles',                  'ADMIN',     2),
    ('SCR-PRM-001', 'Permission Assignment','MOD-ADMIN','/admin/permissions',            'ADMIN',     3),
    ('SCR-MCQ-001', 'Approval Queue',      'MOD-ADMIN','/admin/approvals',              'ADMIN',     4)
) AS sc(screen_code, screen_name, module_code, route_path, screen_type, sort_order)
JOIN modules m ON m.module_code = sc.module_code;
