CREATE TABLE IF NOT EXISTS task_comments(
    id SERIAL PRIMARY KEY,
    comment VARCHAR(255) NOT NULL,
    task_id SERIAL NOT NULL,
    client_id UUID,
    apteka_id INT
);