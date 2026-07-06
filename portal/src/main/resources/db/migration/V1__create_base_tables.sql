CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =========================================================================
-- ТИПЫ ДАННЫХ (ENUMS)
-- =========================================================================
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

-- =========================================================================
-- ТАБЛИЦЫ СТРУКТУРЫ И ГРУПП
-- =========================================================================
CREATE TABLE IF NOT EXISTS group_user (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    phone_number VARCHAR(20),
    internal_number VARCHAR(20),
    extension_number VARCHAR(20),
    avatar_url VARCHAR(255),
    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(50),
    is_active BOOLEAN DEFAULT true
);

CREATE TABLE IF NOT EXISTS group_group_visibility (
    first_group_id INT NOT NULL REFERENCES group_user(id) ON DELETE CASCADE,
    second_group_id INT NOT NULL REFERENCES group_user(id) ON DELETE CASCADE,
    PRIMARY KEY(first_group_id, second_group_id)
);

CREATE TABLE IF NOT EXISTS group_task (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    user_group_id INT NOT NULL,
    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(50),
    is_active BOOLEAN DEFAULT true,
    is_internal BOOLEAN DEFAULT true
);

CREATE TABLE IF NOT EXISTS work_type (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    priority task_priority NOT NULL DEFAULT 'LOW',
    wiki_link VARCHAR(2048),
    group_task_id INT NOT NULL,
    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(50)
);

-- =========================================================================
-- СУЩНОСТИ ПОЛЬЗОВАТЕЛЕЙ И АВТОРИЗАЦИИ
-- =========================================================================
CREATE TABLE IF NOT EXISTS account (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    login VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) UNIQUE,
    role VARCHAR(30) NOT NULL,
    group_id INT NOT NULL,
    is_active BOOLEAN DEFAULT true
);

CREATE TABLE IF NOT EXISTS account_actions (
    account_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    PRIMARY KEY (account_id, action) 
);

CREATE TABLE IF NOT EXISTS apteka (
    id UUID PRIMARY KEY,
    number INT NOT NULL,
    address_id BIGINT,
    created_by VARCHAR(50),
    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS address(
    id BIGSERIAL PRIMARY KEY,
    city VARCHAR(100),
    street VARCHAR(150),
    house VARCHAR(20),
    fias_id UUID
);

CREATE TABLE IF NOT EXISTS client (
    id UUID PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    extension_number VARCHAR(20),
    avatar_url VARCHAR(255),
    created_by VARCHAR(50),
    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(50)
);

-- =========================================================================
-- ТАБЛИЦЫ ЗАДАЧ И КОММЕНТАРИЕВ
-- =========================================================================
CREATE TABLE IF NOT EXISTS task (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,
    creation_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    closing_date TIMESTAMPTZ,
    updated_date TIMESTAMPTZ,
    status task_status NOT NULL DEFAULT 'OPEN', 
    work_type_id INT NOT NULL, 
    assigned_client_id UUID,
    assigned_apteka_id UUID,
    created_by_apteka_id UUID,
    created_by_client_id UUID   
);

CREATE TABLE IF NOT EXISTS task_picture (
    id BIGSERIAL PRIMARY KEY,
    path VARCHAR(255) UNIQUE NOT NULL,
    task_id BIGINT NOT NULL 
);

CREATE TABLE IF NOT EXISTS task_comment (
    id BIGSERIAL PRIMARY KEY,
    comment VARCHAR(255) NOT NULL,
    task_id BIGINT NOT NULL,
    client_id UUID,
    apteka_id UUID
);

-- =========================================================================
-- ИНФОРМАЦИОННЫЕ ТАБЛИЦЫ И ССЫЛКИ
-- =========================================================================
CREATE TABLE IF NOT EXISTS news (
    id SERIAL PRIMARY KEY,
    title VARCHAR(50) NOT NULL,
    news_text TEXT NOT NULL,
    author_id UUID,
    group_user_id INT NOT NULL,
    creation_date TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS groups_main_page_links (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
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
    updated_by VARCHAR(50)
);