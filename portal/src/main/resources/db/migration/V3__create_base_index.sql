CREATE INDEX IF NOT EXISTS idx_work_type_priority ON work_type(priority);
CREATE INDEX IF NOT EXISTS idx_work_type_group_task_id ON work_type(group_task_id);
CREATE INDEX IF NOT EXISTS idx_task_work_type_id ON task(work_type_id);
CREATE INDEX IF NOT EXISTS idx_task_status ON task(status);
CREATE INDEX IF NOT EXISTS idx_task_comment_task_id ON task_comment(task_id);
CREATE INDEX IF NOT EXISTS idx_mail_outbox_status_next_attempt ON mail_outbox(status, next_attempt_at);