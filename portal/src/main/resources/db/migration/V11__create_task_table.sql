DROP TABLE IF EXISTS task;
CREATE TABLE IF NOT EXISTS task (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,
    comments VARCHAR(255),
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    closing_date TIMESTAMP,
    updated_date TIMESTAMP,
    status task_status NOT NULL DEFAULT 'OPEN',
    priority task_priority NOT NULL DEFAULT 'LOW', 
    work_type_id INT NOT NULL,
    group_user_id INT,
    assigned_client_id UUID,
    assigned_apteka_id INT,
    created_by_apteka_id INT,
    created_by_client_id UUID   
);