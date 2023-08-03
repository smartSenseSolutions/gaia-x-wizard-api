/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.core.service.k8s;

import eu.gaiax.wizard.api.exception.ParticipantNotFoundException;
import eu.gaiax.wizard.api.model.RegistrationStatus;
import eu.gaiax.wizard.api.model.StringPool;
import eu.gaiax.wizard.api.model.setting.K8SSettings;
import eu.gaiax.wizard.api.utils.S3Utils;
import eu.gaiax.wizard.api.utils.Validate;
import eu.gaiax.wizard.core.service.job.ScheduleService;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import eu.gaiax.wizard.dao.repository.participant.ParticipantRepository;
import eu.gaiax.wizard.vault.Vault;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.NetworkingV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class K8SService {

    public static final String DEFAULT = "default";

    private final ParticipantRepository participantRepository;
    private final Vault vault;
    private final S3Utils s3Util;

    private final K8SSettings k8SSettings;

    private final ScheduleService scheduleService;


    public void createIngress(UUID participantId) {
        Participant participant = this.participantRepository.findById(participantId).orElse(null);
        Validate.isNull(participant).launch(new ParticipantNotFoundException("Participant not found"));
        try {
            Map<String, Object> certificates = this.vault.get(participant.getId().toString());
            //Step 1: create secret using SSL certificate
            ApiClient client = Config.fromToken(this.k8SSettings.basePath(), this.k8SSettings.token(), false);
            Configuration.setDefaultApiClient(client);


            CoreV1Api api = new CoreV1Api();

            V1Secret secret = new V1Secret();
            secret.setMetadata(new V1ObjectMeta().name(participant.getDomain()));
            secret.setType("kubernetes.io/tls");


            String certString = (String) certificates.get(participant.getDomain() + ".csr");
            String keyString = (String) certificates.get(participant.getDomain() + ".key");
            log.debug("certString  -> {}", certString);
            log.debug("keyString  -> {}", keyString);

            secret.putDataItem("tls.crt", certString.getBytes());
            secret.putDataItem("tls.key", keyString.getBytes());


            api.createNamespacedSecret(DEFAULT, secret, null, null, null, null);
            log.debug("tls secret created for participant -{} domain ->{}", participant.getId(), participant.getDomain());

            ///annotations
            Map<String, String> annotations = new HashMap<>();
            annotations.put("nginx.ingress.kubernetes.io/proxy-body-size", "35m");
            annotations.put("nginx.ingress.kubernetes.io/client-body-buffer-size", "35m");
            annotations.put("nginx.ingress.kubernetes.io/proxy-connect-timeout", "600");
            annotations.put("nginx.ingress.kubernetes.io/proxy-send-timeout", "600");
            annotations.put("nginx.ingress.kubernetes.io/proxy-read-timeout", "600");

            //TODO need to add letsencrypt-prod  as configuration
            annotations.put("cert-manager.io/cluster-issuer", "letsencrypt-prod");


            //Step 2: Create ingress
            NetworkingV1Api networkingV1Api = new NetworkingV1Api();
            V1ObjectMeta metadata = new V1ObjectMeta();
            metadata.setName(participant.getDomain());
            metadata.setNamespace(DEFAULT);
            metadata.setAnnotations(annotations);

            //tls item
            V1IngressTLS ingressTLS = new V1IngressTLS();
            ingressTLS.setSecretName(participant.getDomain());
            ingressTLS.setHosts(List.of(participant.getDomain()));

            //service backend
            V1IngressServiceBackend backend = new V1IngressServiceBackend();
            backend.setName(this.k8SSettings.serviceName());
            V1ServiceBackendPort port = new V1ServiceBackendPort();
            port.setNumber(8080);
            backend.setPort(port);

            V1IngressBackend v1IngressBackend = new V1IngressBackend();
            v1IngressBackend.setService(backend);

            //path
            V1HTTPIngressPath path = new V1HTTPIngressPath();
            path.backend(v1IngressBackend);
            path.pathType("Prefix");
            path.path("/");

            //http rule
            V1HTTPIngressRuleValue httpIngressRuleValue = new V1HTTPIngressRuleValue();
            httpIngressRuleValue.addPathsItem(path);

            //v1 rule
            V1IngressRule rule = new V1IngressRule();
            rule.host(participant.getDomain());
            rule.http(httpIngressRuleValue);

            V1IngressSpec spec = new V1IngressSpec();
            spec.addTlsItem(ingressTLS);
            spec.addRulesItem(rule);

            //main ingress object
            V1Ingress v1Ingress = new V1Ingress();
            v1Ingress.metadata(metadata);
            v1Ingress.setSpec(spec);

            networkingV1Api.createNamespacedIngress(DEFAULT, v1Ingress, null, null, null, null);

            participant.setStatus(RegistrationStatus.INGRESS_CREATED.getStatus());

            log.debug("Ingress created for participant -> {} and domain ->{}", participant.getId(), participant.getDomain());
            this.createDidCreationJob(participant);
        } catch (Exception e) {
            log.error("Can not create ingress for participant -> {}", participant.getId(), e);
            participant.setStatus(RegistrationStatus.INGRESS_CREATION_FAILED.getStatus());
        } finally {
            this.participantRepository.save(participant);
        }
    }

    private void createDidCreationJob(Participant participant) {
        try {
            this.scheduleService.createJob(participant.getId().toString(), StringPool.JOB_TYPE_CREATE_DID, 0);
        } catch (SchedulerException e) {
            log.error("Can not create did creation job for enterprise->{}", participant.getDid(), e);
            participant.setStatus(RegistrationStatus.DID_JSON_CREATION_FAILED.getStatus());
        }
    }

}
