CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TYPE task_status AS ENUM (
    'OPEN',
    'CLOSED',
    'DENIED',
    'PROCESSED'
);

CREATE TYPE task_priority AS ENUM (
    'LOW',
    'MIDDLE',
    'HIGH'
);

CREATE TABLE IF NOT EXISTS group_task (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    user_group_id INT NOT NULL,
    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS group_user (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    phone_number VARCHAR(20),
    internal_number VARCHAR(20),
    extension_number VARCHAR(20),
    avatar_url VARCHAR(255),
    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS work_type (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    priority task_priority NOT NULL DEFAULT 'LOW',
    wiki_link VARCHAR(2048),
    group_task_id INT NOT NULL REFERENCES group_task(id) ON DELETE CASCADE,
    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS account (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    login VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) UNIQUE,
    group_id INT NOT NULL REFERENCES group_user(id)
);

CREATE TABLE IF NOT EXISTS apteka (
    id UUID PRIMARY KEY REFERENCES accounts(id) ON DELETE CASCADE,
    number INT NOT NULL,
    adress VARCHAR(255),
    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS client (
    id UUID PRIMARY KEY REFERENCES accounts(id) ON DELETE CASCADE,
    full_name VARCHAR(150) NOT NULL,
    extension_number VARCHAR(20),
    avatar_url VARCHAR(255),
    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS client_roles (
    client_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    CONSTRAINT fk_client FOREIGN KEY (client_id) REFERENCES client(id) ON DELETE CASCADE,
    PRIMARY KEY (client_id, role) 
);

CREATE TABLE IF NOT EXISTS task (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,
    creation_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    closing_date TIMESTAMPTZ,
    updated_date TIMESTAMPTZ,
    status task_status NOT NULL DEFAULT 'OPEN', 
    work_type_id INT NOT NULL REFERENCES work_type(id), 
    assigned_client_id UUID REFERENCES client(id) ON DELETE SET NULL,
    assigned_apteka_id UUID REFERENCES apteka(id) ON DELETE SET NULL,
    created_by_apteka_id UUID REFERENCES apteka(id) ON DELETE SET NULL,
    created_by_client_id UUID REFERENCES client(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS task_picture (
    id BIGSERIAL PRIMARY KEY,
    path VARCHAR(255) UNIQUE NOT NULL,
    task_id BIGINT NOT NULL REFERENCES task(id) ON DELETE CASCADE 
);

CREATE TABLE IF NOT EXISTS task_comment (
    id BIGSERIAL PRIMARY KEY,
    comment VARCHAR(255) NOT NULL,
    task_id BIGINT NOT NULL REFERENCES task(id) ON DELETE CASCADE,
    client_id UUID REFERENCES client(id) ON DELETE SET NULL,
    apteka_id UUID REFERENCES apteka(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS news (
    id SERIAL PRIMARY KEY,
    title VARCHAR(50) NOT NULL,
    news_text TEXT NOT NULL,
    author_id UUID REFERENCES accounts(id) ON DELETE SET NULL,
    group_user_id INT NOT NULL REFERENCES group_user(id) ON DELETE CASCADE,
    creation_date TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS groups_main_page_links (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(100),
    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS main_page_links (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    link VARCHAR(2048) NOT NULL,
    group_link_id INT NOT NULL,
    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(50),
    FOREIGN KEY (group_link_id) REFERENCES groups_main_page_links(id) ON DELETE CASCADE
);
