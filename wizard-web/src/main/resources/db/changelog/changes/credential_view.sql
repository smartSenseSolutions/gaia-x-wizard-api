CREATE OR REPLACE
VIEW credential_view AS
SELECT
  c.id,
  CASE
    WHEN c.type = 'legal_participant' THEN (
    SELECT
      p.LEGAL_NAME
    FROM
      participant p
    WHERE
      c.PARTICIPANT_ID = p.ID)
    WHEN c.type = 'service_offer' THEN (
    SELECT
      s.name
    FROM
      service_offer s
    WHERE
      s.credential_id = c.id)
    WHEN c.type = 'resource' THEN (
    SELECT
      r.name
    FROM
      resource r
    WHERE
      r.credential_id = c.id)
  END AS name,
  CASE
    WHEN c.type = 'legal_participant' THEN 'gx:LegalParticipant'
    WHEN c.type = 'service_offer' THEN 'gx:ServiceOffering'
    WHEN c.type = 'resource' THEN (
    SELECT
      r.type
    FROM
      resource r
    WHERE
      r.credential_id = c.id)
  END AS type,
  c.vc_url,
  c.participant_id,
  c.created_at,
  c.updated_at
FROM
  credential c
WHERE
  c.type IN ('legal_participant', 'service_offer', 'resource');