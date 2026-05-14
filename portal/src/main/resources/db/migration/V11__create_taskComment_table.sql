CREATE TABLE IF NOT EXISTS task_comment(
    id BIGSERIAL PRIMARY KEY,
    comment VARCHAR(255) NOT NULL,
    task_id BIGINT NOT NULL,
    client_id UUID,
    apteka_id INT
);