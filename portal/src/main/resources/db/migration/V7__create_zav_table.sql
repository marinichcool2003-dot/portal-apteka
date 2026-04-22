DROP TABLE IF EXISTS zav;
CREATE TABLE zav(
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(10) UNIQUE
);