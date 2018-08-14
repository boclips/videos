CREATE TABLE IF NOT EXISTS SharedVideos
(
	Id INT auto_increment
		PRIMARY KEY,
	Member_Id INT NOT NULL,
	Item_Id longtext NULL,
	SharedItemRef longtext NULL,
	InTime INT NULL,
	OutTime INT NULL
);

CREATE INDEX IF NOT EXISTS IX_Member_Id
	on SharedVideos (Member_Id);

CREATE TABLE IF NOT EXISTS aboutuscontents
(
	Id INT auto_increment
		PRIMARY KEY,
	HtmlContent VARCHAR(20000) NULL,
	constraint Id_UNIQUE_1
		unique (Id)
);

CREATE TABLE IF NOT EXISTS analytics
(
	id INT unsigned auto_increment
		PRIMARY KEY,
	type VARCHAR(128) NULL,
	action VARCHAR(256) NULL,
	ip_address VARCHAR(128) NOT NULL,
	referrer VARCHAR(2048) NULL,
	orig_ref VARCHAR(2048) NOT NULL,
	timestamp timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	user_id VARCHAR(128) NOT NULL,
	organisation_id VARCHAR(128) NOT NULL,
	organisation_name VARCHAR(128) NULL,
	user_agent VARCHAR(2048) NOT NULL,
	ua_browser_name VARCHAR(128) NULL,
	ua_browser_version VARCHAR(128) NULL,
	ua_browser_major VARCHAR(128) NULL,
	ua_engine_name VARCHAR(128) NULL,
	ua_engine_version VARCHAR(128) NULL,
	ua_os_name VARCHAR(128) NULL,
	ua_os_version VARCHAR(128) NULL,
	ua_device_model VARCHAR(128) NULL,
	ua_device_vendor VARCHAR(128) NULL,
	ua_device_type VARCHAR(128) NULL,
	ua_cpu_architecture VARCHAR(128) NULL,
	geo_country VARCHAR(128) NULL,
	geo_region VARCHAR(128) NULL,
	geo_city VARCHAR(128) NULL,
	device VARCHAR(1024) NOT NULL,
	click_id VARCHAR(1024) NOT NULL,
	session_id VARCHAR(128) NULL,
	guid VARCHAR(256) NULL
);

CREATE TABLE IF NOT EXISTS boostedvalues
(
	Id INT auto_increment
		PRIMARY KEY,
	item_id VARCHAR(16) NOT NULL,
	module_id VARCHAR(16) NOT NULL,
	value double unsigned NOT NULL,
	constraint Id_UNIQUE_2
		unique (Id)
);

CREATE TABLE IF NOT EXISTS bopicksconfig
(
	Id INT auto_increment
		PRIMARY KEY,
	Pick1 VARCHAR(45) NULL,
	Pick2 VARCHAR(45) NULL,
	Pick3 VARCHAR(45) NULL
);

CREATE TABLE IF NOT EXISTS copy
(
	Id INT auto_increment
		PRIMARY KEY,
	CopyName VARCHAR(45) NOT NULL,
	CopyContent VARCHAR(5000) NULL,
	constraint Id_UNIQUE_3
		unique (Id)
);

CREATE TABLE IF NOT EXISTS curriculum
(
	id INT auto_increment
		PRIMARY KEY,
	name VARCHAR(45) not NULL
);

CREATE TABLE IF NOT EXISTS curriculum_data
(
	id INT auto_increment
		PRIMARY KEY,
	ukell VARCHAR(128) NULL,
	module_title VARCHAR(45) NULL,
	region_title VARCHAR(45) NOT NULL,
	authority_title VARCHAR(45) NOT NULL,
	level_title VARCHAR(45) NOT NULL,
	area VARCHAR(45) NOT NULL,
	module_content text NOT NULL,
	module_keywords text NULL,
	subject_title VARCHAR(45) NULL,
	topic_title VARCHAR(256) NULL,
	topic_keywords VARCHAR(256) NULL,
	editor VARCHAR(128) NULL,
	added_keywords text NULL,
	validated_keywords text NULL
);

