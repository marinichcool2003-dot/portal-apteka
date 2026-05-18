CREATE TABLE IF NOT EXISTS client_roles (
    client_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    CONSTRAINT fk_client FOREIGN KEY (client_id) REFERENCES client(id) ON DELETE CASCADE,
    PRIMARY KEY (client_id, role) 
);