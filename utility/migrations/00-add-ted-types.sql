INSERT INTO type (id, name, displayOrder) VALUES (10, 'TED Talks', 10);
INSERT INTO type (id, name, displayOrder) VALUES (11, 'TED-Ed', 11);

UPDATE metadata_orig SET type_id = 10 WHERE source = 'TED Talks';
UPDATE metadata_orig SET type_id = 11 WHERE source = 'TED-Ed';
UPDATE metadata_orig SET type_id = 9 WHERE source = 'The School of Life';