CREATE INDEX IF NOT EXISTS IX_DropDowns
	on curriculum_data (subject_title, topic_title, module_title);

CREATE TABLE IF NOT EXISTS faqquestions
(
	Id INT auto_increment
		PRIMARY KEY,
	QuestionText VARCHAR(200) NULL,
	AnswerText VARCHAR(15000) NULL,
	constraint Id_UNIQUE_4
		unique (Id)
);

CREATE TABLE IF NOT EXISTS folders
(
	Id INT auto_increment
		PRIMARY KEY,
	Name longtext NULL,
	Member_Id INT NOT NULL,
	CreatedDate datetime NOT NULL,
	IsCollection bit DEFAULT 0 NULL,
	ThumbnailData longblob NULL,
	ThumbnailMime VARCHAR(80) NULL
);

CREATE TABLE IF NOT EXISTS foldervideos
(
	Id INT auto_increment
		PRIMARY KEY,
	Folder_Id INT NOT NULL,
	UserClip tinyint(1) NOT NULL,
	SortOrder INT NOT NULL,
	Metadata_Id INT NULL,
	InTime INT NULL,
	OutTime INT NULL,
	constraint `FK_dbo.FolderVideos_dbo.Folders_Folder_Id`
		foreign key (Folder_Id) references folders (Id)
			on delete cascade
);

CREATE INDEX IF NOT EXISTS IX_Folder_Id
	on foldervideos (Folder_Id);

CREATE TABLE IF NOT EXISTS gettyvideosources
(
	providerId VARCHAR(48) not NULL
		PRIMARY KEY,
	providerName VARCHAR(64) NOT NULL,
	downloadUrl VARCHAR(512) NULL,
	mbItemId VARCHAR(16) NOT NULL,
	downloaded bit DEFAULT 0 NOT NULL,
	newFileName VARCHAR(256) NULL,
	transcodesComplete bit DEFAULT 0 NOT NULL,
	locked bit DEFAULT 0 NOT NULL,
	constraint providerId_UNIQUE
		unique (providerId)
);

CREATE INDEX IF NOT EXISTS ix_mbitemId
	on gettyvideosources (mbItemId, transcodesComplete);

CREATE TABLE IF NOT EXISTS ingest_lookup
(
	id INT not NULL
		PRIMARY KEY,
	url VARCHAR(300) NULL
);

CREATE TABLE IF NOT EXISTS internal_sharedvideo
(
	Id INT auto_increment
		PRIMARY KEY,
  FromMember_Id INT NOT NULL,
	ToMember_Id INT NOT NULL,
	Metadata_Id INT NOT NULL,
  FolderVideo_Id INT NOT NULL,
	InTime INT NOT NULL,
	OutTime INT NOT NULL,
	DateCreated datetime NOT NULL,
	OriginalFolder_Id INT NULL,
	OriginalFolder_Name longtext NULL,
	ShareGroupKey VARCHAR(48) NULL,
	constraint Id_UNIQUE_5
		unique (Id)
);

CREATE TABLE IF NOT EXISTS kaltura_uploads
(
	id INT not NULL
		PRIMARY KEY,
	timestamp timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	status VARCHAR(12) NULL,
	action VARCHAR(12) NULL,
	url text NULL,
	tags text NULL,
	categories text NULL,
	constraint kaltura_uploads_id
		unique (id)
);

CREATE TABLE IF NOT EXISTS marketing_titles
(
	marketing_id INT not NULL
		PRIMARY KEY,
	title VARCHAR(256) NOT NULL,
	description text NULL
);

CREATE TABLE IF NOT EXISTS marketing_videos
(
	id INT auto_increment
		PRIMARY KEY,
	marketing_id INT NOT NULL,
	source text NOT NULL,
	title text NOT NULL,
	description text NOT NULL,
	entry_id VARCHAR(256) NOT NULL,
	reference_id VARCHAR(256) not NULL
);

