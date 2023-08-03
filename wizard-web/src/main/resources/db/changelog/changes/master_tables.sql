--liquibase formatted sql
--changeset Neha:1
CREATE TABLE registration_type_master(
    id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    type VARCHAR(255) NOT NULL,
    active Boolean NOT NULL,
    created_at timestamp(6) NOT NULL DEFAULT NOW(),
    updated_at timestamp(6) NOT NULL DEFAULT NOW()
);

INSERT INTO registration_type_master(type, active) VALUES
('EORI', true),
('VAT ID', true),
('LEI CODE', true);


--changeset Neha:2
CREATE TABLE entity_type_master(
    id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    type VARCHAR(255) NOT NULL,
    active Boolean NOT NULL,
    created_at timestamp(6) NOT NULL DEFAULT NOW(),
    updated_at timestamp(6) NOT NULL DEFAULT NOW()
);

INSERT INTO entity_type_master(type, active) VALUES
('Limited Liability Company', true),
('Public Limited Company', true),
('General Partnership', true),
('Limited Partnership', true),
('Registered Association', true),
('Civil Law Partnership', true),
('Simplified Joint Stock Company', true),
('Real Estate Company', true),
('Private Limited Company', true),
('Partnership with Limited Liability', true),
('Social Enterprise', true),
('Charity', true),
('Partnership Limited by Shares', true),
('Professional Limited Liability Company', true),
('Single Member Limited Liability Company', true),
('Business Association', true),
('Sole Proprietorship', true),
('Cooperative', true);


--changeset Neha:3
CREATE TABLE request_type_master(
    id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    type VARCHAR(255) NOT NULL,
    active Boolean NOT NULL,
    created_at timestamp(6) NOT NULL DEFAULT NOW(),
    updated_at timestamp(6) NOT NULL DEFAULT NOW()
);

INSERT INTO request_type_master(type, active) VALUES
('API', true),
('e-mail', true),
('Webform', true),
('Unregistered Letter', true),
('Support Center', true);


--changeset Neha:4
CREATE TABLE access_type_master(
    id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    type VARCHAR(255) NOT NULL,
    active Boolean NOT NULL,
    created_at timestamp(6) NOT NULL DEFAULT NOW(),
    updated_at timestamp(6) NOT NULL DEFAULT NOW()
);

INSERT INTO access_type_master(type, active) VALUES
('Physical', true),
('Digital', true);


--changeset Neha:5
CREATE TABLE standard_type_master(
    id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    type VARCHAR(255) NOT NULL,
    active Boolean NOT NULL,
    created_at timestamp(6) NOT NULL DEFAULT NOW(),
    updated_at timestamp(6) NOT NULL DEFAULT NOW()
);

INSERT INTO standard_type_master(type, active) VALUES
('GDPR2016', true),
('LGPD2019', true),
('PDPA2012', true),
('CCPA2018', true),
('VCDPA2021', true);


--changeset Neha:6
CREATE TABLE label_level_type_master(
    id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    active Boolean NOT NULL,
    created_at timestamp(6) NOT NULL DEFAULT NOW(),
    updated_at timestamp(6) NOT NULL DEFAULT NOW()
);

INSERT INTO label_level_type_master(name, active) VALUES
('Contractual governance', true),
('General material requirements and transparency', true),
('Technical compliance requirements', true),
('Data Protection - General', true),
('Data Protection - GDPR Art. 26', true),
('Data Protection - GDPR Art. 28', true),
('Cybersecurity', true),
('Portability - Switching and porting of Customer Data', true),
('European Control - General', true),
('European Control - Access to Customer Data', true),
('Data Exchange Services', true);


--changeset Neha:7
CREATE TYPE applicable_level_criteria AS ENUM ('NA', 'MANDATORY_DECLARATION', 'APPLICABLE');
CREATE TABLE label_level_question_master(
    id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    type_id UUID NOT NULL,
    criterion_number VARCHAR(20) NOT NULL,
    question TEXT NOT NULL,
    active Boolean NOT NULL,
    basic_conformity applicable_level_criteria NULL,
    level_1 applicable_level_criteria NULL,
    created_at timestamp(6) NOT NULL DEFAULT NOW(),
    updated_at timestamp(6) NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_label_level_type_id FOREIGN KEY (type_id) REFERENCES label_level_type_master(id)
);

