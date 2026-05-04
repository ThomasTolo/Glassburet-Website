-- Add nullable image_url column to quotes for optional quote images
ALTER TABLE quotes
  ADD COLUMN IF NOT EXISTS image_url VARCHAR(255);

-- No unique constraint required; images may be reused across quotes.
