CREATE TABLE task_picture (
    id SERIAL PRIMARY KEY(),
    path VARCHAR(255) NOT NULL,
    task_id INT NOT NULL
)