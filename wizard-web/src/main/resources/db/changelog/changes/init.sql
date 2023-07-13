--liquibase formatted sql
--changeset Nitin:1
CREATE TABLE public.enterprise (
	id bigserial NOT NULL,
	created_at timestamp(6) NULL,
	created_by int8 NULL,
	updated_at timestamp(6) NULL,
	updated_by int8 NULL,
	email varchar(255) NOT NULL,
	headquarter_address varchar(255) NOT NULL,
	legal_address varchar(255) NOT NULL,
	legal_name varchar(255) NOT NULL,
	legal_registration_number varchar(255) NOT NULL,
	legal_registration_type varchar(255) NOT NULL,
	"password" varchar(255) NOT NULL,
	status int4 NOT NULL,
	sub_domain_name varchar(255) NOT NULL,
	CONSTRAINT enterprise_pkey PRIMARY KEY (id),
	CONSTRAINT uk_4jka3x297mdgt6qcelb88flxb UNIQUE (legal_registration_number),
	CONSTRAINT uk_8wb4cq9087t2jmkxdxgi0yk2f UNIQUE (sub_domain_name),
	CONSTRAINT uk_lxkpjygh71r527lxnmgv1t0y6 UNIQUE (email),
	CONSTRAINT uk_qx10l3r22cmy8xlhmwd3r1lp6 UNIQUE (legal_name)
);

CREATE TABLE public.enterprise_certificate (
	id bigserial NOT NULL,
	created_at timestamp(6) NULL,
	created_by int8 NULL,
	updated_at timestamp(6) NULL,
	updated_by int8 NULL,
	certificate_chain varchar(255) NOT NULL,
	csr varchar(255) NOT NULL,
	enterprise_id int8 NOT NULL,
	private_key varchar(255) NOT NULL,
	CONSTRAINT enterprise_certificate_pkey PRIMARY KEY (id),
	CONSTRAINT uk_3ignb81wdb6cj1ayqg7osgm56 UNIQUE (csr),
	CONSTRAINT uk_6im4rn49d384dee9m8ws0id4f UNIQUE (certificate_chain),
	CONSTRAINT uk_9bxly6qhaj8sjcuic1qpkijvs UNIQUE (private_key),
	CONSTRAINT uk_bfg4cgnx4m548q5jh85a778gu UNIQUE (enterprise_id),
	CONSTRAINT enterprise_certificate_fk FOREIGN KEY (enterprise_id) REFERENCES public.enterprise(id)
);
CREATE INDEX enterprise_certificate_enterprise_id_idx ON public.enterprise_certificate USING btree (enterprise_id);

CREATE TABLE public.enterprise_credential (
	id bigserial NOT NULL,
	created_at timestamp(6) NULL,
	created_by int8 NULL,
	updated_at timestamp(6) NULL,
	updated_by int8 NULL,
	credentials varchar(255) NOT NULL,
	enterprise_id int8 NOT NULL,
	"label" varchar(255) NOT NULL,
	CONSTRAINT enterprise_credential_pkey PRIMARY KEY (id),
	CONSTRAINT enterprise_credential_fk FOREIGN KEY (enterprise_id) REFERENCES public.enterprise(id)
);
CREATE INDEX enterprise_credential_enterprise_id_idx ON public.enterprise_credential USING btree (enterprise_id);


--changeset Nitin:2
CREATE TABLE public.service_offer (
	id bigserial NOT NULL,
	created_at timestamp(6) NULL,
	updated_at timestamp(6) NULL,
	enterprise_id bigserial NOT NULL,
	credential_id bigserial NOT NULL,
	subject_did varchar(255) NOT NULL,
	"name" varchar(255) NOT NULL,
	produced_by varchar(255) NOT NULL,
	copyright_owned_by varchar(255) NOT NULL,
	description varchar(255) NOT NULL,
	license varchar(255) NOT NULL,
	"policy" varchar(255) NOT NULL,
	expiration_date varchar(255) NOT NULL,
	meta text NOT NULL,
	CONSTRAINT service_offer_pkey PRIMARY KEY (id),
	CONSTRAINT service_offer_fk FOREIGN KEY (enterprise_id) REFERENCES public.enterprise(id),
	CONSTRAINT service_offer_fk_1 FOREIGN KEY (credential_id) REFERENCES public.enterprise_credential(id)
);

--changeset Nitin:3
ALTER TABLE public.enterprise_credential ALTER COLUMN credentials TYPE text USING credentials::text;

--changeset Nitin:4
CREATE TABLE public.admin (
	id bigserial NOT NULL,
	user_name varchar(255) NOT NULL,
	password varchar(1000) NOT NULL,
	CONSTRAINT admin_pkey PRIMARY KEY (id)
);

--changeset Nitin:5
ALTER TABLE public.service_offer DROP COLUMN license;
ALTER TABLE public.service_offer DROP COLUMN expiration_date;
ALTER TABLE public.service_offer ADD access_type varchar(255) NULL;
ALTER TABLE public.service_offer ADD request_type varchar(255) NULL;
ALTER TABLE public.service_offer ADD format_type varchar(255) NULL;
ALTER TABLE public.service_offer ADD terms varchar(255) NULL;
ALTER TABLE public.service_offer ADD terms_hash varchar(255) NULL;

--changeset Nitin:6
ALTER TABLE public.service_offer ADD "label" varchar(255) NULL;
