CREATE TABLE IF NOT EXISTS task (
    id SERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,
    comments VARCHAR(255),
    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status task_status NOT NULL DEFAULT 'OPEN',
    priority task_priority NOT NULL DEFAULT 'LOW', 
    group_id INT NOT NULL,
    client_id UUID NOT NULL,
    created_by_apteka_id INT,
    created_by_client_id UUID   
);