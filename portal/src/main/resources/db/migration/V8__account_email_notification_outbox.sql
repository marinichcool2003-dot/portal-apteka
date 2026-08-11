-- AUDIT-FIX: email, notification preferences and mail outbox

-- 1. ADD account.email VARCHAR(255) UNIQUE (nullable first)
ALTER TABLE account ADD COLUMN IF NOT EXISTS email VARCHAR(255);

-- 2. Backfill emails
-- Clients (employees): user1@farmp.ru, user2@farmp.ru, ...
WITH numbered_clients AS (
    SELECT a.id, ROW_NUMBER() OVER (ORDER BY a.login) AS rn
    FROM account a
    INNER JOIN client c ON c.id = a.id
)
UPDATE account
SET email = 'user' || numbered_clients.rn || '@farmp.ru'
FROM numbered_clients
WHERE account.id = numbered_clients.id
  AND account.email IS NULL;

-- Aptekas: apteka1@farmp.ru, apteka2@farmp.ru, ...
WITH numbered_aptekas AS (
    SELECT a.id, ROW_NUMBER() OVER (ORDER BY a.login) AS rn
    FROM account a
    INNER JOIN apteka ap ON ap.id = a.id
)
UPDATE account
SET email = 'apteka' || numbered_aptekas.rn || '@farmp.ru'
FROM numbered_aptekas
WHERE account.id = numbered_aptekas.id
  AND account.email IS NULL;

-- Remaining accounts (neither client nor apteka): account1@farmp.ru, ...
WITH numbered_remaining AS (
    SELECT a.id, ROW_NUMBER() OVER (ORDER BY a.login) AS rn
    FROM account a
    LEFT JOIN client c ON c.id = a.id
    LEFT JOIN apteka ap ON ap.id = a.id
    WHERE c.id IS NULL AND ap.id IS NULL
)
UPDATE account
SET email = 'account' || numbered_remaining.rn || '@farmp.ru'
FROM numbered_remaining
WHERE account.id = numbered_remaining.id
  AND account.email IS NULL;

-- 3. ALTER email SET NOT NULL
ALTER TABLE account ALTER COLUMN email SET NOT NULL;
ALTER TABLE account ADD CONSTRAINT account_email_unique UNIQUE (email);

-- 4. notification_preference table
CREATE TABLE IF NOT EXISTS notification_preference (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES account(id) ON DELETE CASCADE,
    channel VARCHAR(32) NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT false,
    UNIQUE(account_id, channel, event_type)
);

CREATE INDEX IF NOT EXISTS idx_notification_preference_account_id ON notification_preference(account_id);

-- 5. mail_outbox table
CREATE TABLE IF NOT EXISTS mail_outbox (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    to_email VARCHAR(255) NOT NULL,
    subject VARCHAR(512) NOT NULL,
    body_html TEXT NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    attempts INT NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ,
    last_error TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    event_type VARCHAR(64)
);

CREATE INDEX IF NOT EXISTS idx_mail_outbox_status_next_attempt ON mail_outbox(status, next_attempt_at);
