package eu.gaiax.wizard.core.service.participant.model.request;

import java.util.Map;

public record ParticipantOnboardRequest(String legalName, String shortName,
                                        //TODO credential contain legalName, shortName, MultipleRegistrationType & values,
                                        //TODO entity Type id, parent and sub organizations, headquarter and legal address
                                        Map<String, Object> credential, boolean ownDid, String entityType,
                                        String verificationMethod, String privateKey, String issuerDid) {
}