CREATE TABLE IF NOT EXISTS mb_module_dump
(
	Id INT auto_increment
		PRIMARY KEY,
	module_id VARCHAR(16) NULL,
	module_title VARCHAR(512) NULL,
	module_content longtext NULL,
	region_id VARCHAR(16) NULL,
	region_title VARCHAR(512) NULL,
	authority_id VARCHAR(16) NULL,
	authority_title VARCHAR(512) NULL,
	area_id VARCHAR(16) NULL,
	area_title VARCHAR(512) NULL,
	level_id VARCHAR(16) NULL,
	level_title VARCHAR(512) NULL,
	topic_id VARCHAR(16) NULL,
	topic_title VARCHAR(512) NULL,
	subject_id VARCHAR(16) NULL,
	subject_title VARCHAR(512) NULL,
	item_id VARCHAR(16) NULL,
	in_point INT NOT NULL,
	out_point INT NOT NULL,
	confidence double NOT NULL,
	title VARCHAR(512) NULL,
	description longtext NULL,
	image_url VARCHAR(512) NULL,
	thumbnail_url VARCHAR(512) NULL,
	media_type VARCHAR(20) NULL,
	keywords_join varchar(999) NULL,
	record_hash VARCHAR(64) NULL,
	last_updated datetime DEFAULT '1900-01-01 00:00:00' NOT NULL,
	publisher_key VARCHAR(50) NULL,
	publisher_curie VARCHAR(50) NULL,
	constraint IX_OBJECT_HASH
		unique (Id, record_hash, last_updated),
	constraint IX_UPDATED
		unique (last_updated, Id)
);

CREATE INDEX IF NOT EXISTS IX_DROPDOWNS
	on mb_module_dump (level_id, level_title, topic_id, topic_title, subject_id, subject_title, Id, item_id);

CREATE INDEX IF NOT EXISTS IX_IMP
	on mb_module_dump (publisher_curie, Id);

CREATE INDEX IF NOT EXISTS IX_KEYWORDS_ITEM
	on mb_module_dump (item_id, keywords_join);

CREATE INDEX IF NOT EXISTS IX_KEYWORDS_MODULE
	on mb_module_dump (module_id, keywords_join);

CREATE INDEX IF NOT EXISTS IX_publisher
	on mb_module_dump (publisher_key, publisher_curie, item_id);

CREATE INDEX IF NOT EXISTS ix_mbitemId
	on mb_module_dump (item_id, subject_id, topic_id, level_id, area_id, region_id, module_id);

CREATE TABLE IF NOT EXISTS metadata
(
	id INT auto_increment
		PRIMARY KEY,
	source VARCHAR(45) NULL,
	unique_id text NULL,
	namespace text NULL,
	title text NULL,
	description text NULL,
	date date NULL,
	duration time NULL,
	keywords text NULL,
	price_category VARCHAR(45) NULL,
	sounds VARCHAR(45) NULL,
	color VARCHAR(45) NULL,
	location VARCHAR(45) NULL,
	country VARCHAR(45) NULL,
	state VARCHAR(45) NULL,
	city VARCHAR(45) NULL,
	region VARCHAR(45) NULL,
	alternative_id VARCHAR(45) NULL,
	alt_source VARCHAR(45) NULL,
	restrictions text NULL,
	mb_id VARCHAR(12) NULL
);

CREATE TABLE IF NOT EXISTS order_manifest
(
	id INT auto_increment
		PRIMARY KEY,
	order_no INT NOT NULL,
	order_date date NOT NULL,
	member_name VARCHAR(128) NOT NULL,
	member_id INT NOT NULL,
	clip_id VARCHAR(32) NOT NULL,
	title text NOT NULL,
	source VARCHAR(32) NOT NULL,
	license_duration INT NOT NULL,
	territory VARCHAR(32) NOT NULL,
	type VARCHAR(16) NOT NULL,
	publisher VARCHAR(32) NOT NULL,
	isbn VARCHAR(64) NULL,
	language VARCHAR(32) NULL,
	captioning VARCHAR(128) NULL,
	trim VARCHAR(32) NULL,
	notes text NOT NULL,
	additional_notes text NULL
);

