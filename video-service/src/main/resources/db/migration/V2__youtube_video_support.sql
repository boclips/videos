ALTER TABLE metadata_orig
  ADD COLUMN playback_provider VARCHAR(45) NULL;

ALTER TABLE metadata_orig
  ADD COLUMN playback_id VARCHAR(45) NULL;

ALTER TABLE metadata_orig
  ADD INDEX playback_provider (playback_provider);

UPDATE metadata_orig
SET playback_provider = 'KALTURA',
    playback_id       = reference_id;

ALTER TABLE metadata_orig
  CHANGE COLUMN playback_provider playback_provider VARCHAR(45) NOT NULL;

ALTER TABLE metadata_orig
  CHANGE COLUMN playback_id playback_id VARCHAR(45) NOT NULL;
