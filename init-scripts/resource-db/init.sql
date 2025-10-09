CREATE TABLE IF NOT EXISTS resources (
    id BIGSERIAL PRIMARY KEY,
    data BYTEA NOT NULL,
    content_type VARCHAR(255),
    file_name VARCHAR(255)
);
