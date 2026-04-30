CREATE TABLE IF NOT EXISTS native_puzzles (
  id BIGSERIAL PRIMARY KEY,
  game_name VARCHAR(50) NOT NULL,
  created_by VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_daily BOOLEAN DEFAULT FALSE,
  puzzle_date DATE,
  payload_json TEXT NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_native_puzzles_daily
  ON native_puzzles (game_name, puzzle_date)
  WHERE is_daily = true;
