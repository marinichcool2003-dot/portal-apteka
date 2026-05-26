ALTER TABLE task 
ADD CONSTRAINT fk_work_type_task
FOREIGN KEY (work_type_id) REFERENCES work_type(id) ON DELETE SET NULL;

ALTER TABLE task 
ADD CONSTRAINT fk_client_task
FOREIGN KEY (assigned_client_id) REFERENCES client(id);

ALTER TABLE task 
ADD CONSTRAINT fk_apteka_task
FOREIGN KEY (assigned_apteka_id) REFERENCES apteka(id);

ALTER TABLE task
ADD CONSTRAINT fk_created_by_apteka
FOREIGN KEY (created_by_apteka_id) REFERENCES apteka(id);

ALTER TABLE task
ADD CONSTRAINT fk_created_by_client
FOREIGN KEY (created_by_client_id) REFERENCES client(id);

ALTER TABLE task
ADD CONSTRAINT chk_creator_not_null
CHECK (
    created_by_apteka_id IS NOT NULL OR created_by_client_id IS NOT NULL
);

-- ALTER TABLE task
-- ADD CONSTRAINT chk_creator_not_self 
-- CHECK (
--     created_by_client_id IS NULL OR created_by_client_id <> assigned_client_id
-- );

ALTER TABLE work_type 
ADD CONSTRAINT fk_work_type_group
FOREIGN KEY (group_task_id) REFERENCES group_task(id) ON DELETE CASCADE;

ALTER TABLE group_task
ADD CONSTRAINT fk_group_task_group_user
FOREIGN KEY (user_group_id) REFERENCES group_user(id) ON DELETE CASCADE;

ALTER TABLE apteka 
ADD CONSTRAINT fk_group_apteki
FOREIGN KEY (group_id) REFERENCES group_user(id) ON DELETE CASCADE;

ALTER TABLE client
ADD CONSTRAINT fk_group_client
FOREIGN KEY (group_id) REFERENCES group_user(id) ON DELETE CASCADE;

ALTER TABLE task_comment
ADD CONSTRAINT fk_task_comment_task
FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE;

ALTER TABLE task_comment
ADD CONSTRAINT fk_task_comment_client
FOREIGN KEY (client_id) REFERENCES client(id) ON DELETE CASCADE;

ALTER TABLE task_comment
ADD CONSTRAINT fk_task_comment_apteka
FOREIGN KEY (apteka_id) REFERENCES apteka(id) ON DELETE CASCADE;

ALTER TABLE task_picture
ADD CONSTRAINT fk_task_picture
FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE;