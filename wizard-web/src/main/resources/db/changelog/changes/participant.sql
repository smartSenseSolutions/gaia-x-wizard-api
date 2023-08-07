--liquibase formatted sql
--changeset Dilip:1
CREATE TABLE participant (
    id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
	did varchar(200) NOT NULL,
	email varchar(100) NOT NULL,
	legal_name varchar(200) NOT NULL,
	short_name varchar(200) NOT NULL,
	entity_type_id UUID NULL,
    sub_domain varchar(100) NULL,
    private_key_id varchar(100) NULL,
    participant_type varchar(100) NULL,
    own_did_solution Boolean NOT NULL,
    status INTEGER NULL,
    credential TEXT NULL,
	created_at timestamp(6) NULL,
	updated_at timestamp(6) NULL
);

CREATE TABLE credential (
    id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    vc_url TEXT NOT NULL,
    vc_json TEXT NOT NULL,
    type VARCHAR(10) NOT NULL,
    participant_id UUID NOT NULL,
    metadata TEXT NULL,
    created_at timestamp(6) NULL,
    updated_at timestamp(6) NULL,
    CONSTRAINT fk_participant_id FOREIGN KEY (participant_id) REFERENCES participant(id)
);

--changeset Dilip:2
ALTER TABLE participant ALTER COLUMN did DROP NOT NULL;

--changeset Dilip:3
ALTER TABLE credential ALTER COLUMN "type" TYPE varchar(100) USING "type"::varchar;

--changeset Dilip:4
ALTER TABLE participant ADD CONSTRAINT legal_name_unique UNIQUE (legal_name);
ALTER TABLE participant ADD CONSTRAINT short_name_unique UNIQUE (short_name);
ALTER TABLE participant ADD CONSTRAINT email_unique UNIQUE (email);