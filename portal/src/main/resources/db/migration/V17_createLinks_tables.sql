CREATE TABLE groups_main_page_links(
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(100)
);

CREATE TABLE main_page_links(
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    link TEXT NOT NULL,
    group_link_id INT NOT NULL,
    FOREIGN KEY (group_link_id) REFERENCES groups_main_page_links(id) ON DELETE CASCADE,
    CHECK (LENGTH(link) < 1000)
);
