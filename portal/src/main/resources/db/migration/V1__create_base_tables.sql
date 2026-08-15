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
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    updated_by VARCHAR(50),
    group_type VARCHAR(32) NOT NULL,
    is_active BOOLEAN DEFAULT true
);


CREATE TABLE IF NOT EXISTS group_group_visibility (
    first_group_id INT NOT NULL REFERENCES user_group(id) ON DELETE CASCADE,
    second_group_id INT NOT NULL REFERENCES user_group(id) ON DELETE CASCADE,
    PRIMARY KEY (first_group_id, second_group_id)
);

CREATE INDEX IF NOT EXISTS idx_group_visibility_reverse ON group_group_visibility(second_group_id, first_group_id);

CREATE TABLE IF NOT EXISTS group_task (
    id SERIAL PRIMARY KEY,
    creator_group_id INT NOT NULL REFERENCES user_group(id) ON DELETE CASCADE,
    executor_group_id INT NOT NULL REFERENCES user_group(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    updated_by VARCHAR(50),
    is_active BOOLEAN DEFAULT true,
    UNIQUE(creator_group_id, executor_group_id, name)
);

CREATE TABLE IF NOT EXISTS work_type (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'LOW',
    wiki_link VARCHAR(2048),
    comment_for_creator VARCHAR(1024),
    group_task_id INT NOT NULL REFERENCES group_task(id),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    updated_by VARCHAR(50),
    is_active BOOLEAN DEFAULT true
);

CREATE INDEX idx_work_type_group_task_id ON work_type(group_task_id);

-- =========================================================================
-- СУЩНОСТИ ПОЛЬЗОВАТЕЛЕЙ И АВТОРИЗАЦИИ
-- =========================================================================
CREATE TABLE IF NOT EXISTS account (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    login VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) UNIQUE,
    is_active BOOLEAN DEFAULT true
);

CREATE TABLE IF NOT EXISTS account_user_group_relation (
    id BIGSERIAL PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES account(id) ON DELETE CASCADE,
    user_group_id INT NOT NULL REFERENCES user_group(id) ON DELETE CASCADE,
    role VARCHAR(30) NOT NULL,
    UNIQUE (user_group_id, account_id)
);

CREATE TABLE IF NOT EXISTS account_actions (
    relation_id BIGINT NOT NULL REFERENCES account_user_group_relation(id) ON DELETE CASCADE,
    action VARCHAR(50) NOT NULL,
    UNIQUE (relation_id, action)
);

