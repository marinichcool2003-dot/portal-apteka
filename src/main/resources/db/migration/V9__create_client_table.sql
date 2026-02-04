CREATE TABLE IF NOT EXISTS client(
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    login VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role client_role NOT NULL DEFAULT 'USER',
    group_id INT NOT NULL,
    avatar_url VARCHAR(255)
);