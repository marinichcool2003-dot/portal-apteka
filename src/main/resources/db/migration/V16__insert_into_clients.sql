TRUNCATE TABLE client RESTART IDENTITY CASCADE;

INSERT INTO client (login, password, full_name, group_id, avatar_url) VALUES
    ('ivanov_i', 'ivan2024pass', 'Иванов Иван Иванович', 1, 'https://storage.example.com/avatars/ivanov.png'),
    ('petrova_a', 'petrovaSecure1', 'Петрова Анна Сергеевна', 2, 'https://storage.example.com/avatars/anna_petrova.jpg'),
    ('sidorov_m', 'sidorovM2024', 'Сидоров Максим Владимирович', 3, 'https://storage.example.com/avatars/max_sidorov.png'),
    ('smirnova_e', 'elenaSmirnova99', 'Смирнова Елена Дмитриевна', 4, 'https://storage.example.com/avatars/elena_avatar.jpg'),
    ('kuznetsov_p', 'kuznetsovPass123', 'Кузнецов Павел Андреевич', 1, 'https://storage.example.com/avatars/pavel_k.png'),
    ('voronina_s', 'voroninaS2024!', 'Воронина Светлана Петровна', 2, 'https://storage.example.com/avatars/svetlana_v.jpg'),
    ('nikolaev_d', 'dmitryNik2024', 'Николаев Дмитрий Олегович', 3, 'https://storage.example.com/avatars/dmitry_n.png'),
    ('fedorova_o', 'olgaFedorova77', 'Федорова Ольга Васильевна', 4, 'https://storage.example.com/avatars/olga_f.jpg'),
    ('morozov_v', 'morozovVasily88', 'Морозов Василий Ильич', 1, 'https://storage.example.com/avatars/vasily_m.png'),
    ('pavlova_t', 'tatyanaPavlova55', 'Павлова Татьяна Михайловна', 2, 'https://storage.example.com/avatars/tatyana_p.jpg');