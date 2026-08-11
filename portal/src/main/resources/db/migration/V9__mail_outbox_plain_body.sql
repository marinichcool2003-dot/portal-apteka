-- AUDIT-FIX: plain-text fallback для multipart email retry
ALTER TABLE mail_outbox ADD COLUMN IF NOT EXISTS body_plain TEXT;
