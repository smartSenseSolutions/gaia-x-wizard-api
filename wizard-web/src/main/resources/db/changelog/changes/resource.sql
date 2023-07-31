     --liquibase formatted sql
--changeset Dilip:1
CREATE TABLE resource(
    id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    credential_id UUID NOT NULL,
    name varchar(50) NOT NULL,
    description varchar(1000) NULL,
    type varchar(10) NOT NULL,
    sub_type varchar(10) NULL,
    participant_id UUID NOT NULL,
    publish_to_kafka Boolean NOT NULL,
    created_at timestamp(6) NULL,
    updated_at timestamp(6) NULL,
    CONSTRAINT fk_participant_id FOREIGN KEY (participant_id) REFERENCES participant(id),
    CONSTRAINT fk_credential_id FOREIGN KEY (credential_id) REFERENCES credential(id)
);
