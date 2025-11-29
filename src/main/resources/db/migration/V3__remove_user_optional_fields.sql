-- Remove optional user fields: bio, avatar_url, gamer_tags, social_links
ALTER TABLE users 
    DROP COLUMN avatar_url,
    DROP COLUMN bio,
    DROP COLUMN gamer_tags,
    DROP COLUMN social_links;