CREATE TABLE IF NOT EXISTS organisations
(
	Id INT auto_increment
		PRIMARY KEY,
	CompanyName longtext NULL,
	AddressLine1 longtext NULL,
	AddressLine2 longtext NULL,
	AddressLine3 longtext NULL,
	TownCity longtext NULL,
	PostCode longtext NULL,
	Type1Credits INT NOT NULL,
	Type2Credits INT NOT NULL,
	Type3Credits INT NOT NULL,
	Type1AllocatedCredits INT NOT NULL,
	Type2AllocatedCredits INT NOT NULL,
	Type3AllocatedCredits INT not NULL
);

CREATE TABLE IF NOT EXISTS members
(
	Id INT auto_increment
		PRIMARY KEY,
	EmailAddress longtext NULL,
	FirstName longtext NULL,
	LastName longtext NULL,
	PasswordHash longtext NULL,
	PasswordSalt longtext NULL,
	MemberType tinyint unsigned NOT NULL,
	CreatedDate datetime NOT NULL,
	LastLoggedInDate datetime NULL,
	FailedLoginCount tinyint unsigned NOT NULL,
	EmailVerified tinyint(1) NOT NULL,
	EmailVerifiedDate datetime NULL,
	SetupProfile tinyint(1) NOT NULL,
	AgreedTerms tinyint(1) NOT NULL,
	PhoneNumber longtext NULL,
	Organisation_Id INT NULL,
	SchoolName longtext NULL,
	SchoolPostcode longtext NULL,
	GuardianNetworkId longtext NULL,
	ConfirmIsTeacher tinyint(1) NULL,
	IsPrimaryTeacher tinyint(1) NULL,
	discriminator VARCHAR(128) NULL,
	Deleted tinyint(1) unsigned DEFAULT 0 NULL,
	constraint `FK_dbo.Members_dbo.Organisations_Organisation_Id`
		foreign key (Organisation_Id) references organisations (Id)
			on delete cascade
);

CREATE TABLE IF NOT EXISTS baskets
(
	Id INT auto_increment
		PRIMARY KEY,
	Member_Id INT NOT NULL,
	constraint `FK_dbo.Baskets_dbo.Members_Member_Id`
		foreign key (Member_Id) references members (Id)
			on delete cascade
);

CREATE TABLE IF NOT EXISTS basketitems
(
	Id INT auto_increment
		PRIMARY KEY,
	PublisherBasket_Id INT NOT NULL,
	FolderVideo_Id INT NOT NULL,
	LicenseTermYears INT DEFAULT '0' NOT NULL,
	LicenseRegion VARCHAR(80) NOT NULL,
	InTime INT NULL,
	OutTime INT NULL,
	constraint `FK_dbo.BasketItems_dbo.Baskets_PublisherBasket_Id`
		foreign key (PublisherBasket_Id) references baskets (Id)
			on delete cascade,
	constraint `FK_dbo.BasketItems_dbo.FolderVideos_FolderVideo_Id`
		foreign key (FolderVideo_Id) references foldervideos (Id)
			on delete cascade
);

CREATE INDEX IF NOT EXISTS IX_FolderVideo_Id
	on basketitems (FolderVideo_Id);

CREATE INDEX IF NOT EXISTS IX_PublisherBasket_Id
	on basketitems (PublisherBasket_Id);

CREATE INDEX IF NOT EXISTS IX_Member_Id
	on baskets (Member_Id);

CREATE TABLE IF NOT EXISTS emailverificationtokens
(
	Id INT auto_increment
		PRIMARY KEY,
	VerificationToken longtext NULL,
	ApplyNewEmailAddress longtext NULL,
	ValidUntil datetime NOT NULL,
	Member_Id INT NULL,
	constraint `FK_dbo.EmailVerificationTokens_dbo.Members_Member_Id`
		foreign key (Member_Id) references members (Id)
);

CREATE INDEX IF NOT EXISTS IX_Member_Id
	on emailverificationtokens (Member_Id);

CREATE INDEX IF NOT EXISTS IX_Organisation_Id
	on members (Organisation_Id);

CREATE TABLE IF NOT EXISTS orders
(
	Id INT auto_increment
		PRIMARY KEY,
	OrderRef longtext NULL,
	Member_Id INT NOT NULL,
	OrderedDate datetime NOT NULL,
	PaymentReceived tinyint(1) NOT NULL,
	Delivered tinyint(1) NOT NULL,
	ISBN VARCHAR(20) NULL,
	constraint `FK_dbo.Orders_dbo.Members_Member_Id`
		foreign key (Member_Id) references members (Id)
			on delete cascade
);

