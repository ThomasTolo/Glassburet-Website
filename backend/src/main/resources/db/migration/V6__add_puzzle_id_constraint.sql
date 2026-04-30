-- Add unique constraint for puzzle-based scores (when puzzle_id is NOT NULL)
-- This allows users to play the same puzzle only once, while keeping
-- the game_date constraint for daily games (where puzzle_id IS NULL)

CREATE UNIQUE INDEX IF NOT EXISTS uq_scores_member_game_puzzle
  ON scores (member_name, game_name, puzzle_id)
  WHERE puzzle_id IS NOT NULL;
