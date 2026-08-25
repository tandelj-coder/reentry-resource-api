-- V5__create_followups.sql
CREATE TYPE followup_status AS ENUM ('PENDING', 'COMPLETED', 'SKIPPED', 'OVERDUE');

CREATE TABLE follow_ups (
    id BIGSERIAL PRIMARY KEY,
    referral_id BIGINT NOT NULL,
    assigned_to_id BIGINT NOT NULL,
    due_date DATE NOT NULL,
    status followup_status DEFAULT 'PENDING',
    outcome TEXT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (referral_id) REFERENCES referrals(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_to_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE INDEX idx_follow_ups_referral ON follow_ups(referral_id);
CREATE INDEX idx_follow_ups_assigned_to ON follow_ups(assigned_to_id);
CREATE INDEX idx_follow_ups_due_date ON follow_ups(due_date);
CREATE INDEX idx_follow_ups_status ON follow_ups(status);