CREATE TABLE IF NOT EXISTS orderitems
(
	Id INT auto_increment
		PRIMARY KEY,
	Item_Id longtext NULL,
	InTime INT NOT NULL,
	OutTime INT NOT NULL,
	UserClip tinyint(1) NOT NULL,
	Order_Id INT NOT NULL,
	LicenseTermYears INT DEFAULT '0' NULL,
	LicenseRegion VARCHAR(80) NULL,
	Publisher VARCHAR(80) NULL,
	PriceCategory VARCHAR(45) NULL,
	constraint `FK_dbo.OrderItems_dbo.Orders_Order_Id`
		foreign key (Order_Id) references orders (Id)
			on delete cascade
);

CREATE INDEX IF NOT EXISTS IX_Order_Id
	on orderitems (Order_Id);

CREATE INDEX IF NOT EXISTS IX_Member_Id
	on orders (Member_Id);

CREATE TABLE IF NOT EXISTS portalusers
(
	id INT auto_increment
		PRIMARY KEY,
	username VARCHAR(256) NOT NULL,
	password VARCHAR(256) NOT NULL,
	firstname VARCHAR(256) NOT NULL,
	lastname VARCHAR(256) NOT NULL,
	email VARCHAR(512) NOT NULL,
	tel VARCHAR(32) NOT NULL,
	role VARCHAR(16) not NULL
);

CREATE TABLE IF NOT EXISTS producermodulestate
(
	Id INT auto_increment
		PRIMARY KEY,
	ModuleId VARCHAR(20) NOT NULL,
	IsComplete bit DEFAULT 0 NULL,
	constraint Id_UNIQUE_6
		unique (Id)
);

CREATE INDEX IF NOT EXISTS IX_MOD
	on producermodulestate (ModuleId, IsComplete) ;

CREATE TABLE IF NOT EXISTS raw_data
(
	id INT auto_increment
		PRIMARY KEY,
	source VARCHAR(45) NULL,
	timestamp timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	type VARCHAR(12) NULL,
	status VARCHAR(12) NULL,
	data longblob NULL,
	constraint raw_data_id
		unique (id)
);

CREATE TABLE IF NOT EXISTS report_asset
(
	id INT auto_increment
		PRIMARY KEY,
	unique_id VARCHAR(256) NULL,
	source VARCHAR(128) NULL,
	title text NULL,
	description text NULL,
	kaltura_entry_id VARCHAR(128) NULL,
	metadata_id INT NULL,
	mb_id VARCHAR(128) NULL,
	in_kaltura tinyint(1) DEFAULT '0' NOT NULL,
	in_kaltura_with_mb_id tinyint(1) DEFAULT '0' NOT NULL,
	in_kaltura_with_incorrect_id tinyint(1) DEFAULT '0' NOT NULL,
	in_kaltura_with_id tinyint(1) DEFAULT '0' NOT NULL,
	in_metadata tinyint(1) DEFAULT '0' NOT NULL,
	have_asset_s3 tinyint(1) DEFAULT '0' NOT NULL,
	filename VARCHAR(512) NULL,
	constraint report_asset_id
		unique (id),
	constraint metadata_id_2
		unique (metadata_id)
);

CREATE INDEX IF NOT EXISTS metadata_id
	on report_asset (metadata_id) ;

CREATE TABLE IF NOT EXISTS subjects
(
	id INT auto_increment
		PRIMARY KEY,
	name VARCHAR(512) NULL,
	curriculum_id INT NOT NULL,
	constraint fk_subject_1
		foreign key (curriculum_id) references curriculum (id)
);

CREATE TABLE IF NOT EXISTS levels
(
	id INT auto_increment
		PRIMARY KEY,
	name VARCHAR(512) NULL,
	subject_id INT NOT NULL,
	constraint fk_level_1
		foreign key (subject_id) references subjects (id)
);

CREATE INDEX IF NOT EXISTS fk_level_1_idx
	on levels (subject_id) ;

