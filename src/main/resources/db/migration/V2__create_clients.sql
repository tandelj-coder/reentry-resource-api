-- V2__create_clients.sql
CREATE TYPE client_need AS ENUM ('HOUSING', 'EMPLOYMENT', 'LEGAL_AID', 'MENTAL_HEALTH', 'ADDICTION_SUPPORT', 'EDUCATION', 'FINANCIAL_ASSISTANCE', 'FAMILY_SUPPORT', 'HEALTH_CARE', 'TRANSPORTATION');

CREATE TABLE clients (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    birth_year INTEGER,
    city VARCHAR(100),
    postal_code_prefix VARCHAR(10),
    assigned_caseworker_id BIGINT,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (assigned_caseworker_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE client_needs (
    client_id BIGINT NOT NULL,
    need client_need NOT NULL,
    PRIMARY KEY (client_id, need),
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE
);

CREATE INDEX idx_clients_caseworker ON clients(assigned_caseworker_id);
CREATE INDEX idx_clients_city ON clients(city);