INSERT INTO label_level_question_master(criterion_number, question, active, basic_conformity, level_1, type_id) VALUES
('Criterion P1.1.1', 'The Provider shall offer the ability to establish a legally binding act. This legally binding act shall be documented.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Contractual governance')),
('Criterion P1.1.2', 'The Provider shall have an option for each legally binding act to be governed by EU/EEA/Member State law.', true, 'NA', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Contractual governance')),
('Criterion P1.1.3', 'The Provider shall clearly identify for which parties the legal act is binding.', true, 'NA', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Contractual governance')),
('Criterion P1.1.4', 'The Provider shall ensure that the legally binding act covers the entire provision of the Service Offering', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Contractual governance')),
('Criterion P1.1.5', 'The Provider shall clearly identity for each legally binding act its governing law.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Contractual governance')),

('Criterion P1.2.1', 'The Provider shall ensure there are specific provisions regarding service interruptions and business continuity (e.g., by means of a service level agreement), Provider’s bankruptcy or any other reason by which the Provider may cease to exist in law.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'General material requirements and transparency')),
('Criterion P1.2.2', 'The Provider shall ensure there are provisions governing the rights of the parties to use the service and any Customer Data therein.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'General material requirements and transparency')),
('Criterion P1.2.3', 'The Provider shall ensure there are provisions governing changes, regardless of their kind.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'General material requirements and transparency')),
('Criterion P1.2.4', 'The Provider shall ensure there are provisions governing aspects regarding copyright or any other intellectual property rights.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'General material requirements and transparency')),
('Criterion P1.2.5', 'The Provider shall declare the general location of physicals Resources at urban area level.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'General material requirements and transparency')),
('Criterion P1.2.6', 'The Provider shall explain how information about subcontractors and related Customer Data localization will be communicated.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'General material requirements and transparency')),
('Criterion P1.2.7', 'The Provider shall communicate to the Customer where the applicable jurisdiction(s) of subcontractors will be.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'General material requirements and transparency')),
('Criterion P1.2.8', 'The Provider shall include in the contract the contact details where Customer may address any queries regarding the Service Offering and the contract.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'General material requirements and transparency')),
('Criterion P1.2.9', 'The Provider shall adopt the Gaia-X Trust Framework, by which Customers may verify Provider’s Service Offering.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'General material requirements and transparency')),
('Criterion P1.2.10', 'The Provider shall provide transparency on the environmental impact of the Service Offering provided', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'General material requirements and transparency')),

('Criterion P1.3.1', 'Service Offering shall include a policy using a common Domain-Specific Language (DSL) to describe Permissions, Requirements and Constraints.', true, 'APPLICABLE', 'APPLICABLE', (SELECT id FROM label_level_type_master where name = 'Technical compliance requirements')),
('Criterion P1.3.2', 'Service Offering requires being operated by Service Offering Provider with a verified identity.', true, 'APPLICABLE', 'APPLICABLE', (SELECT id FROM label_level_type_master where name = 'Technical compliance requirements')),
('Criterion P1.3.3', 'Service Offering must provide a conformant self-description.', true, 'APPLICABLE', 'APPLICABLE', (SELECT id FROM label_level_type_master where name = 'Technical compliance requirements')),
('Criterion P1.3.4', 'Self-Description attributes need to be consistent across linked Self-Descriptions.', true, 'APPLICABLE', 'APPLICABLE', (SELECT id FROM label_level_type_master where name = 'Technical compliance requirements')),
('Criterion P1.3.5', 'The Provider shall ensure that the Consumer uses a verified identity provided by the Federator.', true, 'APPLICABLE', 'APPLICABLE', (SELECT id FROM label_level_type_master where name = 'Technical compliance requirements')),

('Criterion P2.1.1', 'The Provider shall offer the ability to establish a contract under Union or EU/EEA/Member State law and specifically addressing GDPR requirements.', true, 'NA', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Data Protection - General')),
('Criterion P2.1.2', 'The Provider shall define the roles and responsibilities of each party.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Data Protection - General')),
('Criterion P2.1.3', 'The Provider shall clearly define the technical and organizational measures in accordance with the roles and responsibilities of the parties, including an adequate level of detail.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Data Protection - General')),

('Criterion P2.2.1', 'The Provider shall be ultimately bound to instructions of the Customer.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Data Protection - GDPR Art. 28')),
('Criterion P2.2.2', 'The Provider shall clearly define how Customer may instruct, including by electronic means such as configuration tools or APIs.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Data Protection - GDPR Art. 28')),
('Criterion P2.2.3', 'The Provider shall clearly define if and to which extent third country transfer will take place.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Data Protection - GDPR Art. 28')),
('Criterion P2.2.4', 'The Provider shall clearly define if and to the extent third country transfers will take place, and by which means of Chapter V GDPR these transfers will be protected.', true, 'NA', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Data Protection - GDPR Art. 28')),
('Criterion P2.2.5', 'The Provider shall clearly define if and to which extent sub-processors will be involved.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Data Protection - GDPR Art. 28')),
('Criterion P2.2.6', 'The Provider shall clearly define if and to the extent sub-processors will be involved, and the measures that are in place regarding sub-processors management.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Data Protection - GDPR Art. 28')),
('Criterion P2.2.7', 'The Provider shall define the audit rights for the Customer.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Data Protection - GDPR Art. 28')),

('Criterion P2.3.1', 'In case of a joint controllership, the Provider shall ensure an arrangement pursuant to Art. 26 (1) GDPR is in place.', true, 'NA', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Data Protection - GDPR Art. 26')),
('Criterion P2.3.2', 'In case of a joint controllership, at a minimum, the Provider shall ensure that the very essence of such agreement is communicated to data subjects.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Data Protection - GDPR Art. 26')),
('Criterion P2.3.3', 'In case of a joint controllership, the Provider shall publish a point of contact for data subjects.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Data Protection - GDPR Art. 26')),

('Criterion P3.1.1', 'Organization of information security: Plan, implement, maintain and continuously improve the information security framework within the organisation.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Cybersecurity')),
('Criterion P3.1.2', 'Information Security Policies: Provide a global information security policy, derived into policies and procedures regarding security requirements and to support business requirements', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Cybersecurity')),
('Criterion P3.1.3', 'Risk Management: Ensure that risks related to information security are properly identified, assessed, and treated, and that the residual risk is acceptable to the CSP.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Cybersecurity')),
('Criterion P3.1.4', 'Human Resources: Ensure that employees understand their responsibilities, are aware of their responsibilities with regard to information security, and that the organisation’s assets are protected in the event of changes in responsibilities or termination.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Cybersecurity')),
('Criterion P3.1.5', 'Asset Management: Identify the organisation’s own assets and ensure an appropriate level of protection throughout their lifecycle.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Cybersecurity')),
('Criterion P3.1.6', 'Physical Security: Prevent unauthorised physical access and protect against theft, damage, loss and outage of operations.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Cybersecurity')),
('Criterion P3.1.7', 'Operational Security: Ensure proper and regular operation, including appropriate measures for planning and monitoring capacity, protection against malware, logging and monitoring events, and dealing with vulnerabilities, malfunctions and failures.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Cybersecurity')),
('Criterion P3.1.8', 'Identity, Authentication and access control management: Limit access to information and information processing facilities.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Cybersecurity')),
('Criterion P3.1.9', 'Cryptography and Key management: Ensure appropriate and effective use of cryptography to protect the confidentiality, authenticity or integrity of information.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Cybersecurity')),
('Criterion P3.1.10', 'Communication Security: Ensure the protection of information in networks and the corresponding information processing systems.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Cybersecurity')),
('Criterion P3.1.11', 'Portability and Interoperability: The CSP shall provide a means by which a customer can obtain their stored customer data, and provide documentation on how (where appropriate, through documented API’s) the CSC can obtain the stored data at the end of the contractual relationship and shall document how the data will be securely deleted from the Cloud Service Provider in what timeframe.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Cybersecurity')),
('Criterion P3.1.12', 'Change and Configuration Management: Ensure that changes and configuration actions to information systems guarantee the security of the delivered cloud service.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Cybersecurity')),
('Criterion P3.1.13', 'Development of Information systems: Ensure information security in the development cycle of information systems.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Cybersecurity')),
('Criterion P3.1.14', 'Procurement Management: Ensure the protection of information that suppliers of the CSP can access and monitor the agreed services and security requirements.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Cybersecurity')),
('Criterion P3.1.15', 'Incident Management: Ensure a consistent and comprehensive approach to the capture, assessment, communication and escalation of security incidents.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Cybersecurity')),
('Criterion P3.1.16', 'Business Continuity: Plan, implement, maintain and test procedures and measures for business continuity and emergency management.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Cybersecurity')),
('Criterion P3.1.17', 'Compliance: Avoid non-compliance with legal, regulatory, self-imposed or contractual information security and compliance requirements.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Cybersecurity')),
('Criterion P3.1.18', 'User documentation: Provide up-to-date information on the secure configuration and known vulnerabilities of the cloud service for cloud customers.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Cybersecurity')),
('Criterion P3.1.19', 'Dealing with information requests from government agencies: Ensure appropriate handling of government investigation requests for legal review, information to cloud customers, and limitation of access to or disclosure of Customer Data.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Cybersecurity')),
('Criterion P3.1.20', 'Product safety and security: Provide appropriate mechanisms for cloud customers to enable product safety and security.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Cybersecurity')),

('Criterion P4.1.1', 'The Provider shall implement practices for facilitating the switching of Providers and the porting of Customer Data in a structured, commonly used and machine-readable format including open standard formats where required or requested by the Customer.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Portability - Switching and porting of Customer Data')),
('Criterion P4.1.2', 'The Provider shall ensure pre-contractual information exists, with sufficiently detailed, clear and transparent information regarding the processes of Customer Data portability, technical requirements, timeframes and charges that apply in case a professional user wants to switch to another Provider or port Customer Data back to its own IT systems.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'Portability - Switching and porting of Customer Data')),

('Criterion P5.2.1', 'The Provider shall not access Customer Data unless authorized by the Customer or when the access is in accordance with EU/EEA/Member State law.', true, 'MANDATORY_DECLARATION', 'MANDATORY_DECLARATION', (SELECT id FROM label_level_type_master where name = 'European Control - Access to Customer Data'));
