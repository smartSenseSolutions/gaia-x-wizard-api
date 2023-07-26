--liquibase formatted sql
--changeset Dilip:1
CREATE TABLE service_offer(
    id UUID PRIMARY KEY NOT NULL,
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
    id UUID PRIMARY KEY NOT NULL,
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
    id UUID PRIMARY KEY NOT NULL,
    participant_id UUID NOT NULL,
    service_offer_id UUID NOT NULL,
    file_path varchar(200) NOT NULL,
    description varchar(100) NULL,
    created_at timestamp(6) NULL,
    updated_at timestamp(6) NULL,
    CONSTRAINT fk_participant_id FOREIGN KEY (participant_id) REFERENCES participant(id),
    CONSTRAINT fk_service_offer_id FOREIGN KEY (service_offer_id) REFERENCES service_offer(id)
);