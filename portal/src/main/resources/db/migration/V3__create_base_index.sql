--Новости
CREATE INDEX IF NOT EXISTS idx_news_group_and_coalesce_date
ON news (group_user_id, (COALESCE(updated_at, creation_date)) DESC);