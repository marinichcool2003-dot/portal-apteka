ALTER TABLE task 
ADD CONSTRAINT fk_work_type_task
FOREIGN KEY (work_type_id) REFERENCES work_type(id);

ALTER TABLE work_type 
ADD CONSTRAINT fk_work_type_group
FOREIGN KEY (group_task_id) REFERENCES group_task(id);

ALTER TABLE apteka 
ADD CONSTRAINT fk_group_apteki
FOREIGN KEY (group_id) REFERENCES group_apteki(id);

ALTER TABLE task 
ADD CONSTRAINT fk_client_task
FOREIGN KEY (client_id) REFERENCES client(id);

ALTER TABLE task_comments
ADD CONSTRAINT fk_task_comments_task
FOREIGN KEY (task_id) REFERENCES task(id);

ALTER TABLE task_comments
ADD CONSTRAINT fk_task_comments_client
FOREIGN KEY (client_id) REFERENCES client(id);

ALTER TABLE task_comments
ADD CONSTRAINT fk_task_comments_apteka
FOREIGN KEY (apteka_id) REFERENCES apteka(id);

ALTER TABLE client
ADD CONSTRAINT fk_client_group_client
FOREIGN KEY (group_id) REFERENCES group_client(id);

ALTER TABLE task
ADD CONSTRAINT fk_created_by_apteka
FOREIGN KEY (created_by_apteka_id) REFERENCES apteka(id);

ALTER TABLE task
ADD CONSTRAINT fk_created_by_client
FOREIGN KEY (created_by_client_id) REFERENCES client(id);

ALTER TABLE task_picture
ADD CONSTRAINT fk_task_picture
FOREIGN KEY (task_id) REFERENCES task(id);

ALTER TABLE task
ADD CONSTRAINT chk_creator_not_null
CHECK (
    created_by_apteka_id IS NOT NULL OR created_by_client_id IS NOT NULL
);

ALTER TABLE task
ADD CONSTRAINT chk_creator_not_self 
CHECK (
    created_by_client_id IS NULL OR created_by_client_id <> client_id
);