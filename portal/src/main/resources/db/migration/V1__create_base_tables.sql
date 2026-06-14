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

CREATE TABLE IF NOT EXISTS group_task(
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    user_group_id INT NOT NULL
);

CREATE TABLE IF NOT EXISTS task_picture (
    id BIGSERIAL PRIMARY KEY,
    path VARCHAR(255) UNIQUE NOT NULL,
    task_id BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS group_user(
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    phone_number VARCHAR(20) 
);

CREATE TABLE IF NOT EXISTS work_type(
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    group_task_id INT NOT NULL
);

CREATE TABLE IF NOT EXISTS apteka(
    id SERIAL PRIMARY KEY,
    login VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    number INT NOT NULL,
    adress VARCHAR(255),
    phone_number VARCHAR(20) UNIQUE,
    group_id INT NOT NULL
);

CREATE TABLE IF NOT EXISTS client(
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    login VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    group_id INT NOT NULL,
    avatar_url VARCHAR(255)
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
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    closing_date TIMESTAMP,
    updated_date TIMESTAMP,
    status task_status NOT NULL DEFAULT 'OPEN',
    priority task_priority NOT NULL DEFAULT 'LOW', 
    work_type_id INT NOT NULL,
    assigned_client_id UUID,
    assigned_apteka_id INT,
    created_by_apteka_id INT,
    created_by_client_id UUID   
);

CREATE TABLE IF NOT EXISTS task_comment(
    id BIGSERIAL PRIMARY KEY,
    comment VARCHAR(255) NOT NULL,
    task_id BIGINT NOT NULL,
    client_id UUID,
    apteka_id INT
);

CREATE TABLE news(
    id SERIAL PRIMARY KEY,
    title VARCHAR(50) NOT NULL,
    news_text TEXT NOT NULL,
    author_id UUID,
    group_user_id INT NOT NULL,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP,
    last_modified_by VARCHAR(50)
);

CREATE TABLE groups_main_page_links(
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(100)
);

CREATE TABLE main_page_links(
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    link TEXT NOT NULL,
    group_link_id INT NOT NULL,
    FOREIGN KEY (group_link_id) REFERENCES groups_main_page_links(id) ON DELETE CASCADE,
    CHECK (LENGTH(link) < 1000)
);