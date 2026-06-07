CREATE TABLE IF NOT EXISTS outbox_events
(
    id         BIGSERIAL PRIMARY KEY,
    topic      VARCHAR(100)            NOT NULL,
    payload    TEXT                    NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    sent       BOOLEAN   DEFAULT FALSE NOT NULL
);