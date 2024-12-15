ALTER TABLE if exists PERSON ADD COLUMN IF NOT EXISTS in_search bool default false;
ALTER TABLE if exists PERSON ADD COLUMN IF NOT EXISTS in_search_from timestamptz;