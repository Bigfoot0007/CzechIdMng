--
-- CzechIdM 9.0 Flyway script 
-- BCV solutions s.r.o.
--
-- This is initial SQL script for SQL server


CREATE TABLE logging_event 
  ( 
    timestmp         DECIMAL(20) NOT NULL,
   	formatted_message  NVARCHAR(4000) NOT NULL,
    logger_name       NVARCHAR(254) NOT NULL,
    level_string      NVARCHAR(254) NOT NULL,
    thread_name       NVARCHAR(254),
    reference_flag    SMALLINT,
    arg0              NVARCHAR(254),
    arg1              NVARCHAR(254),
    arg2              NVARCHAR(254),
    arg3              NVARCHAR(254),
    caller_filename   NVARCHAR(254) NOT NULL,
    caller_class      NVARCHAR(254) NOT NULL,
    caller_method     NVARCHAR(254) NOT NULL,
    caller_line       CHAR(4) NOT NULL,
    event_id          DECIMAL(38) NOT NULL identity,
    PRIMARY KEY(event_id) 
  ) 

CREATE TABLE logging_event_property 
  ( 
    event_id          DECIMAL(38) NOT NULL, 
    mapped_key        NVARCHAR(254) NOT NULL, 
    mapped_value      NVARCHAR(1024), 
    PRIMARY KEY(event_id, mapped_key), 
    FOREIGN KEY (event_id) REFERENCES logging_event(event_id) 
  ) 

CREATE TABLE logging_event_exception 
  ( 
    event_id         DECIMAL(38) NOT NULL, 
    i                SMALLINT NOT NULL, 
    trace_line       NVARCHAR(254) NOT NULL, 
    PRIMARY KEY(event_id, i), 
    FOREIGN KEY (event_id) REFERENCES logging_event(event_id) 
  ) 
  
 -- QUARTZ TABLES
  
  

CREATE TABLE [QRTZ_CALENDARS] (
  [SCHED_NAME] [NVARCHAR] (120)  NOT NULL ,
  [CALENDAR_NAME] [NVARCHAR] (200)  NOT NULL ,
  [CALENDAR] [VARBINARY](MAX) NOT NULL
)
GO

CREATE TABLE [QRTZ_CRON_TRIGGERS] (
  [SCHED_NAME] [NVARCHAR] (120)  NOT NULL ,
  [TRIGGER_NAME] [NVARCHAR] (150)  NOT NULL ,
  [TRIGGER_GROUP] [NVARCHAR] (150)  NOT NULL ,
  [CRON_EXPRESSION] [NVARCHAR] (120)  NOT NULL ,
  [TIME_ZONE_ID] [NVARCHAR] (80) 
)
GO

CREATE TABLE [QRTZ_FIRED_TRIGGERS] (
  [SCHED_NAME] [NVARCHAR] (120)  NOT NULL ,
  [ENTRY_ID] [NVARCHAR] (140)  NOT NULL ,
  [TRIGGER_NAME] [NVARCHAR] (150)  NOT NULL ,
  [TRIGGER_GROUP] [NVARCHAR] (150)  NOT NULL ,
  [INSTANCE_NAME] [NVARCHAR] (200)  NOT NULL ,
  [FIRED_TIME] [BIGINT] NOT NULL ,
  [SCHED_TIME] [BIGINT] NOT NULL ,
  [PRIORITY] [INTEGER] NOT NULL ,
  [STATE] [NVARCHAR] (16)  NOT NULL,
  [JOB_NAME] [NVARCHAR] (150)  NULL ,
  [JOB_GROUP] [NVARCHAR] (150)  NULL ,
  [IS_NONCONCURRENT] BIT  NULL ,
  [REQUESTS_RECOVERY] BIT  NULL 
)
GO

CREATE TABLE [QRTZ_PAUSED_TRIGGER_GRPS] (
  [SCHED_NAME] [NVARCHAR] (120)  NOT NULL ,
  [TRIGGER_GROUP] [NVARCHAR] (150)  NOT NULL 
)
GO

CREATE TABLE [QRTZ_SCHEDULER_STATE] (
  [SCHED_NAME] [NVARCHAR] (120)  NOT NULL ,
  [INSTANCE_NAME] [NVARCHAR] (200)  NOT NULL ,
  [LAST_CHECKIN_TIME] [BIGINT] NOT NULL ,
  [CHECKIN_INTERVAL] [BIGINT] NOT NULL
)
GO

CREATE TABLE [QRTZ_LOCKS] (
  [SCHED_NAME] [NVARCHAR] (120)  NOT NULL ,
  [LOCK_NAME] [NVARCHAR] (40)  NOT NULL 
)
GO

CREATE TABLE [QRTZ_JOB_DETAILS] (
  [SCHED_NAME] [NVARCHAR] (120)  NOT NULL ,
  [JOB_NAME] [NVARCHAR] (150)  NOT NULL ,
  [JOB_GROUP] [NVARCHAR] (150)  NOT NULL ,
  [DESCRIPTION] [NVARCHAR] (250) NULL ,
  [JOB_CLASS_NAME] [NVARCHAR] (250)  NOT NULL ,
  [IS_DURABLE] BIT  NOT NULL ,
  [IS_NONCONCURRENT] BIT  NOT NULL ,
  [IS_UPDATE_DATA] BIT  NOT NULL ,
  [REQUESTS_RECOVERY] BIT  NOT NULL ,
  [JOB_DATA] [VARBINARY](MAX) NULL
)
GO

CREATE TABLE [QRTZ_SIMPLE_TRIGGERS] (
  [SCHED_NAME] [NVARCHAR] (120)  NOT NULL ,
  [TRIGGER_NAME] [NVARCHAR] (150)  NOT NULL ,
  [TRIGGER_GROUP] [NVARCHAR] (150)  NOT NULL ,
  [REPEAT_COUNT] [INTEGER] NOT NULL ,
  [REPEAT_INTERVAL] [BIGINT] NOT NULL ,
  [TIMES_TRIGGERED] [INTEGER] NOT NULL
)
GO

CREATE TABLE [QRTZ_SIMPROP_TRIGGERS] (
  [SCHED_NAME] [NVARCHAR] (120)  NOT NULL ,
  [TRIGGER_NAME] [NVARCHAR] (150)  NOT NULL ,
  [TRIGGER_GROUP] [NVARCHAR] (150)  NOT NULL ,
  [STR_PROP_1] [NVARCHAR] (512) NULL,
  [STR_PROP_2] [NVARCHAR] (512) NULL,
  [STR_PROP_3] [NVARCHAR] (512) NULL,
  [INT_PROP_1] [INT] NULL,
  [INT_PROP_2] [INT] NULL,
  [LONG_PROP_1] [BIGINT] NULL,
  [LONG_PROP_2] [BIGINT] NULL,
  [DEC_PROP_1] [NUMERIC] (13,4) NULL,
  [DEC_PROP_2] [NUMERIC] (13,4) NULL,
  [BOOL_PROP_1] BIT NULL,
  [BOOL_PROP_2] BIT NULL,
  [TIME_ZONE_ID] [NVARCHAR] (80) NULL 
)
GO

CREATE TABLE [QRTZ_BLOB_TRIGGERS] (
  [SCHED_NAME] [NVARCHAR] (120)  NOT NULL ,
  [TRIGGER_NAME] [NVARCHAR] (150)  NOT NULL ,
  [TRIGGER_GROUP] [NVARCHAR] (150)  NOT NULL ,
  [BLOB_DATA] [VARBINARY](MAX) NULL
)
GO

CREATE TABLE [QRTZ_TRIGGERS] (
  [SCHED_NAME] [NVARCHAR] (120)  NOT NULL ,
  [TRIGGER_NAME] [NVARCHAR] (150)  NOT NULL ,
  [TRIGGER_GROUP] [NVARCHAR] (150)  NOT NULL ,
  [JOB_NAME] [NVARCHAR] (150)  NOT NULL ,
  [JOB_GROUP] [NVARCHAR] (150)  NOT NULL ,
  [DESCRIPTION] [NVARCHAR] (250) NULL ,
  [NEXT_FIRE_TIME] [BIGINT] NULL ,
  [PREV_FIRE_TIME] [BIGINT] NULL ,
  [PRIORITY] [INTEGER] NULL ,
  [TRIGGER_STATE] [NVARCHAR] (16)  NOT NULL ,
  [TRIGGER_TYPE] [NVARCHAR] (8)  NOT NULL ,
  [START_TIME] [BIGINT] NOT NULL ,
  [END_TIME] [BIGINT] NULL ,
  [CALENDAR_NAME] [NVARCHAR] (200)  NULL ,
  [MISFIRE_INSTR] [INTEGER] NULL ,
  [JOB_DATA] [VARBINARY](MAX) NULL
)
GO

ALTER TABLE [QRTZ_CALENDARS] WITH NOCHECK ADD
  CONSTRAINT [PK_QRTZ_CALENDARS] PRIMARY KEY  CLUSTERED
  (
    [SCHED_NAME],
    [CALENDAR_NAME]
  ) 
GO

ALTER TABLE [QRTZ_CRON_TRIGGERS] WITH NOCHECK ADD
  CONSTRAINT [PK_QRTZ_CRON_TRIGGERS] PRIMARY KEY  CLUSTERED
  (
    [SCHED_NAME],
    [TRIGGER_NAME],
    [TRIGGER_GROUP]
  ) 
GO

ALTER TABLE [QRTZ_FIRED_TRIGGERS] WITH NOCHECK ADD
  CONSTRAINT [PK_QRTZ_FIRED_TRIGGERS] PRIMARY KEY  CLUSTERED
  (
    [SCHED_NAME],
    [ENTRY_ID]
  ) 
GO

