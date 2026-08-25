-- V4__create_referrals.sql
CREATE TYPE referral_status AS ENUM ('PENDING', 'ACCEPTED', 'DECLINED', 'COMPLETED', 'WAITLISTED');

CREATE TABLE referrals (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    program_id BIGINT NOT NULL,
    referrer_id BIGINT NOT NULL,
    status referral_status DEFAULT 'PENDING',
    referral_notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    FOREIGN KEY (program_id) REFERENCES programs(id) ON DELETE RESTRICT,
    FOREIGN KEY (referrer_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE TABLE referral_history (
    id BIGSERIAL PRIMARY KEY,
    referral_id BIGINT NOT NULL,
    status_from referral_status,
    status_to referral_status NOT NULL,
    reason TEXT,
    updated_by_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (referral_id) REFERENCES referrals(id) ON DELETE CASCADE,
    FOREIGN KEY (updated_by_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE INDEX idx_referrals_client ON referrals(client_id);
CREATE INDEX idx_referrals_program ON referrals(program_id);
CREATE INDEX idx_referrals_referrer ON referrals(referrer_id);
CREATE INDEX idx_referrals_status ON referrals(status);
CREATE INDEX idx_referral_history_referral ON referral_history(referral_id);
