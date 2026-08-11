-- AUDIT-FIX: добавляем тип группы (аптеки / сотрудники) для выборки из БД
ALTER TABLE group_user
    ADD COLUMN IF NOT EXISTS group_type VARCHAR(32);

-- AUDIT-FIX: backfill — группы только с аптеками (без client) → APTEKA_GROUP, иначе EMPLOYEE_GROUP
UPDATE group_user gu
SET group_type = 'APTEKA_GROUP'
WHERE group_type IS NULL
  AND EXISTS (
      SELECT 1
      FROM account a
      INNER JOIN apteka ap ON ap.id = a.id
      WHERE a.group_id = gu.id
  )
  AND NOT EXISTS (
      SELECT 1
      FROM account a
      INNER JOIN client c ON c.id = a.id
      WHERE a.group_id = gu.id
  );

UPDATE group_user
SET group_type = 'EMPLOYEE_GROUP'
WHERE group_type IS NULL;

ALTER TABLE group_user
    ALTER COLUMN group_type SET NOT NULL;

ALTER TABLE group_user
    ALTER COLUMN group_type SET DEFAULT 'EMPLOYEE_GROUP';
