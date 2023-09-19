package eu.gaiax.wizard.core.service.service_offer;

import com.smartsensesolutions.java.commons.base.repository.BaseRepository;
import com.smartsensesolutions.java.commons.base.service.BaseService;
import com.smartsensesolutions.java.commons.specification.SpecificationUtil;
import eu.gaiax.wizard.api.exception.BadDataException;
import eu.gaiax.wizard.api.model.CredentialTypeEnum;
import eu.gaiax.wizard.api.model.did.ServiceEndpointConfig;
import eu.gaiax.wizard.api.model.service_offer.LabelLevelFileUpload;
import eu.gaiax.wizard.api.model.service_offer.LabelLevelRequest;
import eu.gaiax.wizard.api.model.setting.ContextConfig;
import eu.gaiax.wizard.api.utils.CommonUtils;
import eu.gaiax.wizard.api.utils.S3Utils;
import eu.gaiax.wizard.api.utils.Validate;
import eu.gaiax.wizard.core.service.credential.CredentialService;
import eu.gaiax.wizard.core.service.hashing.HashingService;
import eu.gaiax.wizard.core.service.signer.SignerService;
import eu.gaiax.wizard.dao.entity.Credential;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import eu.gaiax.wizard.dao.entity.service_offer.ServiceLabelLevel;
import eu.gaiax.wizard.dao.entity.service_offer.ServiceOffer;
import eu.gaiax.wizard.dao.repository.service_offer.ServiceLabelLevelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static eu.gaiax.wizard.api.utils.StringPool.TEMP_FOLDER;

@Service
@Slf4j
@RequiredArgsConstructor
public class ServiceLabelLevelService extends BaseService<ServiceLabelLevel, UUID> {

    private final S3Utils s3Utils;
    private final ContextConfig contextConfig;
    private final SignerService signerService;
    private final CredentialService credentialService;
    private final SpecificationUtil<ServiceLabelLevel> specificationUtil;
    private final ServiceLabelLevelRepository serviceLabelLevelRepository;
    private final ServiceEndpointConfig serviceEndpointConfig;
    @Value("${wizard.host.wizard}")
    private String wizardHost;

    public Map<String, String> createLabelLevelVc(LabelLevelRequest request, Participant participant, String serviceOfferId) {
        Validate.isNull(participant).launch(new BadDataException("participant.not.found"));
        String name = "labelLevel_" + UUID.randomUUID();
        String json = this.signLabelLevelVc(request, participant, name, serviceOfferId);
        Map<String, String> response = new HashMap<>();
        String labelLevelHostUrl = this.wizardHost + participant.getId() + "/" + name + ".json";

        response.put("labelLevelVc", json);
        response.put("vcUrl", labelLevelHostUrl);
        if (!participant.isOwnDidSolution()) {
            this.signerService.addServiceEndpoint(participant.getId(), labelLevelHostUrl, this.serviceEndpointConfig.linkDomainType(), labelLevelHostUrl);
        }
        return response;
    }

    public ServiceLabelLevel saveServiceLabelLevelLink(String json, String path, Participant participant, ServiceOffer serviceOffer) {
        Credential labelLevel = this.credentialService.createCredential(json, path, CredentialTypeEnum.LABEL_LEVEL.getCredentialType(), "", participant);
        return this.serviceLabelLevelRepository.save(
                ServiceLabelLevel.builder()
                        .credential(labelLevel)
                        .serviceOffer(serviceOffer)
                        .participant(participant).build()
        );
    }

    public String uploadLabelLevelFile(LabelLevelFileUpload labelLevelFileUpload) throws IOException {
        File file = new File(TEMP_FOLDER + labelLevelFileUpload.file().getOriginalFilename());
        String fileName = "public/label-level/" + labelLevelFileUpload.fileType() + "/" + labelLevelFileUpload.file().getOriginalFilename().replace(" ", "_");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(labelLevelFileUpload.file().getBytes());

            this.s3Utils.uploadFile(fileName, file);
            return this.s3Utils.getObject(fileName);
        } catch (Exception e) {
            throw new RemoteException("File not Upload " + e.getMessage());
        } finally {
            CommonUtils.deleteFile(file);
        }

    }

    private String signLabelLevelVc(LabelLevelRequest request, Participant participant, String name, String assignerTo) {
        String id = this.wizardHost + participant.getId() + "/" + name + ".json";
        String issuanceDate = LocalDateTime.now().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        Map<String, Object> labelLevel = new HashMap<>();
        Map<String, Object> map = new HashMap<>();
        labelLevel.put("@context", this.contextConfig.labelLevel());
        labelLevel.put("type", Collections.singleton("VerifiableCredential"));
        labelLevel.put("id", id);
        labelLevel.put("issuer", participant.getDid());
        labelLevel.put("issuanceDate", issuanceDate);
        Map<String, Object> credentialSub = new HashMap<>();
        if (request.criteria() != null) {
            credentialSub.put("gx:criteria", request.criteria());
            credentialSub.put("@context", "https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#");
            credentialSub.put("id", id);
            credentialSub.put("type", "gx:ServiceOfferingLabel");
            credentialSub.put("gx:assignedTo", assignerTo);
        }

        labelLevel.put("credentialSubject", credentialSub);
        map.put("labelLevel", labelLevel);
        Map<String, Object> labelLevelMap = new HashMap<>();
        labelLevelMap.put("issuer", participant.getDid());
        labelLevelMap.put("verificationMethod", request.verificationMethod());
        labelLevelMap.put("vcs", map);
        labelLevelMap.put("isVault", participant.isKeyStored());
        if (!participant.isKeyStored()) {
            labelLevelMap.put("privateKey", HashingService.encodeToBase64(request.privateKey()));
        } else {
            labelLevelMap.put("privateKey", participant.getId().toString());
        }
        return this.signerService.signLabelLevel(labelLevelMap, participant.getId(), name);
    }

    @Override
    protected BaseRepository<ServiceLabelLevel, UUID> getRepository() {
        return this.serviceLabelLevelRepository;
    }

    @Override
    protected SpecificationUtil<ServiceLabelLevel> getSpecificationUtil() {
        return this.specificationUtil;
    }
}
