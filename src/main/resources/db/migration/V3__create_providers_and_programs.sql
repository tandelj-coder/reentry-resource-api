-- V3__create_providers_and_programs.sql
CREATE TYPE program_category AS ENUM ('HOUSING', 'EMPLOYMENT', 'LEGAL_AID', 'MENTAL_HEALTH', 'ADDICTION_SUPPORT', 'EDUCATION', 'FINANCIAL_ASSISTANCE', 'FAMILY_SUPPORT', 'HEALTH_CARE', 'TRANSPORTATION');
CREATE TYPE intake_status AS ENUM ('OPEN', 'WAITLIST_ONLY', 'CLOSED');

CREATE TABLE service_providers (
    id BIGSERIAL PRIMARY KEY,
    organization_name VARCHAR(255) NOT NULL,
    description TEXT,
    phone VARCHAR(20),
    email VARCHAR(255),
    street_address VARCHAR(255),
    city VARCHAR(100),
    province VARCHAR(50),
    postal_code VARCHAR(10),
    verified BOOLEAN DEFAULT false,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE programs (
    id BIGSERIAL PRIMARY KEY,
    provider_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category program_category NOT NULL,
    eligibility_criteria TEXT,
    city VARCHAR(100) NOT NULL,
    cost NUMERIC(10, 2) DEFAULT 0.00,
    capacity INTEGER,
    available_slots INTEGER,
    intake_status intake_status DEFAULT 'OPEN',
    minimum_age INTEGER,
    maximum_age INTEGER,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (provider_id) REFERENCES service_providers(id) ON DELETE CASCADE
);

CREATE INDEX idx_providers_city ON service_providers(city);
CREATE INDEX idx_programs_provider ON programs(provider_id);
CREATE INDEX idx_programs_category ON programs(category);
CREATE INDEX idx_programs_city ON programs(city);
CREATE INDEX idx_programs_intake_status ON programs(intake_status);
