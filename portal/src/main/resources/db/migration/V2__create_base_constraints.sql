-- =========================================================================
-- ОГРАНИЧЕНИЯ И ВНЕШНИЕ КЛЮЧИ (ALTER TABLES)
-- =========================================================================

-- Связи аккаунтов и профилей
ALTER TABLE account ADD CONSTRAINT fk_account_group_user FOREIGN KEY (group_id) REFERENCES group_user(id) ON DELETE SET NULL;
ALTER TABLE apteka ADD CONSTRAINT fk_apteka_account FOREIGN KEY (id) REFERENCES account(id) ON DELETE CASCADE;
ALTER TABLE client ADD CONSTRAINT fk_client_account FOREIGN KEY (id) REFERENCES account(id) ON DELETE CASCADE;
ALTER TABLE account_actions ADD CONSTRAINT fk_account_actions_account FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE CASCADE;

-- Связи иерархии групп и типов работ
ALTER TABLE group_task ADD CONSTRAINT fk_group_task_group_user FOREIGN KEY (user_group_id) REFERENCES group_user(id) ON DELETE CASCADE;
ALTER TABLE work_type ADD CONSTRAINT fk_work_type_group FOREIGN KEY (group_task_id) REFERENCES group_task(id) ON DELETE CASCADE;

-- Связи задач
ALTER TABLE task ADD CONSTRAINT fk_work_type_task FOREIGN KEY (work_type_id) REFERENCES work_type(id) ON DELETE CASCADE;
ALTER TABLE task ADD CONSTRAINT fk_creator_task FOREIGN KEY (creator_id) REFERENCES account(id) ON DELETE SET NULL;
ALTER TABLE task ADD CONSTRAINT fk_assigner_task FOREIGN KEY (assigner_id) REFERENCES account(id) ON DELETE SET NULL;

-- Чек-рейт создателя задачи
ALTER TABLE task ADD CONSTRAINT chk_creator_not_null CHECK (created_by_apteka_id IS NOT NULL OR created_by_client_id IS NOT NULL);

-- Картинки и комментарии
ALTER TABLE task_picture ADD CONSTRAINT fk_task_picture_task FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE;
ALTER TABLE task_comment ADD CONSTRAINT fk_task_comment_task FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE;
ALTER TABLE task_comment ADD CONSTRAINT fk_task_comment_client FOREIGN KEY (client_id) REFERENCES client(id) ON DELETE CASCADE;
ALTER TABLE task_comment ADD CONSTRAINT fk_task_comment_apteka FOREIGN KEY (apteka_id) REFERENCES apteka(id) ON DELETE CASCADE;

-- Новости и ссылки
ALTER TABLE news ADD CONSTRAINT fk_news_author FOREIGN KEY (author_id) REFERENCES account(id) ON DELETE SET NULL;
ALTER TABLE news ADD CONSTRAINT fk_news_group_user FOREIGN KEY (group_user_id) REFERENCES group_user(id) ON DELETE CASCADE;
ALTER TABLE news ADD CONSTRAINT check_news_text_length CHECK(LENGTH(news_text) <= 2000 AND LENGTH(news_text) >= 10);
ALTER TABLE main_page_links ADD CONSTRAINT fk_main_page_links_group FOREIGN KEY (group_link_id) REFERENCES groups_main_page_links(id) ON DELETE CASCADE;

-- Аптека и адрес
ALTER TABLE apteka ADD CONSTRAINT fk_apteka_address FOREIGN KEY (address_id) REFERENCES address(id) ON DELETE SET NULL;
