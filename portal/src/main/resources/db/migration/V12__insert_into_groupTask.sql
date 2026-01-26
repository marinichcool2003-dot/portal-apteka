TRUNCATE TABLE group_task RESTART IDENTITY CASCADE;

INSERT into group_task values
    (1, 'Программное обеспечение'), 
    (2, 'Оборудование'),
    (3, 'Интернет'),
    (4, 'Телефония'),
    (5, 'Камеры'),
    (6, 'Удаленный доступ');