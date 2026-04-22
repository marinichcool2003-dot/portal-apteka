-- Для поиска по группе сотрудников + статус + дата (самый частый запрос)
CREATE INDEX idx_task_group_client_status_date ON task(group_client_id, status, creation_date DESC);

-- Для личных задач сотрудника
CREATE INDEX idx_task_assigned_client_status_date ON task(assigned_client_id, status, creation_date DESC);

-- Для личных задач аптеки
CREATE INDEX idx_task_assigned_apteka_status_date ON task(assigned_apteka_id, status, creation_date DESC);

-- Индекс для фильтрации по группе задач (Category)
CREATE INDEX idx_task_work_type_id ON task(work_type_id);

CREATE INDEX idx_task_stats_client_status ON task(assigned_client_id, status);

-- Для групп сотрудников
CREATE INDEX idx_task_group_client_stats ON task(group_user_id, status);
