CREATE TRIGGER set_timestamp_apteka
BEFORE UPDATE ON apteka
FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();

CREATE TRIGGER set_timestamp_client
BEFORE UPDATE ON client
FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();

CREATE TRIGGER set_timestamp_groups_main_page_links
BEFORE UPDATE ON groups_main_page_links
FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();

CREATE TRIGGER set_timestamp_news
BEFORE UPDATE ON news
FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();

CREATE TRIGGER set_timestamp_main_page_links
BEFORE UPDATE ON main_page_links
FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();

CREATE TRIGGER set_timestamp_group_task
BEFORE UPDATE ON group_task
FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();

CREATE TRIGGER set_timestamp_group_user
BEFORE UPDATE ON group_user
FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();

CREATE TRIGGER set_timestamp_work_type
BEFORE UPDATE ON work_type
FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();