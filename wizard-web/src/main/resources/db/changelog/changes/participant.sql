--liquibase formatted sql
--changeset Dilip:1
CREATE TABLE participant (
	id UUID PRIMARY KEY NOT NULL,
	did varchar(200) NOT NULL,
	email varchar(100) NOT NULL,
	legal_name varchar(200) NOT NULL,
	short_name varchar(200) NOT NULL,
	entity_type_id UUID NULL,
    sub_domain varchar(100) NULL,
    private_key_id varchar(100) NULL,
    participant_type varchar(100)) NULL,
	created_at timestamp(6) NULL,
	updated_at timestamp(6) NULL
);

CREATE TABLE credential (
    id UUID PRIMARY KEY NOT NULL,
    vc_url TEXT NOT NULL,
    vc_json TEXT NOT NULL,
    type VARCHAR(10) NOT NULL,
    participant_id UUID NOT NULL,
    metadata TEXT NULL,
    created_at timestamp(6) NULL,
    updated_at timestamp(6) NULL,
    CONSTRAINT fk_participant_id FOREIGN KEY (participant_id) REFERENCES participant(id)
);