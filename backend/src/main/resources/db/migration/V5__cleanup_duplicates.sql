-- Ensure tables exist before deduplicating (V4 may have been applied with
-- older SQL that did not include CREATE TABLE statements).

CREATE TABLE IF NOT EXISTS scores (
  id BIGSERIAL PRIMARY KEY,
  member_name VARCHAR(255) NOT NULL REFERENCES members(name) ON DELETE CASCADE,
  game_name VARCHAR(50) NOT NULL,
  score_value INT NOT NULL,
  game_date DATE NOT NULL,
  puzzle_id BIGINT,
  submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS quotes (
  id BIGSERIAL PRIMARY KEY,
  text TEXT NOT NULL,
  author VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  featured BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS liners (
  id BIGSERIAL PRIMARY KEY,
  number INT NOT NULL,
  text TEXT NOT NULL,
  said_count INT NOT NULL DEFAULT 0,
  since_year INT,
  author VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS photos (
  id BIGSERIAL PRIMARY KEY,
  caption VARCHAR(255),
  image_url VARCHAR(255) NOT NULL,
  album VARCHAR(255) NOT NULL DEFAULT 'Glassburet',
  category VARCHAR(255) NOT NULL DEFAULT 'Alt',
  uploaded_by VARCHAR(255),
  uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS events (
  id BIGSERIAL PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  event_date DATE NOT NULL,
  time_start TIME,
  time_end TIME,
  location VARCHAR(255),
  category VARCHAR(50) NOT NULL DEFAULT 'SOSIALT'
);

-- Deduplicate rows that would violate the unique indexes added later in V4/V6.
WITH dup AS (
  SELECT id FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY member_name, game_name, game_date ORDER BY id) AS rn
    FROM scores
  ) t WHERE t.rn > 1
)
DELETE FROM scores WHERE id IN (SELECT id FROM dup);

WITH dup AS (
  SELECT id FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY text, author ORDER BY id) AS rn
    FROM quotes
  ) t WHERE t.rn > 1
)
DELETE FROM quotes WHERE id IN (SELECT id FROM dup);

WITH dup AS (
  SELECT id FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY number ORDER BY id) AS rn
    FROM liners
  ) t WHERE t.rn > 1
)
DELETE FROM liners WHERE id IN (SELECT id FROM dup);

WITH dup AS (
  SELECT id FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY image_url ORDER BY id) AS rn
    FROM photos
  ) t WHERE t.rn > 1
)
DELETE FROM photos WHERE id IN (SELECT id FROM dup);

WITH dup AS (
  SELECT id FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY title, event_date ORDER BY id) AS rn
    FROM events
  ) t WHERE t.rn > 1
)
DELETE FROM events WHERE id IN (SELECT id FROM dup);
