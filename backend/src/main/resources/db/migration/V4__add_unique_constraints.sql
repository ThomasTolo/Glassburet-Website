-- Deduplicate existing rows before adding unique constraints/indexes.
-- Keep the row with the smallest id for each duplicate group.

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

CREATE TABLE IF NOT EXISTS quote_likes (
  quote_id BIGINT NOT NULL REFERENCES quotes(id) ON DELETE CASCADE,
  member_name VARCHAR(255) NOT NULL REFERENCES members(name) ON DELETE CASCADE,
  PRIMARY KEY (quote_id, member_name)
);

CREATE TABLE IF NOT EXISTS liners (
  id BIGSERIAL PRIMARY KEY,
  number INT NOT NULL,
  text TEXT NOT NULL,
  said_count INT NOT NULL DEFAULT 0,
  since_year INT,
  author VARCHAR(255),
  CONSTRAINT uq_liners_number UNIQUE (number)
);

CREATE TABLE IF NOT EXISTS liner_likes (
  liner_id BIGINT NOT NULL REFERENCES liners(id) ON DELETE CASCADE,
  member_name VARCHAR(255) NOT NULL REFERENCES members(name) ON DELETE CASCADE,
  PRIMARY KEY (liner_id, member_name)
);

CREATE TABLE IF NOT EXISTS photos (
  id BIGSERIAL PRIMARY KEY,
  caption VARCHAR(255),
  image_url VARCHAR(255) NOT NULL,
  album VARCHAR(255) NOT NULL DEFAULT 'Glassburet',
  category VARCHAR(255) NOT NULL DEFAULT 'Alt',
  uploaded_by VARCHAR(255),
  uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_photos_image_url UNIQUE (image_url)
);

CREATE TABLE IF NOT EXISTS photo_likes (
  photo_id BIGINT NOT NULL REFERENCES photos(id) ON DELETE CASCADE,
  member_name VARCHAR(255) NOT NULL REFERENCES members(name) ON DELETE CASCADE,
  PRIMARY KEY (photo_id, member_name)
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

CREATE TABLE IF NOT EXISTS event_attendees (
  event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
  member_name VARCHAR(255) NOT NULL REFERENCES members(name) ON DELETE CASCADE,
  PRIMARY KEY (event_id, member_name)
);

CREATE TABLE IF NOT EXISTS wordle_puzzles (
  id BIGSERIAL PRIMARY KEY,
  word VARCHAR(20) NOT NULL,
  created_by VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_daily BOOLEAN DEFAULT FALSE,
  puzzle_date DATE
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_wordle_puzzles_daily
  ON wordle_puzzles (puzzle_date)
  WHERE is_daily = TRUE;

CREATE TABLE IF NOT EXISTS connections_puzzles (
  id BIGSERIAL PRIMARY KEY,
  created_by VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_daily BOOLEAN DEFAULT FALSE,
  puzzle_date DATE,
  group0_category VARCHAR(255) NOT NULL,
  group0_words TEXT NOT NULL,
  group1_category VARCHAR(255) NOT NULL,
  group1_words TEXT NOT NULL,
  group2_category VARCHAR(255) NOT NULL,
  group2_words TEXT NOT NULL,
  group3_category VARCHAR(255) NOT NULL,
  group3_words TEXT NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_connections_puzzles_daily
  ON connections_puzzles (puzzle_date)
  WHERE is_daily = TRUE;

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