CREATE TABLE IF NOT EXISTS apteka (
    id UUID PRIMARY KEY REFERENCES account(id) ON DELETE CASCADE,
    number INT NOT NULL,
    apteka_name VARCHAR(30) NOT NULL,
    address_id BIGINT REFERENCES address(id) UNIQUE ON DELETE RESTRICT,
    territorial_id UUID REFERENCES client(id) ON DELETE SET NULL,
    created_by VARCHAR(50),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    updated_by VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_apteka_address_territorial_id ON apteka(territorial_id, address_id);
CREATE INDEX IF NOT EXISTS idx_apteka_number ON apteka(number);

CREATE TABLE IF NOT EXISTS address(
    id BIGSERIAL PRIMARY KEY,
    city VARCHAR(100),
    street VARCHAR(150),
    house VARCHAR(20),
    fias_id UUID UNIQUE
);

CREATE INDEX IF NOT EXISTS idx_address_city ON address(city);

CREATE TABLE IF NOT EXISTS client (
    id UUID PRIMARY KEY REFERENCES account(id) ON DELETE CASCADE,
    full_name VARCHAR(150) UNIQUE NOT NULL,
    extension_number VARCHAR(20),
    avatar_url VARCHAR(255),
    created_by VARCHAR(50),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    updated_by VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS territorial_regional_relation(
    regional_id UUID NOT NULL REFERENCES client(id),
    territorial_id UUID NOT NULL REFERENCES client(id),
    PRIMARY KEY (regional_id, territorial_id)
)

CREATE INDEX IF NOT EXISTS idx_territorial_regional_reverse ON territorial_regional_relation(territorial_id, regional_id);

-- =========================================================================
-- ТАБЛИЦЫ ЗАДАЧ И КОММЕНТАРИЕВ
-- =========================================================================
CREATE TABLE IF NOT EXISTS task (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,
    creation_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    closing_date TIMESTAMPTZ,
    updated_date TIMESTAMPTZ DEFAULT NOW(),
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN', 
    work_type_id INT NOT NULL REFERENCES work_type(id),
    creator_id UUID NOT NULL REFERENCES account(id),
    assigner_id UUID REFERENCES account(id)
);

CREATE INDEX IF NOT EXISTS idx_task_work_type ON task(work_type_id, creation_date DESC)
    WHERE status IN ('OPEN', 'PROCESSED');

CREATE INDEX IF NOT EXISTS idx_task_assigner ON task(assigner_id, creation_date DESC)
    WHERE status IN ('OPEN', 'PROCESSED');

CREATE INDEX IF NOT EXISTS idx_task_creator ON task(creator_id, creation_date DESC)
    WHERE status IN ('OPEN', 'PROCESSED');

CREATE TABLE IF NOT EXISTS attachment (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    task_comment_id BIGINT REFERENCES task_comment(id),
    task_id BIGINT REFERENCES task(id),
    CHECK (task_id IS NOT NULL OR task_comment_id IS NOT NULL)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_attachment_task_unique
    ON attachment (task_id, document_type, file_name)
    WHERE task_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_attachment_task_comment_unique
    ON attachment (task_comment_id, document_type, file_name)
    WHERE task_comment_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS task_comment (
    id BIGSERIAL PRIMARY KEY,
    comment VARCHAR(255) NOT NULL,
    task_id BIGINT NOT NULL REFERENCES task(id) ON DELETE CASCADE,
    account_id UUID NOT NULL REFERENCES account(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_task_comment_task ON task_comment(task_id);

CREATE TABLE IF NOT EXISTS spectator (
    task_id BIGINT REFERENCES task(id) ON DELETE CASCADE,
    account_id UUID REFERENCES account(id) ON DELETE CASCADE,
    PRIMARY KEY (task_id, account_id)
);

CREATE INDEX IF NOT EXISTS idx_spectator_account ON spectator(account_id);

-- =========================================================================
-- ИНФОРМАЦИОННЫЕ ТАБЛИЦЫ И ССЫЛКИ
-- =========================================================================
CREATE TABLE IF NOT EXISTS news (
    id SERIAL PRIMARY KEY,
    title VARCHAR(50) NOT NULL,
    news_text TEXT NOT NULL,
    author_id UUID NOT NULL REFERENCES account(id) ON DELETE RESTRICT,
    user_group_id INT NOT NULL REFERENCES user_group(id) ON DELETE CASCADE,
    creation_date TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    updated_by VARCHAR(50),
    CHECK (LENGTH(news_text) <= 2000 AND LENGTH(news_text) >= 10)
);

CREATE INDEX IF NOT EXISTS idx_news_user_group ON news(user_group_id, updated_at DESC);

CREATE TABLE IF NOT EXISTS groups_main_page_links (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    updated_by VARCHAR(50),
    is_active BOOLEAN DEFAULT true
);

CREATE TABLE IF NOT EXISTS main_page_links (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    link VARCHAR(2048) NOT NULL UNIQUE,
    group_link_id INT NOT NULL REFERENCES groups_main_page_links(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    updated_by VARCHAR(50),
    is_active BOOLEAN DEFAULT true,
    UNIQUE (group_link_id, name)
);

CREATE INDEX IF NOT EXISTS idx_main_page_links_group_link_id ON main_page_links(group_link_id);

-- =========================================================================
-- УВЕДОМЛЕНИЯ
-- =========================================================================
CREATE TABLE notifications_preference (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    account_id UUID REFERENCES account(id) ON DELETE CASCADE,
    channel VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE(account_id, channel, event_type)
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
    attempts SMALLINT NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    event_type VARCHAR(64) NOT NULL,
    body_plain TEXT
);

CREATE INDEX IF NOT EXISTS idx_mail_outbox_status_next_attempt ON mail_outbox(status, next_attempt_at);

-- =========================================================================
-- РЕЙТИНГ
-- =========================================================================
CREATE TABLE apteka_task_rating (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES task(id) ON DELETE SET CASCADE,
    apteka_id UUID NOT NULL REFERENCES apteka(id) ON DELETE CASCADE,
    rater_client_id UUID NOT NULL REFERENCES client(id) ON DELETE SET NULL,
    stars SMALLINT NOT NULL,
    reason TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    employee_edit_count SMALLINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_rating_apteka ON apteka_task_rating(apteka_id);

-- =========================================================================
-- ОБОРУДОВАНИЕ
-- =========================================================================
CREATE TABLE IF NOT EXISTS equipment_type(
    id SMALLSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE
);

CREATE TABLE IF NOT EXISTS equipment(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    apteka_id UUID REFERENCES apteka(id) ON DELETE RESTRICT,
    type_id SMALLINT NOT NULL REFERENCES equipment_type(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_equipment_apteka_type ON equipment(apteka_id, type_id);
