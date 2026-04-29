DROP TABLE IF EXISTS apteka; 
CREATE TABLE IF NOT EXISTS apteka(
    id SERIAL PRIMARY KEY,
    login VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    number INT NOT NULL,
    adress VARCHAR(255),
    phone_number VARCHAR(20) UNIQUE,
    group_id INT NOT NULL
);