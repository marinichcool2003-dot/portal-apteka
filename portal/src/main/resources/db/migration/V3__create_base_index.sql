-- 1. Для личных задач сотрудника (поиск по сотруднику -> статусу -> сортировка по дате)
CREATE INDEX idx_task_assigned_client_status_date 
ON task(assigned_client_id, status, creation_date DESC);

-- 2. Для личных задач аптеки (поиск по аптеке -> статусу -> сортировка по дате)
CREATE INDEX idx_task_assigned_apteka_status_date 
ON task(assigned_apteka_id, status, creation_date DESC);

-- 3. Индекс для фильтрации по типу работы (используется при JOIN с work_type)
CREATE INDEX idx_task_work_type_id 
ON task(work_type_id);

-- 4. Уникальный индекс на номер аптеки (гарантирует, что номера аптек не повторяются)
CREATE UNIQUE INDEX idx_apteka_number_unique 
ON apteka(number);

-- 5. РЕКОМЕНДУЕМЫЙ ИНДЕКС: Для быстрого поиска созданных пользователем задач
-- Поможет вашему методу getMyStats быстро считать количество созданных задач
CREATE INDEX idx_task_created_by_client 
ON task(created_by_client_id) 
WHERE created_by_client_id IS NOT NULL;

CREATE INDEX idx_task_created_by_apteka 
ON task(created_by_apteka_id) 
WHERE created_by_apteka_id IS NOT NULL;
