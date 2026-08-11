-- AUDIT-FIX: таблица оценок аптеки по закрытым/отклонённым задачам
CREATE TABLE IF NOT EXISTS apteka_task_rating (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT UNIQUE NOT NULL,
    apteka_id UUID NOT NULL,
    rater_account_id UUID NOT NULL,
    stars INT NOT NULL CHECK (stars BETWEEN 1 AND 5),
    reason VARCHAR(1000) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    employee_edit_count INT NOT NULL DEFAULT 0
);

-- AUDIT-FIX: внешние ключи apteka_task_rating
ALTER TABLE apteka_task_rating
    ADD CONSTRAINT fk_apteka_task_rating_task
        FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE;

ALTER TABLE apteka_task_rating
    ADD CONSTRAINT fk_apteka_task_rating_apteka
        FOREIGN KEY (apteka_id) REFERENCES apteka(id) ON DELETE CASCADE;

ALTER TABLE apteka_task_rating
    ADD CONSTRAINT fk_apteka_task_rating_rater
        FOREIGN KEY (rater_account_id) REFERENCES account(id) ON DELETE CASCADE;

-- AUDIT-FIX: индекс для постраничной выборки по аптеке
CREATE INDEX IF NOT EXISTS idx_apteka_task_rating_apteka_id ON apteka_task_rating(apteka_id);

-- AUDIT-FIX: автообновление updated_at при изменении записи
CREATE TRIGGER set_timestamp_apteka_task_rating
BEFORE UPDATE ON apteka_task_rating
FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();
