CREATE TABLE IF NOT EXISTS client_roles (
    client_id UUID NOT NULL,
    role client_role NOT NULL,
    -- Внешний ключ, чтобы при удалении клиента удалялись и его роли
    CONSTRAINT fk_client FOREIGN KEY (client_id) REFERENCES client(id) ON DELETE CASCADE,
    -- Уникальный индекс, чтобы одна и та же роль не дублировалась у юзера
    PRIMARY KEY (client_id, role) 
);