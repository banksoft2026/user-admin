CREATE TABLE maker_checker_queue (
    queue_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    maker_user_id UUID NOT NULL REFERENCES users(user_id),
    checker_user_id UUID REFERENCES users(user_id),
    action_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(100),
    screen_id UUID REFERENCES screens(screen_id),
    payload_before JSONB,
    payload_after JSONB NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('DRAFT','PENDING','APPROVED','REJECTED','EXECUTED','FAILED','EXPIRED','RECALLED')),
    priority VARCHAR(10) NOT NULL DEFAULT 'NORMAL'
        CHECK (priority IN ('LOW','NORMAL','HIGH','URGENT')),
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL DEFAULT NOW() + INTERVAL '24 hours',
    resolved_at TIMESTAMPTZ,
    executed_at TIMESTAMPTZ,
    rejection_reason TEXT,
    execution_error TEXT,
    CONSTRAINT no_self_approve CHECK (checker_user_id IS NULL OR checker_user_id != maker_user_id)
);

CREATE INDEX idx_mcq_status ON maker_checker_queue(status);
CREATE INDEX idx_mcq_maker ON maker_checker_queue(maker_user_id);
CREATE INDEX idx_mcq_action ON maker_checker_queue(action_type);
CREATE INDEX idx_mcq_expires ON maker_checker_queue(expires_at) WHERE status = 'PENDING';
