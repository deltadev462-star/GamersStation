-- Complete the V4 migration manually

-- Drop remaining FK to allow index modification
ALTER TABLE comments DROP FOREIGN KEY fk_comment_post;

-- Drop and recreate index for comments table
ALTER TABLE comments DROP INDEX idx_ad_created;
ALTER TABLE comments ADD INDEX idx_post_created (post_id, created_at DESC);

-- Add index for cursor-based pagination
ALTER TABLE comments ADD INDEX idx_post_id_cursor (post_id, id DESC);

-- Recreate the foreign key
ALTER TABLE comments 
    ADD CONSTRAINT fk_comment_post 
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE;
