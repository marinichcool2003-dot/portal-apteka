CREATE EXTENSION IF NOT EXISTS "pgcrypto";
-- =========================================================================
-- ТАБЛИЦЫ СТРУКТУРЫ И ГРУПП
-- =========================================================================
CREATE TABLE IF NOT EXISTS user_group (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    phone_number VARCHAR(20),
    internal_number VARCHAR(20),
    extension_number VARCHAR(20),
    avatar_url VARCHAR(255),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(50),
    group_type VARCHAR(32) NOT NULL,
    is_active BOOLEAN DEFAULT true
);

CREATE TABLE IF NOT EXISTS group_group_visibility (
    id SERIAL PRIMARY KEY,
    first_group_id INT NOT NULL REFERENCES group_user(id) ON DELETE CASCADE,
    second_group_id INT NOT NULL REFERENCES group_user(id) ON DELETE CASCADE,
    UNIQUE(first_group_id, second_group_id)
);

CREATE TABLE IF NOT EXISTS group_task (
    id SERIAL PRIMARY KEY,
    creator_group_id INT NOT NULL REFERENCES group_user(id) ON DELETE CASCADE,
    executor_group_id INT NOT NULL REFERENCES group_user(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(50),
    is_active BOOLEAN DEFAULT true,
    UNIQUE(name, creator_group_id, executor_group_id)
);

CREATE TABLE IF NOT EXISTS work_type (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'LOW',
    wiki_link VARCHAR(2048),
    comment_for_creator VARCHAR(1024),
    group_task_id INT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(50),
    is_active BOOLEAN DEFAULT true
);

-- =========================================================================
-- СУЩНОСТИ ПОЛЬЗОВАТЕЛЕЙ И АВТОРИЗАЦИИ
-- =========================================================================
CREATE TABLE IF NOT EXISTS account (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    login VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) UNIQUE,
    is_active BOOLEAN DEFAULT true
);

CREATE TABLE IF NOT EXISTS account_user_group_relation (
    id BIGSERIAL PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES account(id) ON DELETE CASCADE,
    user_group_id INT NOT NULL REFERENCES user_group(id) ON DELETE CASCADE,
    role VARCHAR(30) NOT NULL,
    UNIQUE (account_id, user_group_id)
);

CREATE TABLE IF NOT EXISTS account_actions (
    relation_id BIGINT NOT NULL REFERENCES account_user_group_relation(id) ON DELETE CASCADE,
    action VARCHAR(50) NOT NULL,
    UNIQUE (relation_id, action)
);

CREATE TABLE IF NOT EXISTS apteka (
    id UUID PRIMARY KEY,
    number INT NOT NULL,
    apteka_name VARCHAR(30) NOT NULL,
    address_id BIGINT,
    territorial_id UUID REFERENCES client(id) ON DELETE SET NULL, --FIX
    created_by VARCHAR(50),
    created_at TIMESTAMPTZ DEFAULT NOW(),
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
    created_at TIMESTAMPTZ DEFAULT NOW(),
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
    creation_date TIMESTAMPTZ DEFAULT NOW(),
    closing_date TIMESTAMPTZ,
    updated_date TIMESTAMPTZ,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN', 
    work_type_id INT NOT NULL, 
    creator_id UUID NOT NULL,
    assigner_id UUID 
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
    account_id UUID
);

CREATE TABLE IF NOT EXISTS spectator (
    task_id BIGINT REFERENCES task(id),
    account_id UUID REFERENCES account(id),
    PRIMARY KEY (task_id, account_id)
)

-- =========================================================================
-- ИНФОРМАЦИОННЫЕ ТАБЛИЦЫ И ССЫЛКИ
-- =========================================================================
CREATE TABLE IF NOT EXISTS news (
    id SERIAL PRIMARY KEY,
    title VARCHAR(50) NOT NULL,
    news_text TEXT NOT NULL,
    author_id UUID,
    group_user_id INT NOT NULL,
    creation_date TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS groups_main_page_links (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(50),
    is_active BOOLEAN DEFAULT true
);

CREATE TABLE IF NOT EXISTS main_page_links (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    link VARCHAR(2048) NOT NULL,
    group_link_id INT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(50),
    is_active BOOLEAN DEFAULT true
);

-- =========================================================================
-- УВЕДОМЛЕНИЯ
-- =========================================================================
CREATE TABLE notifications_preference (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    account_id UUID REFERENCES accounts(id) ON DELETE CASCADE,
    channel VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT FALSE
);

-- =========================================================================
-- ПОЧТА
-- =========================================================================
CREATE TABLE mail_outbox (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    to_email VARCHAR(255) NOT NULL,
    subject VARCHAR(512) NOT NULL,
    body_html TEXT NOT NULL,
    status VARCHAR(16) NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    event_type VARCHAR(64) NOT NULL,
    body_plain TEXT
);

-- =========================================================================
-- РЕЙТИНГ
-- =========================================================================
CREATE TABLE apteka_task_rating (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL ON DELETE CASCADE,
    apteka_id UUID NOT NULL REFERENCES apteka(id) ON DELETE CASCADE,
    rater_client_id UUID NOT NULL REFERENCES client(id) ON DELETE SET NULL,
    stars SMALLINT NOT NULL,
    reason TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    employee_edit_count SMALLINT DEFAULT 0
);

-- =========================================================================
-- ОБОРУДОВАНИЕ
-- =========================================================================
CREATE TABLE IF NOT EXISTS equipment_type(
    id SMALLSERIAL PRIMARY KEY,
    name VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS equipment(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    apteka_id UUID REFERENCES apteka(id) ON DELETE RESTRICT,
    type_id SMALLINT NOT NULL REFERENCES equipment_type(id) ON DELETE RESTRICT
);