CREATE INDEX IF NOT EXISTS fk_subject_1_idx
	on subjects (curriculum_id);

CREATE TABLE IF NOT EXISTS syslog
(
	Id bigint auto_increment
		PRIMARY KEY,
	EventType smallint(6) NOT NULL,
	PrimaryObjectId INT NOT NULL,
	SecondaryObjectId INT NOT NULL,
	RawDetails longtext NULL,
	EventDateTime datetime NOT NULL,
	Member_Id INT NULL
);

CREATE INDEX IF NOT EXISTS IX_Member_Id
	on syslog (Member_Id);

CREATE TABLE IF NOT EXISTS teacherlevels
(
	Id INT auto_increment
		PRIMARY KEY,
	Level_MbId longtext NULL,
	Member_Id INT NULL,
	constraint `FK_dbo.TeacherLevels_dbo.Members_Member_Id`
		foreign key (Member_Id) references members (Id)
);

CREATE INDEX IF NOT EXISTS IX_Member_Id
	on teacherlevels (Member_Id);

CREATE TABLE IF NOT EXISTS teachersubjects
(
	Id INT auto_increment
		PRIMARY KEY,
	Subject_MbId longtext NULL,
	Member_Id INT NULL,
	constraint `FK_dbo.TeacherSubjects_dbo.Members_Member_Id`
		foreign key (Member_Id) references members (Id)
);

CREATE INDEX IF NOT EXISTS IX_Member_Id
	on teachersubjects (Member_Id);

CREATE TABLE IF NOT EXISTS tiny_url
(
	id INT auto_increment
		PRIMARY KEY,
	uuid VARCHAR(128) NOT NULL,
	url VARCHAR(2048) NOT NULL,
	constraint uuid
		unique (uuid)
);

CREATE TABLE IF NOT EXISTS topics
(
	id INT auto_increment
		PRIMARY KEY,
	name VARCHAR(512) NULL,
	level_id INT NOT NULL,
	constraint fk_topic_1
		foreign key (level_id) references levels (id)
);

CREATE TABLE IF NOT EXISTS keywords
(
	id INT auto_increment
		PRIMARY KEY,
	module_content text NULL,
	validated_keywords text NULL,
	topic_id INT NOT NULL,
	constraint fk_keywords_1
		foreign key (topic_id) references topics (id)
);

CREATE INDEX IF NOT EXISTS fk_keywords_1_idx
	on keywords (topic_id);

CREATE INDEX IF NOT EXISTS fk_topic_1_idx
	on topics (level_id);

CREATE TABLE IF NOT EXISTS type
(
	id INT not NULL
		PRIMARY KEY,
	name VARCHAR(64) NOT NULL,
	displayOrder INT NULL
);

CREATE TABLE IF NOT EXISTS metadata_orig
(
	id INT auto_increment
		PRIMARY KEY,
	source VARCHAR(45) NULL,
	unique_id mediumtext NULL,
	namespace mediumtext NULL,
	title mediumtext NULL,
	description mediumtext NULL,
	date date NULL,
	duration VARCHAR(12) NULL,
	keywords mediumtext NULL,
	price_category VARCHAR(45) NULL,
	sounds VARCHAR(45) NULL,
	color VARCHAR(45) NULL,
	location VARCHAR(45) NULL,
	country VARCHAR(45) NULL,
	state VARCHAR(45) NULL,
	city VARCHAR(45) NULL,
	region VARCHAR(45) NULL,
	alternative_id VARCHAR(45) NULL,
	alt_source VARCHAR(45) NULL,
	restrictions mediumtext NULL,
	type_id INT DEFAULT '0' NOT NULL,
	constraint metadata_orig_id
		unique (id),
	constraint fk_type_id
		foreign key (type_id) references type (id)
);

CREATE INDEX IF NOT EXISTS IX_PUBLISHER
	on metadata_orig (unique_id, source);

CREATE INDEX IF NOT EXISTS fk_type_id
	on metadata_orig (type_id);

CREATE INDEX IF NOT EXISTS id_2
	on metadata_orig (id);

CREATE INDEX IF NOT EXISTS idx_source
	on metadata_orig (source);

