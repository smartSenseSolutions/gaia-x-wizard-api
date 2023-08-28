package eu.gaiax.wizard.controller;

import eu.gaiax.wizard.core.service.domain.DomainService;
import eu.gaiax.wizard.core.service.k8s.K8SService;
import eu.gaiax.wizard.core.service.signer.SignerService;
import eu.gaiax.wizard.core.service.ssl.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DIDCreationController {
    private final DomainService domainService;
    private final CertificateService certificateService;
    private final K8SService k8SService;
    private final SignerService signerService;

    @Deprecated
    @PostMapping(value = "/create/did", produces = MediaType.APPLICATION_JSON_VALUE)
    public void createDid(@RequestParam("shortName") String shortName) {
        String domainName = shortName + ".smart-x.smartsenselabs.com";
//        this.domainService.createSubDomain(domainName);
//        this.certificateService.createSSLCertificate(domainName);
        this.k8SService.createIngress(domainName);
        this.signerService.createDid(domainName);
    }
}
