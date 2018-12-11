CREATE TABLE IF NOT EXISTS video_subject
(
  video_id INT NOT NULL,
  subject_name VARCHAR(100) NOT NULL,
  FOREIGN KEY (video_id) REFERENCES metadata_orig(id) ON DELETE CASCADE,
  PRIMARY KEY (video_id, subject_name)
);