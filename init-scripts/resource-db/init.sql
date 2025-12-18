CREATE TABLE IF NOT EXISTS resources (
    id BIGSERIAL PRIMARY KEY,
    storage_key VARCHAR(255) NOT NULL UNIQUE,
    content_type VARCHAR(255),
    file_name VARCHAR(255)
);
