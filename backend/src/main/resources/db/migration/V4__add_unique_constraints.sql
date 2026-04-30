-- Deduplicate existing rows before adding unique constraints/indexes.
-- Keep the row with the smallest id for each duplicate group.

WITH dup_scores AS (
  SELECT id FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY member_name, game_name, game_date ORDER BY id) AS rn
    FROM scores
  ) t WHERE t.rn > 1
)
DELETE FROM scores WHERE id IN (SELECT id FROM dup_scores);

WITH dup_quotes AS (
  SELECT id FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY text, author ORDER BY id) AS rn
    FROM quotes
  ) t WHERE t.rn > 1
)
DELETE FROM quotes WHERE id IN (SELECT id FROM dup_quotes);

WITH dup_liners AS (
  SELECT id FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY number ORDER BY id) AS rn
    FROM liners
  ) t WHERE t.rn > 1
)
DELETE FROM liners WHERE id IN (SELECT id FROM dup_liners);

WITH dup_photos AS (
  SELECT id FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY image_url ORDER BY id) AS rn
    FROM photos
  ) t WHERE t.rn > 1
)
DELETE FROM photos WHERE id IN (SELECT id FROM dup_photos);

WITH dup_events AS (
  SELECT id FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY title, event_date ORDER BY id) AS rn
    FROM events
  ) t WHERE t.rn > 1
)
DELETE FROM events WHERE id IN (SELECT id FROM dup_events);

-- Scores: ensure only one score per member/game/date
CREATE UNIQUE INDEX IF NOT EXISTS uq_scores_member_game_date
  ON scores (member_name, game_name, game_date);

-- Liners: ensure number uniqueness (prevents duplicates if JPA/DDL wasn't applied)
CREATE UNIQUE INDEX IF NOT EXISTS uq_liners_number
  ON liners (number);

-- Quotes: prevent exact duplicate quote text/author pairs
CREATE UNIQUE INDEX IF NOT EXISTS uq_quotes_text_author
  ON quotes (text, author);

-- Photos: avoid storing the same image URL multiple times
CREATE UNIQUE INDEX IF NOT EXISTS uq_photos_image_url
  ON photos (image_url);

-- Events: prevent duplicate titled events on the same date
CREATE UNIQUE INDEX IF NOT EXISTS uq_events_title_date
  ON events (title, event_date);