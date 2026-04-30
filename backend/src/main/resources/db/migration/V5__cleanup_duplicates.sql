-- Temporarily disabled: referenced tables (scores, quotes, liners, photos, events) don't exist yet.
-- TODO: Recreate cleanup logic after table creation.

-- Quotes: dedupe exact text+author duplicates
WITH dup AS (
  SELECT id FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY text, author ORDER BY id) AS rn
    FROM quotes
  ) t WHERE t.rn > 1
)
DELETE FROM quotes WHERE id IN (SELECT id FROM dup);

-- Liners: dedupe by number (keep lowest id)
WITH dup AS (
  SELECT id FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY number ORDER BY id) AS rn
    FROM liners
  ) t WHERE t.rn > 1
)
DELETE FROM liners WHERE id IN (SELECT id FROM dup);

-- Photos: dedupe by image_url
WITH dup AS (
  SELECT id FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY image_url ORDER BY id) AS rn
    FROM photos
  ) t WHERE t.rn > 1
)
DELETE FROM photos WHERE id IN (SELECT id FROM dup);

-- Events: dedupe by title + event_date
WITH dup AS (
  SELECT id FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY title, event_date ORDER BY id) AS rn
    FROM events
  ) t WHERE t.rn > 1
)
DELETE FROM events WHERE id IN (SELECT id FROM dup);

-- Note: child collections using ON DELETE CASCADE will be cleaned up automatically.
