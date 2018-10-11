create table if not exists metadata_orig
(
	  id             int auto_increment
    primary key,
  source         varchar(45)     null,
  unique_id      mediumtext      null,
  namespace      mediumtext      null,
  title          mediumtext      null,
  description    mediumtext      null,
  date           date            null,
  duration       varchar(12)     null,
  keywords       mediumtext      null,
  price_category varchar(45)     null,
  sounds         varchar(45)     null,
  color          varchar(45)     null,
  location       varchar(45)     null,
  country        varchar(45)     null,
  state          varchar(45)     null,
  city           varchar(45)     null,
  region         varchar(45)     null,
  alternative_id varchar(45)     null,
  alt_source     varchar(45)     null,
  reference_id   varchar(45)     not null,
  restrictions   mediumtext      null,
  type_id        int default '0' not null
);

