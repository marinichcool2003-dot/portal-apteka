CREATE TABLE refresh_token(
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    token TEXT NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    remember_me BOOLEAN DEFAULT false NOT NULL
);