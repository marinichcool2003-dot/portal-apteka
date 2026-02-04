TRUNCATE TABLE group_client RESTART IDENTITY CASCADE;

INSERT INTO group_client values
    (1, 'IT-отдел'),
    (2, 'Отдел Розницы'),
    (3, 'АХО'),
    (4, 'Территориальные менеджеры');