SET SQL_SAFE_UPDATES = 0;
DELETE FROM SharedVideos;
DELETE FROM aboutuscontents;
DELETE FROM analytics;
DELETE FROM baskets;
DELETE FROM basketitems;
DELETE FROM boostedvalues;
DELETE FROM bopicksconfig;
DELETE FROM copy;
DELETE FROM keywords;
DELETE FROM topics;
DELETE FROM levels;
DELETE FROM subjects;
DELETE FROM curriculum;
DELETE FROM curriculum_data;
DELETE FROM emailverificationtokens;
DELETE FROM faqquestions;
DELETE FROM folders;
DELETE FROM foldervideos;
DELETE FROM gettyvideosources;
DELETE FROM ingest_lookup;
DELETE FROM internal_sharedvideo;
DELETE FROM kaltura_uploads;
DELETE FROM marketing_titles;
DELETE FROM marketing_videos;
TRUNCATE TABLE mb_module_dump;
DELETE FROM members;
DELETE FROM metadata;
TRUNCATE TABLE metadata_orig;
DELETE FROM order_manifest;
DELETE FROM orderitems;
DELETE FROM orders;
DELETE FROM organisations;
DELETE FROM portalusers;
DELETE FROM producermodulestate;
TRUNCATE TABLE raw_data;
TRUNCATE TABLE report_asset;
TRUNCATE TABLE syslog;
DELETE FROM teacherlevels;
DELETE FROM teachersubjects;
TRUNCATE TABLE tiny_url;
TRUNCATE TABLE metadata_orig;

DELETE FROM type;
INSERT INTO type (id, name, displayOrder) VALUES (0, 'Other', 4);
INSERT INTO type (id, name, displayOrder) VALUES (1, 'News', 1);
INSERT INTO type (id, name, displayOrder) VALUES (2, 'Stock', 0);
INSERT INTO type (id, name, displayOrder) VALUES (3, 'Instructional Clips', 2);
INSERT INTO type (id, name, displayOrder) VALUES (4, 'TV Clips', 3);
INSERT INTO type (id, name, displayOrder) VALUES (5, 'News Package', 5);
INSERT INTO type (id, name, displayOrder) VALUES (6, 'UGC News', 6);
INSERT INTO type (id, name, displayOrder) VALUES (7, '360 VR Stock', 7);
INSERT INTO type (id, name, displayOrder) VALUES (8, '360 VR Immersive', 8);
INSERT INTO type (id, name, displayOrder) VALUES (9, 'Short Programme', 9);

ALTER TABLE metadata_orig AUTO_INCREMENT=10;