ALTER TABLE [QRTZ_PAUSED_TRIGGER_GRPS] WITH NOCHECK ADD
  CONSTRAINT [PK_QRTZ_PAUSED_TRIGGER_GRPS] PRIMARY KEY  CLUSTERED
  (
    [SCHED_NAME],
    [TRIGGER_GROUP]
  ) 
GO

ALTER TABLE [QRTZ_SCHEDULER_STATE] WITH NOCHECK ADD
  CONSTRAINT [PK_QRTZ_SCHEDULER_STATE] PRIMARY KEY  CLUSTERED
  (
    [SCHED_NAME],
    [INSTANCE_NAME]
  ) 
GO

ALTER TABLE [QRTZ_LOCKS] WITH NOCHECK ADD
  CONSTRAINT [PK_QRTZ_LOCKS] PRIMARY KEY  CLUSTERED
  (
    [SCHED_NAME],
    [LOCK_NAME]
  ) 
GO

ALTER TABLE [QRTZ_JOB_DETAILS] WITH NOCHECK ADD
  CONSTRAINT [PK_QRTZ_JOB_DETAILS] PRIMARY KEY  CLUSTERED
  (
    [SCHED_NAME],
    [JOB_NAME],
    [JOB_GROUP]
  ) 
GO

ALTER TABLE [QRTZ_SIMPLE_TRIGGERS] WITH NOCHECK ADD
  CONSTRAINT [PK_QRTZ_SIMPLE_TRIGGERS] PRIMARY KEY  CLUSTERED
  (
    [SCHED_NAME],
    [TRIGGER_NAME],
    [TRIGGER_GROUP]
  ) 
GO

ALTER TABLE [QRTZ_SIMPROP_TRIGGERS] WITH NOCHECK ADD
  CONSTRAINT [PK_QRTZ_SIMPROP_TRIGGERS] PRIMARY KEY  CLUSTERED
  (
    [SCHED_NAME],
    [TRIGGER_NAME],
    [TRIGGER_GROUP]
  ) 
GO

ALTER TABLE [QRTZ_TRIGGERS] WITH NOCHECK ADD
  CONSTRAINT [PK_QRTZ_TRIGGERS] PRIMARY KEY  CLUSTERED
  (
    [SCHED_NAME],
    [TRIGGER_NAME],
    [TRIGGER_GROUP]
  ) 
GO

ALTER TABLE QRTZ_BLOB_TRIGGERS WITH NOCHECK ADD
  CONSTRAINT [PK_QRTZ_BLOB_TRIGGERS] PRIMARY KEY  CLUSTERED
  (
    [SCHED_NAME],
    [TRIGGER_NAME],
    [TRIGGER_GROUP]
  ) 
GO

ALTER TABLE [QRTZ_CRON_TRIGGERS] ADD
  CONSTRAINT [FK_QRTZ_CRON_TRIGGERS_QRTZ_TRIGGERS] FOREIGN KEY
  (
    [SCHED_NAME],
    [TRIGGER_NAME],
    [TRIGGER_GROUP]
  ) REFERENCES [QRTZ_TRIGGERS] (
    [SCHED_NAME],
    [TRIGGER_NAME],
    [TRIGGER_GROUP]
  ) ON DELETE CASCADE
GO

ALTER TABLE [QRTZ_SIMPLE_TRIGGERS] ADD
  CONSTRAINT [FK_QRTZ_SIMPLE_TRIGGERS_QRTZ_TRIGGERS] FOREIGN KEY
  (
    [SCHED_NAME],
    [TRIGGER_NAME],
    [TRIGGER_GROUP]
  ) REFERENCES [QRTZ_TRIGGERS] (
    [SCHED_NAME],
    [TRIGGER_NAME],
    [TRIGGER_GROUP]
  ) ON DELETE CASCADE
GO

ALTER TABLE [QRTZ_SIMPROP_TRIGGERS] ADD
  CONSTRAINT [FK_QRTZ_SIMPROP_TRIGGERS_QRTZ_TRIGGERS] FOREIGN KEY
  (
	[SCHED_NAME],
    [TRIGGER_NAME],
    [TRIGGER_GROUP]
  ) REFERENCES [QRTZ_TRIGGERS] (
    [SCHED_NAME],
    [TRIGGER_NAME],
    [TRIGGER_GROUP]
  ) ON DELETE CASCADE
GO

ALTER TABLE [QRTZ_TRIGGERS] ADD
  CONSTRAINT [FK_QRTZ_TRIGGERS_QRTZ_JOB_DETAILS] FOREIGN KEY
  (
    [SCHED_NAME],
    [JOB_NAME],
    [JOB_GROUP]
  ) REFERENCES [QRTZ_JOB_DETAILS] (
    [SCHED_NAME],
    [JOB_NAME],
    [JOB_GROUP]
  )
GO

CREATE INDEX IDX_QRTZ_T_J ON QRTZ_TRIGGERS(SCHED_NAME,JOB_NAME,JOB_GROUP)
CREATE INDEX IDX_QRTZ_T_JG ON QRTZ_TRIGGERS(SCHED_NAME,JOB_GROUP)
CREATE INDEX IDX_QRTZ_T_C ON QRTZ_TRIGGERS(SCHED_NAME,CALENDAR_NAME)
CREATE INDEX IDX_QRTZ_T_G ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_GROUP)
CREATE INDEX IDX_QRTZ_T_STATE ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_STATE)
CREATE INDEX IDX_QRTZ_T_N_STATE ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP,TRIGGER_STATE)
CREATE INDEX IDX_QRTZ_T_N_G_STATE ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_GROUP,TRIGGER_STATE)
CREATE INDEX IDX_QRTZ_T_NEXT_FIRE_TIME ON QRTZ_TRIGGERS(SCHED_NAME,NEXT_FIRE_TIME)
CREATE INDEX IDX_QRTZ_T_NFT_ST ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_STATE,NEXT_FIRE_TIME)
CREATE INDEX IDX_QRTZ_T_NFT_MISFIRE ON QRTZ_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME)
CREATE INDEX IDX_QRTZ_T_NFT_ST_MISFIRE ON QRTZ_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_STATE)
CREATE INDEX IDX_QRTZ_T_NFT_ST_MISFIRE_GRP ON QRTZ_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_GROUP,TRIGGER_STATE)

CREATE INDEX IDX_QRTZ_FT_TRIG_INST_NAME ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,INSTANCE_NAME)
CREATE INDEX IDX_QRTZ_FT_INST_JOB_REQ_RCVRY ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,INSTANCE_NAME,REQUESTS_RECOVERY)
CREATE INDEX IDX_QRTZ_FT_J_G ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,JOB_NAME,JOB_GROUP)
CREATE INDEX IDX_QRTZ_FT_JG ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,JOB_GROUP)
CREATE INDEX IDX_QRTZ_FT_T_G ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
CREATE INDEX IDX_QRTZ_FT_TG ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,TRIGGER_GROUP)
GO

-- CORE TABLES


