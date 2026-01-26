TRUNCATE TABLE apteka RESTART IDENTITY CASCADE;

INSERT INTO apteka (login, password, number, adress, phone_number, group_id) VALUES
    ('apteka1', 'pass123', 101, 'ул. Ленина, 10', '9012345678', 1),
    ('pharm_city', 'pharm456', 102, 'пр. Победы, 25', '9023456789', 2),
    ('zdorovye', 'health789', 103, 'ул. Центральная, 5', '9034567890', 1),
    ('medexpress', 'express12', 104, 'ул. Садовая, 15', '9045678901', 3),
    ('apteka24', '24hours34', 105, 'пр. Мира, 42', '9056789012', 2),
    ('pharmacyplus', 'plus5678', 106, 'ул. Молодёжная, 8', '9067890123', 1),
    ('medservice', 'service90', 107, 'ул. Гагарина, 33', '9078901234', 4),
    ('aptekavita', 'vita1234', 108, 'ул. Лесная, 12', '9089012345', 3),
    ('pharmalux', 'lux56789', 109, 'пр. Строителей, 7', '9090123456', 2),
    ('medicina', 'med2023', 110, 'ул. Школьная, 20', '9001234567', 1);