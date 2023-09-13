--liquibase formatted sql
--changeset Dilip:1
CREATE TABLE service_offer(
    id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    credential_id UUID NOT NULL,
    name varchar(50) NOT NULL,
    description varchar(1000) NULL,
    participant_id UUID NOT NULL,
    created_at timestamp(6) NULL,
    updated_at timestamp(6) NULL,
    CONSTRAINT fk_participant_id FOREIGN KEY (participant_id) REFERENCES participant(id),
    CONSTRAINT fk_credential_id FOREIGN KEY (credential_id) REFERENCES credential(id)
);

CREATE TABLE label_level_answer(
    id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    participant_id UUID NOT NULL,
    service_offer_id UUID NOT NULL,
    question_id UUID NOT NULL,
    answer Boolean NOT NULL,
    created_at timestamp(6) NULL,
    updated_at timestamp(6) NULL,
    CONSTRAINT fk_participant_id FOREIGN KEY (participant_id) REFERENCES participant(id),
    CONSTRAINT fk_service_offer_id FOREIGN KEY (service_offer_id) REFERENCES service_offer(id),
    CONSTRAINT fk_label_level_question_id FOREIGN KEY (question_id) REFERENCES label_level_question_master(id)
);

CREATE TABLE label_level_upload_files(
    id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    participant_id UUID NOT NULL,
    service_offer_id UUID NOT NULL,
    file_path varchar(200) NOT NULL,
    description varchar(100) NULL,
    created_at timestamp(6) NULL,
    updated_at timestamp(6) NULL,
    CONSTRAINT fk_participant_id FOREIGN KEY (participant_id) REFERENCES participant(id),
    CONSTRAINT fk_service_offer_id FOREIGN KEY (service_offer_id) REFERENCES service_offer(id)
);

--changeset Mittal:2
ALTER TABLE service_offer ADD veracity_data text NULL;
--changeset Neha:3
ALTER TABLE service_offer ADD label_level int4 NULL;
CREATE TABLE service_offer_standard_type(
    id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    service_offer_id UUID NOT NULL,
    standard_type_id UUID NOT NULL,
    CONSTRAINT fk_service_offer_id FOREIGN KEY (service_offer_id) REFERENCES service_offer(id),
    CONSTRAINT fk_standard_type_id FOREIGN KEY (standard_type_id) REFERENCES standard_type_master(id)
);

--changeset Mittal:4

CREATE TABLE service_label_level(
    id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    credential_id UUID NOT NULL,
    participant_id UUID NOT NULL,
    service_offer_id UUID NOT NULL,
    created_at timestamp(6) NULL,
    updated_at timestamp(6) NULL,
    CONSTRAINT fk_participant_id FOREIGN KEY (participant_id) REFERENCES participant(id),
    CONSTRAINT fk_service_offer FOREIGN KEY (service_offer_id) REFERENCES service_offer(id),
    CONSTRAINT fk_credential_id FOREIGN KEY (credential_id) REFERENCES credential(id)
);

--changeset Neha:5
ALTER TABLE service_offer ADD message_reference_id varchar(50) NULL;

--changeset Neha:6
ALTER TABLE service_offer ALTER COLUMN label_level TYPE varchar(20) USING label_level::varchar;

--changeset Neha:7
ALTER TABLE service_offer ALTER COLUMN name TYPE varchar(255) USING name::varchar;

--changeset Neha:8
ALTER TABLE service_offer ALTER COLUMN name TYPE text USING name::text;
ALTER TABLE service_offer ALTER COLUMN description TYPE text USING description::text;