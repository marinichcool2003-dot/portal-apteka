CREATE TABLE news(
    id SERIAL PRIMARY KEY,
    title VARCHAR(50) NOT NULL,
    news_text TEXT NOT NULL,
    author_id UUID,
    group_user_id INT NOT NULL,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP
);

ALTER TABLE news 
ADD CONSTRAINT fk_author_id_client_id
FOREIGN KEY (author_id) REFERENCES client(id) ON DELETE SET NULL;

ALTER TABLE news
ADD CONSTRAINT fk_author_id_group_user_id
FOREIGN KEY (group_user_id) REFERENCES group_user(id) ON DELETE CASCADE;

ALTER TABLE news
ADD CONSTRAINT check_news_text_length
CHECK(LENGTH(news_text) <= 2000 AND LENGTH(news_text) >= 10);