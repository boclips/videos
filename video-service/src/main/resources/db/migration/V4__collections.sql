CREATE TABLE IF NOT EXISTS collection
(
  id VARCHAR(64) PRIMARY KEY NOT NULL,
  owner VARCHAR(64) NOT NULL,
  title VARCHAR(1024) NOT NULL
);

CREATE TABLE IF NOT EXISTS collection_video
(
  id INT PRIMARY KEY auto_increment,
  collection_id VARCHAR(64) NOT NULL,
  video_id INT NOT NULL,
  FOREIGN KEY (collection_id) REFERENCES collection(id) ON DELETE CASCADE,
  FOREIGN KEY (video_id) REFERENCES metadata_orig(id) ON DELETE CASCADE,
);