CREATE TABLE idm_attachment (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	attachment_type nvarchar(50),
	content_id binary(255) NOT NULL,
	content_path nvarchar(512),
	description nvarchar(2000),
	encoding nvarchar(100) NOT NULL,
	filesize numeric(19,0) NOT NULL,
	mimetype nvarchar(255) NOT NULL,
	name nvarchar(255) NOT NULL,
	owner_id binary(16),
	owner_state nvarchar(50),
	owner_type nvarchar(255) NOT NULL,
	version_label nvarchar(10) NOT NULL,
	version_number int NOT NULL,
	next_version_id binary(16),
	parent_id binary(16),
	CONSTRAINT idm_attachment_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_attachment_desc ON idm_attachment (description);
CREATE INDEX idx_idm_attachment_name ON idm_attachment (name);
CREATE INDEX idx_idm_attachment_o_id ON idm_attachment (owner_id);
CREATE INDEX idx_idm_attachment_o_type ON idm_attachment (owner_type);


CREATE TABLE idm_authorization_policy (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	authorizable_type nvarchar(255),
	base_permissions nvarchar(255),
	description nvarchar(2000),
	disabled bit NOT NULL,
	evaluator_properties image,
	evaluator_type nvarchar(255) NOT NULL,
	group_permission nvarchar(255),
	seq smallint,
	role_id binary(16) NOT NULL,
	CONSTRAINT idm_authorization_policy_pkey PRIMARY KEY (id),
	CONSTRAINT idm_authorization_policy_seq_check CHECK ((seq <= 99999))
);
CREATE INDEX idx_idm_author_policy_a_t ON idm_authorization_policy (authorizable_type);
CREATE INDEX idx_idm_author_policy_role ON idm_authorization_policy (role_id);


CREATE TABLE idm_auto_role (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	name nvarchar(255) NOT NULL,
	role_id binary(16) NOT NULL,
	CONSTRAINT idm_auto_role_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_auto_role_name ON idm_auto_role (name);
CREATE INDEX idx_idm_auto_role_role ON idm_auto_role (role_id);


CREATE TABLE idm_auto_role_att_rule (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	attribute_name nvarchar(255),
	comparison nvarchar(255) NOT NULL,
	[type] nvarchar(255) NOT NULL,
	value nvarchar(2000),
	auto_role_att_id binary(16) NOT NULL,
	form_attribute_id binary(16),
	CONSTRAINT idm_auto_role_att_rule_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_auto_role_att_rule_id ON idm_auto_role_att_rule (auto_role_att_id);
CREATE INDEX idx_idm_auto_role_form_att_id ON idm_auto_role_att_rule (form_attribute_id);
CREATE INDEX idx_idm_auto_role_form_att_name ON idm_auto_role_att_rule (attribute_name);
CREATE INDEX idx_idm_auto_role_form_type ON idm_auto_role_att_rule ([type]);


CREATE TABLE idm_auto_role_att_rule_req (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	attribute_name nvarchar(255),
	comparison nvarchar(255),
	operation nvarchar(255) NOT NULL,
	[type] nvarchar(255),
	value nvarchar(2000),
	form_attribute_id binary(16),
	auto_role_att_id binary(16) NOT NULL,
	rule_id binary(16),
	CONSTRAINT idm_auto_role_att_rule_req_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_au_r_att_rule_id_req ON idm_auto_role_att_rule_req (auto_role_att_id);
CREATE INDEX idx_idm_au_r_att_rule_req_rule ON idm_auto_role_att_rule_req (rule_id);
CREATE INDEX idx_idm_au_r_form_att_id_req ON idm_auto_role_att_rule_req (form_attribute_id);
CREATE INDEX idx_idm_au_r_form_att_n_req ON idm_auto_role_att_rule_req (attribute_name);
CREATE INDEX idx_idm_au_r_form_type_req ON idm_auto_role_att_rule_req ([type]);


CREATE TABLE idm_auto_role_attribute (
	concept bit NOT NULL,
	id binary(16) NOT NULL,
	CONSTRAINT idm_auto_role_attribute_pkey PRIMARY KEY (id),
	CONSTRAINT fk_b8r7j4ssop819j82ebm29kdaq FOREIGN KEY (id) REFERENCES idm_auto_role(id)
);


CREATE TABLE idm_auto_role_request (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	description nvarchar(2000),
	execute_immediately bit NOT NULL,
	name nvarchar(255),
	operation nvarchar(255),
	recursion_type nvarchar(255),
	request_type nvarchar(255) NOT NULL,
	result_cause nvarchar(MAX),
	result_code nvarchar(255),
	result_model image,
	result_state nvarchar(45) NOT NULL,
	state nvarchar(255) NOT NULL,
	wf_process_id nvarchar(255),
	auto_role_att_id binary(16),
	role_id binary(16),
	tree_node_id binary(16),
	CONSTRAINT idm_auto_role_request_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_auto_role_name_req ON idm_auto_role_request (name);
CREATE INDEX idx_idm_auto_role_role_req ON idm_auto_role_request (role_id);


CREATE TABLE idm_con_slice_form_value (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	boolean_value bit,
	byte_value varbinary(255),
	confidential bit NOT NULL,
	date_value datetime2(6),
	double_value numeric(38,4),
	long_value numeric(19,0),
	persistent_type nvarchar(45) NOT NULL,
	seq smallint,
	short_text_value nvarchar(2000),
	string_value nvarchar(MAX),
	uuid_value binary(16),
	attribute_id binary(16) NOT NULL,
	owner_id binary(16) NOT NULL,
	CONSTRAINT idm_con_slice_form_value_pkey PRIMARY KEY (id),
	CONSTRAINT idm_con_slice_form_value_seq_check CHECK ((seq <= 99999))
);
CREATE INDEX idx_idm_con_slice_form_a ON idm_con_slice_form_value (owner_id);
CREATE INDEX idx_idm_con_slice_form_a_def ON idm_con_slice_form_value (attribute_id);
CREATE INDEX idx_idm_con_slice_form_uuid ON idm_con_slice_form_value (uuid_value);


CREATE TABLE idm_concept_role_request (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	log nvarchar(MAX),
	operation nvarchar(255),
	state nvarchar(255) NOT NULL,
	valid_from datetime2(6),
	valid_till datetime2(6),
	wf_process_id nvarchar(255),
	automatic_role_id binary(16),
	identity_contract_id binary(16),
	identity_role_id binary(16),
	role_id binary(16),
	request_role_id binary(16) NOT NULL,
	CONSTRAINT idm_concept_role_request_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_conc_role_ident_c ON idm_concept_role_request (identity_contract_id);
CREATE INDEX idx_idm_conc_role_request ON idm_concept_role_request (request_role_id);
CREATE INDEX idx_idm_conc_role_role ON idm_concept_role_request (role_id);
CREATE INDEX idx_idm_conc_role_iden_rol ON idm_concept_role_request  (identity_role_id);
CREATE INDEX idx_idm_conc_role_tree_node ON idm_concept_role_request (automatic_role_id);


CREATE TABLE idm_confidential_storage (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	storage_key nvarchar(255) NOT NULL,
	owner_id binary(16) NOT NULL,
	owner_type nvarchar(255) NOT NULL,
	storage_value image,
	CONSTRAINT idm_confidential_storage_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_confidential_storage_key ON idm_confidential_storage (storage_key);
CREATE INDEX idx_confidential_storage_o_i ON idm_confidential_storage (owner_id);
CREATE INDEX idx_confidential_storage_o_t ON idm_confidential_storage (owner_type);


CREATE TABLE idm_configuration (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	confidential bit NOT NULL,
	name nvarchar(255) NOT NULL,
	secured bit NOT NULL,
	value nvarchar(255),
	CONSTRAINT idm_configuration_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ux_configuration_name ON idm_configuration (name);


CREATE TABLE idm_contract_guarantee (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	external_id nvarchar(255),
	guarantee_id binary(16) NOT NULL,
	identity_contract_id binary(16) NOT NULL,
	CONSTRAINT idm_contract_guarantee_pkey PRIMARY KEY (id)
);
CREATE INDEX idm_contract_guarantee_contr ON idm_contract_guarantee (identity_contract_id);
CREATE INDEX idx_contract_guarantee_idnt ON idm_contract_guarantee (guarantee_id);
CREATE INDEX idx_idm_contract_guar_ext_id ON idm_contract_guarantee (external_id);


CREATE TABLE idm_contract_slice (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	contract_code nvarchar(255),
	contract_valid_from datetime2(6),
	contract_valid_till datetime2(6),
	description nvarchar(2000),
	disabled bit NOT NULL,
	external_id nvarchar(255),
	externe bit NOT NULL,
	main bit NOT NULL,
	[position] nvarchar(255),
	state nvarchar(45),
	using_as_contract bit NOT NULL,
	valid_from datetime2(6),
	valid_till datetime2(6),
	identity_id binary(16) NOT NULL,
	parent_contract_id binary(16),
	work_position_id binary(16),
	CONSTRAINT idm_contract_slice_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_contract_slice_ext_id ON idm_contract_slice (external_id);
CREATE INDEX idx_idm_contract_slice_idnt ON idm_contract_slice (identity_id);
CREATE INDEX idx_idm_contract_slice_wp ON idm_contract_slice (work_position_id);


CREATE TABLE idm_contract_slice_guarantee (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	contract_slice_id binary(16) NOT NULL,
	guarantee_id binary(16) NOT NULL,
	CONSTRAINT idm_contract_slice_guarantee_pkey PRIMARY KEY (id)
);
CREATE INDEX idm_contract_slice_guar_contr ON idm_contract_slice_guarantee (contract_slice_id);
CREATE INDEX idx_contract_slice_guar_idnt ON idm_contract_slice_guarantee (guarantee_id);


CREATE TABLE idm_dependent_task_trigger (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	dependent_task_id nvarchar(255) NOT NULL,
	initiator_task_id nvarchar(255) NOT NULL,
	CONSTRAINT idm_dependent_task_trigger_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_dependent_t_dep ON idm_dependent_task_trigger (dependent_task_id);
CREATE INDEX idx_idm_dependent_t_init ON idm_dependent_task_trigger (initiator_task_id);


CREATE TABLE idm_entity_event (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	closed bit NOT NULL,
	content image,
	event_type nvarchar(255),
	execute_date datetime2(6),
	instance_id nvarchar(255) NOT NULL,
	original_source image,
	owner_id binary(16) NOT NULL,
	owner_type nvarchar(255) NOT NULL,
	parent_event_type nvarchar(255),
	priority nvarchar(45) NOT NULL,
	processed_order int,
	properties image,
	result_cause nvarchar(MAX),
	result_code nvarchar(255),
	result_model image,
	result_state nvarchar(45) NOT NULL,
	suspended bit NOT NULL,
	parent_id binary(16),
	CONSTRAINT idm_entity_event_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_entity_event_created ON idm_entity_event (created);
CREATE INDEX idx_idm_entity_event_exe ON idm_entity_event (execute_date);
CREATE INDEX idx_idm_entity_event_inst ON idm_entity_event (instance_id);
CREATE INDEX idx_idm_entity_event_o_id ON idm_entity_event (owner_id);
CREATE INDEX idx_idm_entity_event_o_type ON idm_entity_event (owner_type);


CREATE TABLE idm_entity_state (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	closed bit NOT NULL,
	instance_id nvarchar(255) NOT NULL,
	owner_id binary(16) NOT NULL,
	owner_type nvarchar(255) NOT NULL,
	processed_order int,
	processor_id nvarchar(255),
	processor_module nvarchar(255),
	processor_name nvarchar(255),
	result_cause nvarchar(MAX),
	result_code nvarchar(255),
	result_model image,
	result_state nvarchar(45) NOT NULL,
	suspended bit NOT NULL,
	event_id binary(16),
	CONSTRAINT idm_entity_state_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_entity_state_event ON idm_entity_state (event_id);
CREATE INDEX idx_idm_entity_state_o_id ON idm_entity_state (owner_id);
CREATE INDEX idx_idm_entity_state_o_type ON idm_entity_state (owner_type);


CREATE TABLE idm_forest_index (
	id numeric(19,0) NOT NULL IDENTITY(1,1),
	forest_tree_type nvarchar(255) NOT NULL,
	lft numeric(19,0),
	rgt numeric(19,0),
	content_id binary(16),
	parent_id numeric(19,0),
	CONSTRAINT idm_forest_index_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_forest_index_content ON idm_forest_index (content_id);
CREATE INDEX idx_forest_index_lft ON idm_forest_index (lft);
CREATE INDEX idx_forest_index_parent ON idm_forest_index (parent_id);
CREATE INDEX idx_forest_index_rgt ON idm_forest_index (rgt);
CREATE INDEX idx_forest_index_tree_type ON idm_forest_index (forest_tree_type);


CREATE TABLE idm_form (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	name nvarchar(255),
	owner_code nvarchar(255),
	owner_id binary(16),
	owner_type nvarchar(255) NOT NULL,
	form_definition_id binary(16) NOT NULL,
	CONSTRAINT idm_form_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_form_f_definition_id ON idm_form (form_definition_id);
CREATE INDEX idx_idm_form_owner_code ON idm_form (owner_code);
CREATE INDEX idx_idm_form_owner_id ON idm_form (owner_id);
CREATE INDEX idx_idm_form_owner_type ON idm_form (owner_type);


CREATE TABLE idm_form_attribute (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	code nvarchar(255) NOT NULL,
	confidential bit NOT NULL,
	default_value nvarchar(MAX),
	description nvarchar(2000),
	face_type nvarchar(45),
	multiple bit NOT NULL,
	name nvarchar(255) NOT NULL,
	persistent_type nvarchar(45) NOT NULL,
	placeholder nvarchar(255),
	readonly bit NOT NULL,
	required bit NOT NULL,
	seq smallint,
	unmodifiable bit NOT NULL,
	definition_id binary(16) NOT NULL,
	CONSTRAINT idm_form_attribute_pkey PRIMARY KEY (id),
	CONSTRAINT idm_form_attribute_seq_check CHECK ((seq <= 99999))
);
CREATE INDEX idx_idm_f_a_definition_def ON idm_form_attribute (definition_id);
CREATE UNIQUE INDEX ux_idm_f_a_definition_name ON idm_form_attribute (definition_id,code);


CREATE TABLE idm_form_definition (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	code nvarchar(255) NOT NULL,
	description nvarchar(2000),
	main bit NOT NULL,
	name nvarchar(255) NOT NULL,
	definition_type nvarchar(255) NOT NULL,
	unmodifiable bit NOT NULL,
	CONSTRAINT idm_form_definition_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ux_idm_form_definition_tn ON idm_form_definition (definition_type,code);


CREATE TABLE idm_form_value (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	boolean_value bit,
	byte_value varbinary(255),
	confidential bit NOT NULL,
	date_value datetime2(6),
	double_value numeric(38,4),
	long_value numeric(19,0),
	persistent_type nvarchar(45) NOT NULL,
	seq smallint,
	short_text_value nvarchar(2000),
	string_value nvarchar(MAX),
	uuid_value binary(16),
	attribute_id binary(16) NOT NULL,
	owner_id binary(16) NOT NULL,
	CONSTRAINT idm_form_value_pkey PRIMARY KEY (id),
	CONSTRAINT idm_form_value_seq_check CHECK ((seq <= 99999))
);
CREATE INDEX idx_idm_form_value_a ON idm_form_value (owner_id);
CREATE INDEX idx_idm_form_value_a_def ON idm_form_value (attribute_id);
CREATE INDEX idx_idm_form_value_uuid ON idm_form_value (uuid_value);


CREATE TABLE idm_i_contract_form_value (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	boolean_value bit,
	byte_value varbinary(255),
	confidential bit NOT NULL,
	date_value datetime2(6),
	double_value numeric(38,4),
	long_value numeric(19,0),
	persistent_type nvarchar(45) NOT NULL,
	seq smallint,
	short_text_value nvarchar(2000),
	string_value nvarchar(MAX),
	uuid_value binary(16),
	attribute_id binary(16) NOT NULL,
	owner_id binary(16) NOT NULL,
	CONSTRAINT idm_i_contract_form_value_pkey PRIMARY KEY (id),
	CONSTRAINT idm_i_contract_form_value_seq_check CHECK ((seq <= 99999))
);
CREATE INDEX idx_idm_i_contract_form_a ON idm_i_contract_form_value (owner_id);
CREATE INDEX idx_idm_i_contract_form_a_def ON idm_i_contract_form_value (attribute_id);
CREATE INDEX idx_idm_i_contract_form_uuid ON idm_i_contract_form_value (uuid_value);


CREATE TABLE idm_identity (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	description nvarchar(2000),
	disabled bit NOT NULL,
	email nvarchar(255),
	external_code nvarchar(255),
	external_id nvarchar(255),
	first_name nvarchar(255),
	last_name nvarchar(255),
	phone nvarchar(30),
	state nvarchar(45) NOT NULL,
	title_after nvarchar(100),
	title_before nvarchar(100),
	username nvarchar(255) NOT NULL,
	version numeric(19,0),
	CONSTRAINT idm_identity_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_identity_external_code ON idm_identity (external_code);
CREATE INDEX idx_idm_identity_external_id ON idm_identity (external_id);
CREATE UNIQUE INDEX ux_idm_identity_username ON idm_identity (username);


CREATE TABLE idm_identity_contract (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	description nvarchar(2000),
	disabled bit NOT NULL,
	external_id nvarchar(255),
	externe bit NOT NULL,
	main bit NOT NULL,
	[position] nvarchar(255),
	state nvarchar(45),
	valid_from datetime2(6),
	valid_till datetime2(6),
	identity_id binary(16) NOT NULL,
	work_position_id binary(16),
	CONSTRAINT idm_identity_contract_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_contract_ext_id ON idm_identity_contract (external_id);
CREATE INDEX idx_idm_identity_contract_idnt ON idm_identity_contract (identity_id);
CREATE INDEX idx_idm_identity_contract_wp ON idm_identity_contract (work_position_id);


CREATE TABLE idm_identity_form_value (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	boolean_value bit,
	byte_value varbinary(255),
	confidential bit NOT NULL,
	date_value datetime2(6),
	double_value numeric(38,4),
	long_value numeric(19,0),
	persistent_type nvarchar(45) NOT NULL,
	seq smallint,
	short_text_value nvarchar(2000),
	string_value nvarchar(MAX),
	uuid_value binary(16),
	attribute_id binary(16) NOT NULL,
	owner_id binary(16) NOT NULL,
	CONSTRAINT idm_identity_form_value_pkey PRIMARY KEY (id),
	CONSTRAINT idm_identity_form_value_seq_check CHECK ((seq <= 99999))
);
CREATE INDEX idx_idm_identity_form_a ON idm_identity_form_value (owner_id);
CREATE INDEX idx_idm_identity_form_a_def ON idm_identity_form_value (attribute_id);
CREATE INDEX idx_idm_identity_form_uuid ON idm_identity_form_value (uuid_value);


CREATE TABLE idm_identity_role (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	external_id nvarchar(255),
	valid_from datetime2(6),
	valid_till datetime2(6),
	automatic_role_id binary(16),
	identity_contract_id binary(16) NOT NULL,
	role_id binary(16) NOT NULL,
	CONSTRAINT idm_identity_role_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_identity_role_aut_r ON idm_identity_role (automatic_role_id);
CREATE INDEX idx_idm_identity_role_ext_id ON idm_identity_role (external_id);
CREATE INDEX idx_idm_identity_role_ident_c ON idm_identity_role (identity_contract_id);
CREATE INDEX idx_idm_identity_role_role ON idm_identity_role (role_id);


CREATE TABLE idm_identity_role_valid_req (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	current_attempt int,
	result_cause nvarchar(MAX),
	result_code nvarchar(255),
	result_model image,
	result_state nvarchar(45) NOT NULL,
	identity_role_id binary(16) NOT NULL,
	CONSTRAINT idm_identity_role_valid_request_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uk_cs0od1m7no03giio0p27mfpsr ON idm_identity_role_valid_req (identity_role_id);
CREATE INDEX idx_idm_identity_role_id ON idm_identity_role_valid_req (identity_role_id);


CREATE TABLE idm_long_running_task (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	task_count numeric(19,0),
	task_counter numeric(19,0),
	dry_run bit NOT NULL,
	instance_id nvarchar(255) NOT NULL,
	result_cause nvarchar(MAX),
	result_code nvarchar(255),
	result_model image,
	result_state nvarchar(45) NOT NULL,
	running bit NOT NULL,
	stateful bit NOT NULL,
	task_description nvarchar(255),
	task_properties image,
	task_started datetime2(6),
	task_type nvarchar(255) NOT NULL,
	thread_id numeric(19,0) NOT NULL,
	thread_name nvarchar(255),
	scheduled_task_id binary(16),
	CONSTRAINT idm_long_running_task_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_long_r_t_inst ON idm_long_running_task (instance_id);
CREATE INDEX idx_idm_long_r_t_s_task ON idm_long_running_task (scheduled_task_id);
CREATE INDEX idx_idm_long_r_t_type ON idm_long_running_task (task_type);


CREATE TABLE idm_notification (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	html_message nvarchar(MAX),
	[level] nvarchar(45) NOT NULL,
	result_model image,
	subject nvarchar(255),
	text_message nvarchar(MAX),
	sent datetime2(6),
	sent_log nvarchar(2000),
	topic nvarchar(255),
	identity_sender_id binary(16),
	notification_template_id binary(16),
	parent_notification_id binary(16),
	CONSTRAINT idm_notification_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_notification_parent ON idm_notification (parent_notification_id);
CREATE INDEX idx_idm_notification_sender ON idm_notification (identity_sender_id);


CREATE TABLE idm_notification_configuration (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	description nvarchar(2000),
	[level] nvarchar(45),
	notification_type nvarchar(255) NOT NULL,
	topic nvarchar(255) NOT NULL,
	template_id binary(16),
	CONSTRAINT idm_notification_configuration_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_not_conf_level ON idm_notification_configuration ([level]);
CREATE INDEX idx_idm_not_conf_topic ON idm_notification_configuration (topic);
CREATE INDEX idx_idm_not_conf_type ON idm_notification_configuration (notification_type);
CREATE INDEX idx_idm_not_template ON idm_notification_configuration (template_id);
CREATE UNIQUE INDEX ux_idm_not_conf ON idm_notification_configuration (topic,[level],notification_type);


CREATE TABLE idm_notification_log (
	id binary(16) NOT NULL,
	CONSTRAINT idm_notification_log_pkey PRIMARY KEY (id),
	CONSTRAINT fk_6lxo8e33m2cn2kemxjfo72cp7 FOREIGN KEY (id) REFERENCES idm_notification(id)
);


CREATE TABLE idm_notification_console (
	id binary(16) NOT NULL,
	CONSTRAINT idm_notification_console_pkey PRIMARY KEY (id),
	CONSTRAINT fk_ptf0bbum1akrs8sx6eoy0csw1 FOREIGN KEY (id) REFERENCES idm_notification_log(id)
);


CREATE TABLE idm_notification_email (
	id binary(16) NOT NULL,
	CONSTRAINT idm_notification_email_pkey PRIMARY KEY (id),
	CONSTRAINT fk_675yat9emstk1gse1nybduyly FOREIGN KEY (id) REFERENCES idm_notification_log(id)
);


CREATE TABLE idm_notification_recipient (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	real_recipient nvarchar(255),
	identity_recipient_id binary(16),
	notification_id binary(16) NOT NULL,
	CONSTRAINT idm_notification_recipient_pkey PRIMARY KEY (id),
	CONSTRAINT fk_svipm8scpjnuy2keri4u1whf1 FOREIGN KEY (notification_id) REFERENCES idm_notification(id)
);
CREATE INDEX idx_idm_notification_rec_idnt ON idm_notification_recipient (identity_recipient_id);
CREATE INDEX idx_idm_notification_rec_not ON idm_notification_recipient (notification_id);


CREATE TABLE idm_notification_sms (
	id binary(16) NOT NULL,
	CONSTRAINT idm_notification_sms_pkey PRIMARY KEY (id),
	CONSTRAINT fk_1t9lptl1wo4fdo5vux54lru6 FOREIGN KEY (id) REFERENCES idm_notification_log(id)
);


CREATE TABLE idm_notification_template (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	body_html nvarchar(MAX),
	body_text nvarchar(MAX),
	code nvarchar(255) NOT NULL,
	module nvarchar(255),
	name nvarchar(255) NOT NULL,
	[parameter] nvarchar(255),
	sender nvarchar(255),
	subject nvarchar(255) NOT NULL,
	unmodifiable bit NOT NULL,
	CONSTRAINT idm_notification_template_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_n_template_name ON idm_notification_template (name);
CREATE UNIQUE INDEX ux_idm_notification_template_code ON idm_notification_template (code);


CREATE TABLE idm_notification_websocket (
	id binary(16) NOT NULL,
	CONSTRAINT idm_notification_websocket_pkey PRIMARY KEY (id),
	CONSTRAINT fk_bnnrv7yx0p8mj75tn6ogkc4qe FOREIGN KEY (id) REFERENCES idm_notification_log(id)
);


CREATE TABLE idm_password (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	block_login_date datetime2(6),
	last_successful_login datetime2(6),
	must_change bit,
	password nvarchar(255),
	unsuccessful_attempts int NOT NULL,
	valid_from datetime2(6),
	valid_till datetime2(6),
	identity_id binary(16) NOT NULL,
	CONSTRAINT idm_password_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX idx_idm_password_identity ON idm_password (identity_id);


CREATE TABLE idm_password_history (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	password nvarchar(255) NOT NULL,
	identity_id binary(16) NOT NULL,
	CONSTRAINT idm_password_history_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_identity ON idm_password_history (identity_id);


CREATE TABLE idm_password_policy (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	block_login_time int,
	default_policy bit NOT NULL,
	description nvarchar(2000),
	disabled bit NOT NULL,
	enchanced_control bit NOT NULL,
	generate_type nvarchar(255),
	identity_attribute_check nvarchar(255),
	lower_char_base nvarchar(255) NOT NULL,
	lower_char_required bit NOT NULL,
	max_history_similar int,
	max_password_age int,
	max_password_length int,
	max_unsuccessful_attempts int,
	min_lower_char int,
	min_number int,
	min_password_age int,
	min_password_length int,
	min_rules_to_fulfill int,
	min_special_char int,
	min_upper_char int,
	name nvarchar(255) NOT NULL,
	number_base nvarchar(255) NOT NULL,
	number_required bit NOT NULL,
	passphrase_words int,
	password_length_required bit NOT NULL,
	prohibited_characters nvarchar(255),
	special_char_base nvarchar(255) NOT NULL,
	special_char_required bit NOT NULL,
	[type] nvarchar(255),
	upper_char_base nvarchar(255) NOT NULL,
	upper_char_required bit NOT NULL,
	weak_pass nvarchar(255),
	weak_pass_required bit NOT NULL,
	CONSTRAINT idm_password_policy_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ux_idm_pass_policy_name ON idm_password_policy (name);


CREATE TABLE idm_processed_task_item (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	result_cause nvarchar(MAX),
	result_code nvarchar(255),
	result_model image,
	result_state nvarchar(45) NOT NULL,
	referenced_dto_type nvarchar(255) NOT NULL,
	referenced_entity_id binary(16) NOT NULL,
	long_running_task binary(16),
	scheduled_task_queue_owner binary(16),
	CONSTRAINT idm_processed_task_item_pkey PRIMARY KEY (id)
);
CREATE INDEX idm_processed_t_i_l_r_t ON idm_processed_task_item (long_running_task);
CREATE INDEX idm_processed_t_i_q_o ON idm_processed_task_item (scheduled_task_queue_owner);


CREATE TABLE idm_profile (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	preferred_language nvarchar(45),
	identity_id binary(16) NOT NULL,
	image_id binary(16),
	CONSTRAINT idm_profile_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uk_profile_identity_id ON idm_profile (identity_id);


CREATE TABLE idm_role (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	approve_remove bit NOT NULL,
	can_be_requested bit NOT NULL,
	description nvarchar(2000),
	disabled bit NOT NULL,
	external_id nvarchar(255),
	code nvarchar(255) NOT NULL,
	name nvarchar(255) NOT NULL,
	priority int NOT NULL,
	role_type nvarchar(255) NOT NULL,
	version numeric(19,0),
	CONSTRAINT idm_role_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_role_external_id ON idm_role (external_id);
CREATE INDEX idx_idm_role_name ON idm_role (name);
CREATE UNIQUE INDEX ux_idm_role_code ON idm_role (code);


CREATE TABLE idm_role_catalogue (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	code nvarchar(255) NOT NULL,
	description nvarchar(2000),
	external_id nvarchar(255),
	name nvarchar(255) NOT NULL,
	url nvarchar(255),
	url_title nvarchar(255),
	parent_id binary(16),
	CONSTRAINT idm_role_catalogue_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_role_cat_ext_id ON idm_role_catalogue (external_id);
CREATE INDEX idx_idm_role_cat_parent ON idm_role_catalogue (parent_id);
CREATE UNIQUE INDEX ux_role_catalogue_code ON idm_role_catalogue (code);
CREATE INDEX ux_role_catalogue_name ON idm_role_catalogue (name);


CREATE TABLE idm_role_catalogue_role (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	role_id binary(16) NOT NULL,
	role_catalogue_id binary(16) NOT NULL,
	CONSTRAINT idm_role_catalogue_role_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_role_catalogue_id ON idm_role_catalogue_role (role_catalogue_id);
CREATE INDEX idx_idm_role_id ON idm_role_catalogue_role (role_id);


CREATE TABLE idm_role_composition (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	sub_id binary(16) NOT NULL,
	superior_id binary(16) NOT NULL,
	CONSTRAINT idm_role_composition_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_role_composition_sub ON idm_role_composition (sub_id);
CREATE INDEX idx_idm_role_composition_super ON idm_role_composition (superior_id);


CREATE TABLE idm_role_form_value (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	boolean_value bit,
	byte_value varbinary(255),
	confidential bit NOT NULL,
	date_value datetime2(6),
	double_value numeric(38,4),
	long_value numeric(19,0),
	persistent_type nvarchar(45) NOT NULL,
	seq smallint,
	short_text_value nvarchar(2000),
	string_value nvarchar(MAX),
	uuid_value binary(16),
	attribute_id binary(16) NOT NULL,
	owner_id binary(16) NOT NULL,
	CONSTRAINT idm_role_form_value_pkey PRIMARY KEY (id),
	CONSTRAINT idm_role_form_value_seq_check CHECK ((seq <= 99999))
);
CREATE INDEX idx_idm_role_form_a ON idm_role_form_value (owner_id);
CREATE INDEX idx_idm_role_form_a_def ON idm_role_form_value (attribute_id);
CREATE INDEX idx_idm_role_form_uuid ON idm_role_form_value (uuid_value);


CREATE TABLE idm_role_guarantee (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	guarantee_id binary(16) NOT NULL,
	role_id binary(16) NOT NULL,
	CONSTRAINT idm_role_guarantee_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_role_guarantee_gnt ON idm_role_guarantee (guarantee_id);
CREATE INDEX idx_idm_role_guarantee_role ON idm_role_guarantee (role_id);


CREATE TABLE idm_role_guarantee_role (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	guarantee_role_id binary(16) NOT NULL,
	role_id binary(16) NOT NULL,
	CONSTRAINT idm_role_guarantee_role_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_role_g_r_g_role ON idm_role_guarantee_role (guarantee_role_id);
CREATE INDEX idx_idm_role_g_r_role ON idm_role_guarantee_role (role_id);


CREATE TABLE idm_role_request (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	description nvarchar(255),
	execute_immediately bit NOT NULL,
	log nvarchar(MAX),
	original_request nvarchar(MAX),
	requested_by_type nvarchar(255) NOT NULL,
	state nvarchar(255) NOT NULL,
	wf_process_id nvarchar(255),
	applicant_id binary(16) NOT NULL,
	duplicated_to_request binary(16),
	CONSTRAINT idm_role_request_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_role_request_app_id ON idm_role_request (applicant_id);
CREATE INDEX idx_idm_role_request_state ON idm_role_request (state);


CREATE TABLE idm_role_tree_node (
	recursion_type nvarchar(255) NOT NULL,
	id binary(16) NOT NULL,
	tree_node_id binary(16) NOT NULL,
	CONSTRAINT idm_role_tree_node_pkey PRIMARY KEY (id),
	CONSTRAINT fk_2qgkyu38e9u67xtedpr0ylqb5 FOREIGN KEY (id) REFERENCES idm_auto_role(id)
);
CREATE INDEX idx_idm_role_tree_node ON idm_role_tree_node (tree_node_id);


CREATE TABLE idm_scheduled_task (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	quartz_task_name nvarchar(255) NOT NULL,
	CONSTRAINT idm_scheduled_task_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uk_8bbpr92i3lvuiw52kvmh8ci1c ON idm_scheduled_task (quartz_task_name);


CREATE TABLE idm_script (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	category nvarchar(255) NOT NULL,
	code nvarchar(255) NOT NULL,
	description nvarchar(2000),
	name nvarchar(255),
	script nvarchar(MAX),
	CONSTRAINT idm_script_pkey PRIMARY KEY (id)
);
CREATE INDEX ux_script_category ON idm_script (category);
CREATE UNIQUE INDEX ux_script_code ON idm_script (code);


CREATE TABLE idm_script_authority (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	class_name nvarchar(255),
	service nvarchar(255),
	[type] nvarchar(255) NOT NULL,
	script_id binary(16) NOT NULL,
	CONSTRAINT idm_script_authority_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_script_auth_script ON idm_script_authority (script_id);


CREATE TABLE idm_token (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	disabled bit NOT NULL,
	expiration datetime2(6),
	external_id nvarchar(255),
	issued_at datetime2(6) NOT NULL,
	module_id nvarchar(255),
	owner_id binary(16),
	owner_type nvarchar(255) NOT NULL,
	properties image,
	token nvarchar(2000),
	token_type nvarchar(45),
	CONSTRAINT idm_token_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_token_exp ON idm_token (expiration);
CREATE INDEX idx_idm_token_external_id ON idm_token (external_id);
CREATE INDEX idx_idm_token_o_id ON idm_token (owner_id);
CREATE INDEX idx_idm_token_o_type ON idm_token (owner_type);
CREATE INDEX idx_idm_token_token ON idm_token (token);
CREATE INDEX idx_idm_token_type ON idm_token (token_type);


CREATE TABLE idm_tree_node (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	code nvarchar(255) NOT NULL,
	disabled bit NOT NULL,
	external_id nvarchar(255),
	name nvarchar(255) NOT NULL,
	version numeric(19,0),
	parent_id binary(16),
	tree_type_id binary(16) NOT NULL,
	CONSTRAINT idm_tree_node_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_tree_node_ext_id ON idm_tree_node (external_id);
CREATE INDEX idx_idm_tree_node_parent ON idm_tree_node (parent_id);
CREATE INDEX idx_idm_tree_node_type ON idm_tree_node (tree_type_id);
CREATE UNIQUE INDEX ux_tree_node_code ON idm_tree_node (tree_type_id,code);


CREATE TABLE idm_tree_node_form_value (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	boolean_value bit,
	byte_value varbinary(255),
	confidential bit NOT NULL,
	date_value datetime2(6),
	double_value numeric(38,4),
	long_value numeric(19,0),
	persistent_type nvarchar(45) NOT NULL,
	seq smallint,
	short_text_value nvarchar(2000),
	string_value nvarchar(MAX),
	uuid_value binary(16),
	attribute_id binary(16) NOT NULL,
	owner_id binary(16) NOT NULL,
	CONSTRAINT idm_tree_node_form_value_pkey PRIMARY KEY (id),
	CONSTRAINT idm_tree_node_form_value_seq_check CHECK ((seq <= 99999))
);
CREATE INDEX idx_idm_tree_node_form_a ON idm_tree_node_form_value (owner_id);
CREATE INDEX idx_idm_tree_node_form_a_def ON idm_tree_node_form_value (attribute_id);
CREATE INDEX idx_idm_tree_node_form_uuid ON idm_tree_node_form_value (uuid_value);


CREATE TABLE idm_tree_type (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	code nvarchar(255) NOT NULL,
	external_id nvarchar(255),
	name nvarchar(255) NOT NULL,
	CONSTRAINT idm_tree_type_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_tree_type_ext_id ON idm_tree_type (external_id);
CREATE UNIQUE INDEX ux_tree_type_code ON idm_tree_type (code);
CREATE INDEX ux_tree_type_name ON idm_tree_type (name);

-- AUDIT TABLES


CREATE TABLE idm_audit (
	id numeric(19,0) NOT NULL IDENTITY(1,1),
	changed_attributes nvarchar(2000),
	entity_id binary(255),
	modification nvarchar(255),
	modifier nvarchar(255),
	modifier_id binary(255),
	original_modifier nvarchar(255),
	original_modifier_id binary(255),
	owner_code nvarchar(255),
	owner_id nvarchar(255),
	owner_type nvarchar(255),
	realm_id binary(255),
	sub_owner_code nvarchar(255),
	sub_owner_id nvarchar(255),
	sub_owner_type nvarchar(255),
	[timestamp] numeric(19,0) NOT NULL,
	[type] nvarchar(255),
	CONSTRAINT idm_audit_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_audit_changed_attributes ON idm_audit (changed_attributes);
CREATE INDEX idx_idm_audit_entity_id ON idm_audit (entity_id);
CREATE INDEX idx_idm_audit_modification ON idm_audit (modification);
CREATE INDEX idx_idm_audit_modifier ON idm_audit (modifier);
CREATE INDEX idx_idm_audit_original_modifier ON idm_audit (original_modifier);
CREATE INDEX idx_idm_audit_owner_code ON idm_audit (owner_code);
CREATE INDEX idx_idm_audit_owner_id ON idm_audit (owner_id);
CREATE INDEX idx_idm_audit_owner_type ON idm_audit (owner_type);
CREATE INDEX idx_idm_audit_sub_owner_code ON idm_audit (sub_owner_code);
CREATE INDEX idx_idm_audit_sub_owner_id ON idm_audit (sub_owner_id);
CREATE INDEX idx_idm_audit_sub_owner_type ON idm_audit (sub_owner_type);
CREATE INDEX idx_idm_audit_timestamp ON idm_audit ([timestamp]);


CREATE TABLE idm_authorization_policy_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	authorizable_type nvarchar(255),
	authorizable_type_m bit,
	base_permissions nvarchar(255),
	base_permissions_m bit,
	description nvarchar(2000),
	description_m bit,
	disabled bit,
	disabled_m bit,
	evaluator_properties image,
	evaluator_properties_m bit,
	evaluator_type nvarchar(255),
	evaluator_type_m bit,
	group_permission nvarchar(255),
	group_permission_m bit,
	seq smallint,
	seq_m bit,
	role_id binary(16),
	role_m bit,
	CONSTRAINT idm_authorization_policy_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_gobi05mdqmnq11h8n409tsqmm FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_auto_role_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	name nvarchar(255),
	name_m bit,
	role_id binary(16),
	role_m bit,
	CONSTRAINT idm_auto_role_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_a5dnmuabje245da81j1mtv1id FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_auto_role_att_rule_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	attribute_name nvarchar(255),
	attribute_name_m bit,
	comparison nvarchar(255),
	comparison_m bit,
	[type] nvarchar(255),
	type_m bit,
	value nvarchar(2000),
	value_m bit,
	auto_role_att_id binary(16),
	automatic_role_attribute_m bit,
	form_attribute_id binary(16),
	form_attribute_m bit,
	CONSTRAINT idm_auto_role_att_rule_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_4h00l1b06fwkwi26fed2xq8p4 FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_auto_role_att_rule_req_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	attribute_name nvarchar(255),
	attribute_name_m bit,
	comparison nvarchar(255),
	comparison_m bit,
	operation nvarchar(255),
	operation_m bit,
	[type] nvarchar(255),
	type_m bit,
	value nvarchar(2000),
	value_m bit,
	form_attribute_id binary(16),
	form_attribute_m bit,
	auto_role_att_id binary(16),
	request_m bit,
	rule_id binary(16),
	rule_m bit,
	CONSTRAINT idm_auto_role_att_rule_req_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_h5wcaotvqs4m7pf0siqfr27u4 FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_auto_role_attribute_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	concept bit,
	concept_m bit,
	CONSTRAINT idm_auto_role_attribute_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_otby9l02vverqso9ejrgeabj2 FOREIGN KEY (id,rev) REFERENCES idm_auto_role_a(id,rev)
);


CREATE TABLE idm_auto_role_request_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	description nvarchar(2000),
	description_m bit,
	execute_immediately bit,
	execute_immediately_m bit,
	name nvarchar(255),
	name_m bit,
	operation nvarchar(255),
	operation_m bit,
	recursion_type nvarchar(255),
	recursion_type_m bit,
	request_type nvarchar(255),
	request_type_m bit,
	state nvarchar(255),
	state_m bit,
	wf_process_id nvarchar(255),
	wf_process_id_m bit,
	auto_role_att_id binary(16),
	automatic_role_m bit,
	role_id binary(16),
	role_m bit,
	tree_node_id binary(16),
	tree_node_m bit,
	CONSTRAINT idm_auto_role_request_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_3c5uwgj4whal2entaae9xsros FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_con_slice_form_value_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	boolean_value bit,
	boolean_value_m bit,
	byte_value varbinary(255),
	byte_value_m bit,
	confidential bit,
	confidential_m bit,
	date_value datetime2(6),
	date_value_m bit,
	double_value numeric(38,4),
	double_value_m bit,
	long_value numeric(19,0),
	long_value_m bit,
	persistent_type nvarchar(45),
	persistent_type_m bit,
	seq smallint,
	seq_m bit,
	short_text_value nvarchar(2000),
	short_text_value_m bit,
	string_value nvarchar(MAX),
	string_value_m bit,
	uuid_value binary(16),
	uuid_value_m bit,
	attribute_id binary(16),
	form_attribute_m bit,
	owner_id binary(16),
	owner_m bit,
	CONSTRAINT idm_con_slice_form_value_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_pq67ujqv1s2xmdkl80j36dvhp FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_concept_role_request_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	operation nvarchar(255),
	operation_m bit,
	state nvarchar(255),
	state_m bit,
	valid_from datetime2(6),
	valid_from_m bit,
	valid_till datetime2(6),
	valid_till_m bit,
	wf_process_id nvarchar(255),
	wf_process_id_m bit,
	automatic_role_id binary(16),
	automatic_role_m bit,
	identity_contract_id binary(16),
	identity_contract_m bit,
	identity_role_id binary(16),
	identity_role_m bit,
	role_id binary(16),
	role_m bit,
	request_role_id binary(16),
	role_request_m bit,
	CONSTRAINT idm_concept_role_request_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_s00svvhj93oge2mlgotun5nuv FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_confidential_storage_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	storage_key nvarchar(255),
	key_m bit,
	owner_id binary(16),
	owner_id_m bit,
	owner_type nvarchar(255),
	owner_type_m bit,
	CONSTRAINT idm_confidential_storage_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_agmqlakgyyjnhyx4up05iim52 FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_configuration_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	confidential bit,
	confidential_m bit,
	name nvarchar(255),
	name_m bit,
	secured bit,
	secured_m bit,
	value nvarchar(255),
	value_m bit,
	CONSTRAINT idm_configuration_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_i8it9y3yjy5196fbs7cpn7faq FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_contract_guarantee_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	external_id nvarchar(255),
	external_id_m bit,
	guarantee_id binary(16),
	guarantee_m bit,
	identity_contract_id binary(16),
	identity_contract_m bit,
	CONSTRAINT idm_contract_guarantee_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_dglppsjqr3kdnqtdbfipbtma5 FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_contract_slice_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	contract_code nvarchar(255),
	contract_code_m bit,
	contract_valid_from datetime2(6),
	contract_valid_from_m bit,
	contract_valid_till datetime2(6),
	contract_valid_till_m bit,
	description nvarchar(2000),
	description_m bit,
	disabled bit,
	disabled_m bit,
	external_id nvarchar(255),
	external_id_m bit,
	externe bit,
	externe_m bit,
	main bit,
	main_m bit,
	[position] nvarchar(255),
	position_m bit,
	state nvarchar(45),
	state_m bit,
	using_as_contract bit,
	using_as_contract_m bit,
	valid_from datetime2(6),
	valid_from_m bit,
	valid_till datetime2(6),
	valid_till_m bit,
	identity_id binary(16),
	identity_m bit,
	parent_contract_id binary(16),
	parent_contract_m bit,
	work_position_id binary(16),
	work_position_m bit,
	CONSTRAINT idm_contract_slice_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_fdq20kiyy3x2ly1meht7exxd1 FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_contract_slice_guarantee_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	contract_slice_id binary(16),
	contract_slice_m bit,
	guarantee_id binary(16),
	guarantee_m bit,
	CONSTRAINT idm_contract_slice_guarantee_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_nbb01g3t4g9o0hlcxxq7e75ci FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_form_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	name nvarchar(255),
	name_m bit,
	owner_code nvarchar(255),
	owner_code_m bit,
	owner_id binary(16),
	owner_id_m bit,
	owner_type nvarchar(255),
	owner_type_m bit,
	CONSTRAINT idm_form_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_idm_form_rev FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_form_attribute_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	code nvarchar(255),
	code_m bit,
	confidential bit,
	confidential_m bit,
	default_value nvarchar(MAX),
	default_value_m bit,
	description nvarchar(2000),
	description_m bit,
	face_type nvarchar(45),
	face_type_m bit,
	multiple bit,
	multiple_m bit,
	name nvarchar(255),
	name_m bit,
	persistent_type nvarchar(45),
	persistent_type_m bit,
	placeholder nvarchar(255),
	placeholder_m bit,
	readonly bit,
	readonly_m bit,
	required bit,
	required_m bit,
	seq smallint,
	seq_m bit,
	unmodifiable bit,
	unmodifiable_m bit,
	definition_id binary(16),
	form_definition_m bit,
	CONSTRAINT idm_form_attribute_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_87mqe2oxk3u5udokcqbpmd1s5 FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_form_definition_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	code nvarchar(255),
	code_m bit,
	description nvarchar(2000),
	description_m bit,
	main bit,
	main_m bit,
	name nvarchar(255),
	name_m bit,
	definition_type nvarchar(255),
	type_m bit,
	CONSTRAINT idm_form_definition_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_mpbe9mt8v8mbmomjiftoxftlf FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_form_value_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	boolean_value bit,
	boolean_value_m bit,
	byte_value varbinary(255),
	byte_value_m bit,
	confidential bit,
	confidential_m bit,
	date_value datetime2(6),
	date_value_m bit,
	double_value numeric(38,4),
	double_value_m bit,
	long_value numeric(19,0),
	long_value_m bit,
	persistent_type nvarchar(45),
	persistent_type_m bit,
	seq smallint,
	seq_m bit,
	short_text_value nvarchar(2000),
	short_text_value_m bit,
	string_value nvarchar(MAX),
	string_value_m bit,
	uuid_value binary(16),
	uuid_value_m bit,
	attribute_id binary(16),
	form_attribute_m bit,
	owner_id binary(16),
	owner_m bit,
	CONSTRAINT idm_form_value_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_idm_form_value_rev FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_i_contract_form_value_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	boolean_value bit,
	boolean_value_m bit,
	byte_value varbinary(255),
	byte_value_m bit,
	confidential bit,
	confidential_m bit,
	date_value datetime2(6),
	date_value_m bit,
	double_value numeric(38,4),
	double_value_m bit,
	long_value numeric(19,0),
	long_value_m bit,
	persistent_type nvarchar(45),
	persistent_type_m bit,
	seq smallint,
	seq_m bit,
	short_text_value nvarchar(2000),
	short_text_value_m bit,
	string_value nvarchar(MAX),
	string_value_m bit,
	uuid_value binary(16),
	uuid_value_m bit,
	attribute_id binary(16),
	form_attribute_m bit,
	owner_id binary(16),
	owner_m bit,
	CONSTRAINT idm_i_contract_form_value_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_qu8vg2k1wembpdp2jj1smb5s5 FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_identity_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	description nvarchar(2000),
	description_m bit,
	disabled bit,
	disabled_m bit,
	email nvarchar(255),
	email_m bit,
	external_code nvarchar(255),
	external_code_m bit,
	external_id nvarchar(255),
	external_id_m bit,
	first_name nvarchar(255),
	first_name_m bit,
	last_name nvarchar(255),
	last_name_m bit,
	phone nvarchar(30),
	phone_m bit,
	state nvarchar(45),
	state_m bit,
	title_after nvarchar(100),
	title_after_m bit,
	title_before nvarchar(100),
	title_before_m bit,
	username nvarchar(255),
	username_m bit,
	CONSTRAINT idm_identity_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_4pm45eprpsmhy5qnrkbgua56c FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_identity_contract_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	description nvarchar(2000),
	description_m bit,
	disabled bit,
	disabled_m bit,
	external_id nvarchar(255),
	external_id_m bit,
	externe bit,
	externe_m bit,
	main bit,
	main_m bit,
	[position] nvarchar(255),
	position_m bit,
	state nvarchar(45),
	state_m bit,
	valid_from datetime2(6),
	valid_from_m bit,
	valid_till datetime2(6),
	valid_till_m bit,
	identity_id binary(16),
	identity_m bit,
	work_position_id binary(16),
	work_position_m bit,
	CONSTRAINT idm_identity_contract_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_o27u52ytamblwynwf958a7rna FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_identity_form_value_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	boolean_value bit,
	boolean_value_m bit,
	byte_value varbinary(255),
	byte_value_m bit,
	confidential bit,
	confidential_m bit,
	date_value datetime2(6),
	date_value_m bit,
	double_value numeric(38,4),
	double_value_m bit,
	long_value numeric(19,0),
	long_value_m bit,
	persistent_type nvarchar(45),
	persistent_type_m bit,
	seq smallint,
	seq_m bit,
	short_text_value nvarchar(2000),
	short_text_value_m bit,
	string_value nvarchar(MAX),
	string_value_m bit,
	uuid_value binary(16),
	uuid_value_m bit,
	attribute_id binary(16),
	form_attribute_m bit,
	owner_id binary(16),
	owner_m bit,
	CONSTRAINT idm_identity_form_value_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_j85k7gu9fuv136lcim1yf442d FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_identity_role_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	external_id nvarchar(255),
	external_id_m bit,
	valid_from datetime2(6),
	valid_from_m bit,
	valid_till datetime2(6),
	valid_till_m bit,
	automatic_role_id binary(16),
	automatic_role_m bit,
	identity_contract_id binary(16),
	identity_contract_m bit,
	role_id binary(16),
	role_m bit,
	CONSTRAINT idm_identity_role_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_6f8hi53e37sxhyti4xlxx0k34 FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_notification_configuration_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	description nvarchar(2000),
	description_m bit,
	notification_type nvarchar(255),
	notification_type_m bit,
	topic nvarchar(255),
	topic_m bit,
	template_id binary(16),
	template_m bit,
	CONSTRAINT idm_notification_configuration_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_198fj5oy8o3fejsnhvryul3p8 FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_notification_template_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	body_html nvarchar(MAX),
	body_html_m bit,
	body_text nvarchar(MAX),
	body_text_m bit,
	code nvarchar(255),
	code_m bit,
	module nvarchar(255),
	module_m bit,
	name nvarchar(255),
	name_m bit,
	[parameter] nvarchar(255),
	parameter_m bit,
	sender nvarchar(255),
	sender_m bit,
	subject nvarchar(255),
	subject_m bit,
	CONSTRAINT idm_notification_template_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_pxuc0kajt3bs0malyc4iy86tt FOREIGN KEY (rev) REFERENCES idm_audit(id)
);

CREATE TABLE idm_password_policy_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	block_login_time int,
	block_login_time_m bit,
	default_policy bit,
	default_policy_m bit,
	description nvarchar(2000),
	description_m bit,
	disabled bit,
	disabled_m bit,
	enchanced_control bit,
	enchanced_control_m bit,
	generate_type nvarchar(255),
	generate_type_m bit,
	identity_attribute_check nvarchar(255),
	identity_attribute_check_m bit,
	lower_char_base nvarchar(255),
	lower_char_base_m bit,
	lower_char_required bit,
	lower_char_required_m bit,
	max_history_similar int,
	max_history_similar_m bit,
	max_password_age int,
	max_password_age_m bit,
	max_password_length int,
	max_password_length_m bit,
	max_unsuccessful_attempts int,
	max_unsuccessful_attempts_m bit,
	min_lower_char int,
	min_lower_char_m bit,
	min_number int,
	min_number_m bit,
	min_password_age int,
	min_password_age_m bit,
	min_password_length int,
	min_password_length_m bit,
	min_rules_to_fulfill int,
	min_rules_to_fulfill_m bit,
	min_special_char int,
	min_special_char_m bit,
	min_upper_char int,
	min_upper_char_m bit,
	name nvarchar(255),
	name_m bit,
	number_base nvarchar(255),
	number_base_m bit,
	number_required bit,
	number_required_m bit,
	passphrase_words int,
	passphrase_words_m bit,
	password_length_required bit,
	password_length_required_m bit,
	prohibited_characters nvarchar(255),
	prohibited_characters_m bit,
	special_char_base nvarchar(255),
	special_char_base_m bit,
	special_char_required bit,
	special_char_required_m bit,
	[type] nvarchar(255),
	type_m bit,
	upper_char_base nvarchar(255),
	upper_char_base_m bit,
	upper_char_required bit,
	upper_char_required_m bit,
	weak_pass nvarchar(255),
	weak_pass_m bit,
	weak_pass_required bit,
	weak_pass_required_m bit,
	CONSTRAINT idm_password_policy_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_swya50ugpjfiuh5hdw1k1wgbm FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_profile_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	preferred_language nvarchar(45),
	preferred_language_m bit,
	identity_id binary(16),
	identity_m bit,
	image_id binary(16),
	image_m bit,
	CONSTRAINT idm_profile_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_l70tqy66dd7lcx5by8egpg11a FOREIGN KEY (rev) REFERENCES idm_audit(id)
);



CREATE TABLE idm_password_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	block_login_date datetime2(6),
	block_login_date_m bit,
	last_successful_login datetime2(6),
	last_successful_login_m bit,
	must_change bit,
	must_change_m bit,
	unsuccessful_attempts int,
	unsuccessful_attempts_m bit,
	valid_from datetime2(6),
	valid_from_m bit,
	valid_till datetime2(6),
	valid_till_m bit,
	identity_id binary(16),
	identity_m bit,
	CONSTRAINT idm_password_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_1t5dpjknl7r4lxhfauqu1h2tm FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_role_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	approve_remove bit,
	approve_remove_m bit,
	can_be_requested bit,
	can_be_requested_m bit,
	description nvarchar(2000),
	description_m bit,
	disabled bit,
	disabled_m bit,
	external_id nvarchar(255),
	external_id_m bit,
	name nvarchar(255),
	name_m bit,
	code nvarchar(255),
	code_m bit,
	priority int,
	priority_m bit,
	role_type nvarchar(255),
	role_type_m bit,
	CONSTRAINT idm_role_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_nwuujf2vpbphudbfcvxdj3exp FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_role_catalogue_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	code nvarchar(255),
	code_m bit,
	description nvarchar(2000),
	description_m bit,
	external_id nvarchar(255),
	external_id_m bit,
	name nvarchar(255),
	name_m bit,
	url nvarchar(255),
	url_m bit,
	url_title nvarchar(255),
	url_title_m bit,
	parent_id binary(16),
	parent_m bit,
	CONSTRAINT idm_role_catalogue_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_pirc5x13v836h3hmyt0cc42kv FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_role_catalogue_role_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	role_id binary(16),
	role_m bit,
	role_catalogue_id binary(16),
	role_catalogue_m bit,
	CONSTRAINT idm_role_catalogue_role_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_l085dvgnnwk6e46daedndy6sr FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_role_composition_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	sub_id binary(16),
	sub_m bit,
	superior_id binary(16),
	superior_m bit,
	CONSTRAINT idm_role_composition_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_a8jshb3t1vrfp1vp0819x6h8q FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_role_form_value_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	boolean_value bit,
	boolean_value_m bit,
	byte_value varbinary(255),
	byte_value_m bit,
	confidential bit,
	confidential_m bit,
	date_value datetime2(6),
	date_value_m bit,
	double_value numeric(38,4),
	double_value_m bit,
	long_value numeric(19,0),
	long_value_m bit,
	persistent_type nvarchar(45),
	persistent_type_m bit,
	seq smallint,
	seq_m bit,
	short_text_value nvarchar(2000),
	short_text_value_m bit,
	string_value nvarchar(MAX),
	string_value_m bit,
	uuid_value binary(16),
	uuid_value_m bit,
	attribute_id binary(16),
	form_attribute_m bit,
	owner_id binary(16),
	owner_m bit,
	CONSTRAINT idm_role_form_value_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_gfx77xleqng5i3jipnm4648sr FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_role_guarantee_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	guarantee_id binary(16),
	guarantee_m bit,
	role_id binary(16),
	role_m bit,
	CONSTRAINT idm_role_guarantee_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_spgix52wl4u4ajm06ha9jiajv FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_role_guarantee_role_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	guarantee_role_id binary(16),
	guarantee_role_m bit,
	role_id binary(16),
	role_m bit,
	CONSTRAINT idm_role_guarantee_role_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_52w0m9i93h9galqewk813qqd3 FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_role_request_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	description nvarchar(255),
	description_m bit,
	execute_immediately bit,
	execute_immediately_m bit,
	requested_by_type nvarchar(255),
	requested_by_type_m bit,
	state nvarchar(255),
	state_m bit,
	wf_process_id nvarchar(255),
	wf_process_id_m bit,
	applicant_id binary(16),
	applicant_m bit,
	duplicated_to_request binary(16),
	duplicated_to_request_m bit,
	CONSTRAINT idm_role_request_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_o61enmny8d3wffg87pvfhi2m3 FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_role_tree_node_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	recursion_type nvarchar(255),
	recursion_type_m bit,
	tree_node_id binary(16),
	tree_node_m bit,
	CONSTRAINT idm_role_tree_node_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_gvppr8wma5biag712dyjedvjf FOREIGN KEY (id,rev) REFERENCES idm_auto_role_a(id,rev)
);


CREATE TABLE idm_scheduled_task_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	quartz_task_name nvarchar(255),
	quartz_task_name_m bit,
	CONSTRAINT idm_scheduled_task_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_mdjjd0gqna0tw0ewqf571abj5 FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_script_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	category nvarchar(255),
	category_m bit,
	code nvarchar(255),
	code_m bit,
	description nvarchar(2000),
	description_m bit,
	name nvarchar(255),
	name_m bit,
	script nvarchar(MAX),
	script_m bit,
	CONSTRAINT idm_script_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_a0o0bhr6om9dd80tdawuennqp FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_script_authority_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	class_name nvarchar(255),
	class_name_m bit,
	service nvarchar(255),
	service_m bit,
	[type] nvarchar(255),
	type_m bit,
	script_id binary(16),
	script_m bit,
	CONSTRAINT idm_script_authority_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_idm_script_authority_a_rev FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_tree_node_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	code nvarchar(255),
	code_m bit,
	disabled bit,
	disabled_m bit,
	external_id nvarchar(255),
	external_id_m bit,
	name nvarchar(255),
	name_m bit,
	parent_id binary(16),
	parent_m bit,
	tree_type_id binary(16),
	tree_type_m bit,
	CONSTRAINT idm_tree_node_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_pfe3d9yuunnvu0caum686sji8 FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_tree_node_form_value_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	boolean_value bit,
	boolean_value_m bit,
	byte_value varbinary(255),
	byte_value_m bit,
	confidential bit,
	confidential_m bit,
	date_value datetime2(6),
	date_value_m bit,
	double_value numeric(38,4),
	double_value_m bit,
	long_value numeric(19,0),
	long_value_m bit,
	persistent_type nvarchar(45),
	persistent_type_m bit,
	seq smallint,
	seq_m bit,
	short_text_value nvarchar(2000),
	short_text_value_m bit,
	string_value nvarchar(MAX),
	string_value_m bit,
	uuid_value binary(16),
	uuid_value_m bit,
	attribute_id binary(16),
	form_attribute_m bit,
	owner_id binary(16),
	owner_m bit,
	CONSTRAINT idm_tree_node_form_value_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_82r94pui8abb9sapy4y2a07g7 FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_tree_type_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	code nvarchar(255),
	code_m bit,
	external_id nvarchar(255),
	external_id_m bit,
	name nvarchar(255),
	name_m bit,
	CONSTRAINT idm_tree_type_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_f9ipost6xa40kv7bu5gqti3sa FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE revchanges (
	rev numeric(19,0) NOT NULL,
	modified_entity_names nvarchar(255),
	CONSTRAINT fk_et6b2lrkqkab5mhvxkv861n8h FOREIGN KEY (rev) REFERENCES idm_audit(id)
);
CREATE INDEX idx_revchanges ON revchanges (rev);

