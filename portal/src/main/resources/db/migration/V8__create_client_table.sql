DROP TABLE IF EXISTS client_roles;
DROP TABLE IF EXISTS client;
CREATE TABLE IF NOT EXISTS client(
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    login VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    group_id INT NOT NULL,
    avatar_url VARCHAR(255)
);

CREATE TABLE client_roles (
    client_id UUID NOT NULL,
    role client_role NOT NULL DEFAULT 'USER',
    CONSTRAINT fk_client_roles FOREIGN KEY (client_id) REFERENCES client (id) ON DELETE CASCADE
);