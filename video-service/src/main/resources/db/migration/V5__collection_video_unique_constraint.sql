ALTER TABLE collection_video
ADD CONSTRAINT collection_video_unique UNIQUE (collection_id, video_id);