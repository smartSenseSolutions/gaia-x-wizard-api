package eu.gaiax.wizard.controller;

import com.smartsensesolutions.java.commons.FilterRequest;
import eu.gaiax.wizard.api.model.CommonResponse;
import eu.gaiax.wizard.api.model.PageResponse;
import eu.gaiax.wizard.api.model.ServiceFilterResponse;
import eu.gaiax.wizard.api.model.service_offer.*;
import eu.gaiax.wizard.api.utils.StringPool;
import eu.gaiax.wizard.core.service.service_offer.ServiceOfferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.UUID;

import static eu.gaiax.wizard.utils.WizardRestConstant.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
@Tag(name = "Service-Offering")
public class ServiceOfferController extends BaseController {

    private final ServiceOfferService serviceOfferService;
    private final MessageSource messageSource;

    @Operation(summary = "Create Service offering for enterprise, role = enterprise")
    @PostMapping(path = SERVICE_OFFER, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(examples = {
                    @ExampleObject(name = "Create Service offer", value = """
                            {
                              "name": "name of service",
                              "description": "test service data",
                              "credentialSubject": {
                                "gx:termsAndConditions": {
                                  "gx:URL": "https://aws.amazon.com/service-terms/"
                                },
                                "gx:policy": {
                                  "gx:location": [
                                    "BE-BRU"
                                  ],
                                  "gx:customAttribute": [
                                    "BE-BRU",
                                    "IN-GJ"
                                  ]
                                },
                                "gx:dataAccountExport": {
                                  "gx:requestType": "API",
                                  "gx:accessType": "physical",
                                  "gx:formatType": "pdf"
                                },
                                "gx:aggregationOf": [
                                  {
                                    "id": "https://exmaple.com/12081064-8878-477e-8092-564a240c69e2/resource_9a40cafd-43ed-41b0-a53e-4e2af164fde5.json"
                                  }
                                ],
                                "gx:dependsOn": [
                                  {
                                    "id": "https://exmaple.com/12081064-8878-477e-8092-564a240c69e2/service_S7GZ.json"
                                  }
                                ],
                                "gx:dataProtectionRegime": "GDPR2016",
                                "type": "gx:ServiceOffering",
                                "gx:criteria": {
                                  "P1.1.1": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P1.1.2": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P1.1.3": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P1.1.4": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P1.2.1": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P1.2.2": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P1.2.3": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P1.2.4": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P1.2.5": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P1.2.6": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P1.2.7": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P1.2.8": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P1.2.9": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P1.2.10": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P1.3.1": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P1.3.2": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P1.3.3": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P1.3.4": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P1.3.5": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P2.1.1": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P2.1.2": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P2.1.3": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P2.2.1": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P2.2.2": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P2.2.3": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P2.2.4": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P2.2.5": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P2.2.6": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P2.2.7": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P2.3.1": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P2.3.2": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P2.3.3": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P3.1.1": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P3.1.2": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P3.1.3": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P3.1.4": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P3.1.5": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P3.1.6": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P3.1.7": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P3.1.8": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P3.1.9": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P3.1.10": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P3.1.11": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P3.1.12": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P3.1.13": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P3.1.14": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P3.1.15": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P3.1.16": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P3.1.17": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P3.1.18": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P3.1.19": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P3.1.20": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P4.1.1": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P4.1.2": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P5.1.1": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P5.1.2": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P5.1.3": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P5.1.4": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P5.1.5": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P5.1.6": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P5.1.7": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  },
                                  "P5.2.1": {
                                    "evidence": {
                                      "website": "",
                                      "pdf": {},
                                      "vc": {}
                                    },
                                    "response": "Confirm",
                                    "reason": ""
                                  }
                                }
                              }
                            }""")
            })
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service offering successfully.",
                    content = {
                            @Content(
                                    examples = {
                                            @ExampleObject(name = "Success Response", value = """
                                                    {
                                                      "status": 200,
                                                      "payload": {
                                                        "name": "name of service",
                                                        "description": "test service data",
                                                        "vcUrl": "https://exmaple.json",
                                                        "vcJson": [
                                                          {
                                                            "selfDescriptionCredential": {
                                                              "@context": "https://www.w3.org/2018/credentials/v1",
                                                              "type": [
                                                                "VerifiablePresentation"
                                                              ],
                                                              "verifiableCredential": [
                                                                {
                                                                  "credentialSubject": {
                                                                    "gx:legalName": "Green World",
                                                                    "gx:headquarterAddress": {
                                                                      "gx:countrySubdivisionCode": "BE-BRU"
                                                                    },
                                                                    "gx:legalAddress": {
                                                                      "gx:countrySubdivisionCode": "BE-BRU"
                                                                    },
                                                                    "id": "https://exmaple/participant.json#0",
                                                                    "type": "gx:LegalParticipant",
                                                                    "gx:legalRegistrationNumber": {
                                                                      "id": "https://example.com/125/participant.json#1"
                                                                    }
                                                                  },
                                                                  "@context": [
                                                                    "https://www.w3.org/2018/credentials/v1",
                                                                    "https://w3id.org/security/suites/jws-2020/v1",
                                                                    "https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#"
                                                                  ],
                                                                  "type": [
                                                                    "VerifiableCredential"
                                                                  ],
                                                                  "id": "did:web:casio50.smart-x.smartsenselabs.com",
                                                                  "issuer": "did:web:casio50.smart-x.smartsenselabs.com",
                                                                  "issuanceDate": "2023-08-07T16:04:30.307219451Z",
                                                                  "proof": {
                                                                    "type": "JsonWebSignature2020",
                                                                    "created": "2023-08-07T10:34:33.838Z",
                                                                    "proofPurpose": "assertionMethod",
                                                                    "verificationMethod": "did:web:casio50.smart-x.smartsenselabs.com",
                                                                    "jws": ""
                                                                  }
                                                                },
                                                                {
                                                                  "@context": [
                                                                    "https://www.w3.org/2018/credentials/v1",
                                                                    "https://w3id.org/security/suites/jws-2020/v1"
                                                                  ],
                                                                  "type": "VerifiableCredential",
                                                                  "id": "https://example.com/125/participant.json#1",
                                                                  "issuer": "did:web:registration.lab.gaia-x.eu:development",
                                                                  "issuanceDate": "2023-08-07T10:34:32.101Z",
                                                                  "credentialSubject": {
                                                                    "@context": "https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#",
                                                                    "type": "gx:legalRegistrationNumber",
                                                                    "id": "https://example.com/125/participant.json#1",
                                                                    "gx:leiCode": "9695007586GCAKPYJ703",
                                                                    "gx:leiCode-countryCode": "FR"
                                                                  },
                                                                  "evidence": [
                                                                    {
                                                                      "gx:evidenceURL": "https://api.gleif.org/api/v1/lei-records/",
                                                                      "gx:executionDate": "2023-08-07T10:34:32.101Z",
                                                                      "gx:evidenceOf": "gx:leiCode"
                                                                    }
                                                                  ],
                                                                  "proof": {
                                                                    "type": "JsonWebSignature2020",
                                                                    "created": "2023-08-07T10:34:33.102Z",
                                                                    "proofPurpose": "assertionMethod",
                                                                    "verificationMethod": "did:web:registration.lab.gaia-x.eu:development#X509-JWK2020",
                                                                    "jws": ""
                                                                  }
                                                                },
                                                                {
                                                                  "@context": [
                                                                    "https://www.w3.org/2018/credentials/v1",
                                                                    "https://w3id.org/security/suites/jws-2020/v1",
                                                                    "https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#"
                                                                  ],
                                                                  "credentialSubject": {
                                                                    "@Context": [
                                                                      "https://www.w3.org/2018/credentials/v1",
                                                                      "https://w3id.org/security/suites/jws-2020/v1",
                                                                      "https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#"
                                                                    ],
                                                                    "gx:termsAndConditions": "The PARTICIPANT signing the Self-Description agrees as follows:\\n- to update its descriptions about any changes, be it technical, organizational, or legal - especially but not limited to contractual in regards to the indicated attributes present in the descriptions.\\n\\nThe keypair used to sign Verifiable Credentials will be revoked where Gaia-X Association becomes aware of any inaccurate statements in regards to the claims which result in a non-compliance with the Trust Framework and policy rules defined in the Policy Rules and Labelling Document (PRLD).",
                                                                    "id": "https://example.com/125/participant.json#2",
                                                                    "type": "gx:GaiaXTermsAndConditions"
                                                                  },
                                                                  "id": "did:web:casio50.smart-x.smartsenselabs.com",
                                                                  "issuanceDate": "2023-08-07T16:04:30.307219451Z",
                                                                  "issuer": "did:web:casio50.smart-x.smartsenselabs.com",
                                                                  "type": [
                                                                    "VerifiableCredential"
                                                                  ],
                                                                  "proof": {
                                                                    "type": "JsonWebSignature2020",
                                                                    "created": "2023-08-07T10:34:34.513Z",
                                                                    "proofPurpose": "assertionMethod",
                                                                    "verificationMethod": "did:web:casio50.smart-x.smartsenselabs.com",
                                                                    "jws": ""
                                                                  }
                                                                },
                                                                {
                                                                  "type": "VerifiableCredential",
                                                                  "id": "did:web:casio50.smart-x.smartsenselabs.com",
                                                                  "issuer": "did:web:casio50.smart-x.smartsenselabs.com",
                                                                  "issuanceDate": "2023-08-21T14:09:58.805414431Z",
                                                                  "credentialSubject": {
                                                                    "gx:termsAndConditions": {
                                                                      "gx:URL": "https://aws.amazon.com/service-terms/",
                                                                      "gx:hash": "bc9f9f66fa34a52a8da4d0d14f4594cbb7cc6d929c5de1e161dbbd6f0b1985b9"
                                                                    },
                                                                    "gx:policy": [
                                                                      "https://example.com/125/service_Fmai_policy.json"
                                                                    ],
                                                                    "gx:dataAccountExport": {
                                                                      "gx:requestType": "API",
                                                                      "gx:accessType": "physical",
                                                                      "gx:formatType": "pdf"
                                                                    },
                                                                    "gx:aggregationOf": [
                                                                      {
                                                                        "id": "https://example.com/125/resource_9a40cafd-43ed-41b0-a53e-4e2af164fde5.json"
                                                                      }
                                                                    ],
                                                                    "gx:dependsOn": [
                                                                      {
                                                                        "id": "https://example.com/125/service_S7GZ.json"
                                                                      }
                                                                    ],
                                                                    "gx:dataProtectionRegime": "GDPR2016",
                                                                    "type": "gx:ServiceOffering",
                                                                    "gx:providedBy": {
                                                                      "id": "https://example.com/125/participant.json#0"
                                                                    },
                                                                    "id": "https://example.com/125/service_Fmai.json",
                                                                    "gx:name": "Service_soft_testttt2",
                                                                    "gx:description": "test service data"
                                                                  },
                                                                  "@context": [
                                                                    "https://www.w3.org/2018/credentials/v1",
                                                                    "https://w3id.org/security/suites/jws-2020/v1"
                                                                  ],
                                                                  "proof": {
                                                                    "type": "JsonWebSignature2020",
                                                                    "created": "2023-08-21T08:39:59.839Z",
                                                                    "proofPurpose": "assertionMethod",
                                                                    "verificationMethod": "did:web:casio50.smart-x.smartsenselabs.com",
                                                                    "jws": ""
                                                                  }
                                                                },
                                                                {
                                                                  "type": "VerifiableCredential",
                                                                  "id": "did:web:casio50.smart-x.smartsenselabs.com",
                                                                  "issuer": "did:web:casio50.smart-x.smartsenselabs.com",
                                                                  "issuanceDate": "2023-08-19T18:14:15.643722304Z",
                                                                  "credentialSubject": {
                                                                    "gx:termsAndConditions": {
                                                                      "gx:URL": "https://aws.amazon.com/service-terms/",
                                                                      "gx:hash": "bc9f9f66fa34a52a8da4d0d14f4594cbb7cc6d929c5de1e161dbbd6f0b1985b9"
                                                                    },
                                                                    "gx:policy": [
                                                                      "https://example.com/125/service_S7GZ_policy.json"
                                                                    ],
                                                                    "gx:location": [
                                                                      "BE-BRU"
                                                                    ],
                                                                    "gx:customAttribute": "https://www.smartsensesolutions.com/",
                                                                    "gx:dataAccountExport": {
                                                                      "gx:requestType": "API",
                                                                      "gx:accessType": "physical",
                                                                      "gx:formatType": "pdf"
                                                                    },
                                                                    "gx:aggregationOf": [
                                                                      {
                                                                        "id": "https://example.com/125/resource_86e8a7a9-c341-4049-abc7-dcf20e1736fe.json"
                                                                      }
                                                                    ],
                                                                    "gx:dataProtectionRegime": "GDPR2016",
                                                                    "type": "gx:ServiceOffering",
                                                                    "gx:providedBy": {
                                                                      "id": "https://example.com/125/participant.json#0"
                                                                    },
                                                                    "id": "https://example.com/125/service_S7GZ.json",
                                                                    "gx:name": "Service_soft_res_1",
                                                                    "gx:description": "test service data"
                                                                  },
                                                                  "@context": [
                                                                    "https://www.w3.org/2018/credentials/v1",
                                                                    "https://w3id.org/security/suites/jws-2020/v1"
                                                                  ],
                                                                  "proof": {
                                                                    "type": "JsonWebSignature2020",
                                                                    "created": "2023-08-19T12:44:16.295Z",
                                                                    "proofPurpose": "assertionMethod",
                                                                    "verificationMethod": "did:web:casio50.smart-x.smartsenselabs.com",
                                                                    "jws": ""
                                                                  }
                                                                }
                                                              ]
                                                            },
                                                            "complianceCredential": {
                                                              "@context": [
                                                                "https://www.w3.org/2018/credentials/v1",
                                                                "https://w3id.org/security/suites/jws-2020/v1",
                                                                "https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#"
                                                              ],
                                                              "type": [
                                                                "VerifiableCredential"
                                                              ],
                                                              "id": "https://compliance.lab.gaia-x.eu/development/credential-offers/55427329-5d44-43cb-a425-308f874a4dc9",
                                                              "issuer": "did:web:compliance.lab.gaia-x.eu:development",
                                                              "issuanceDate": "2023-08-21T08:40:14.393Z",
                                                              "expirationDate": "2023-11-19T08:40:14.393Z",
                                                              "credentialSubject": [
                                                                {
                                                                  "type": "gx:compliance",
                                                                  "id": "https://example.com/125/participant.json#0",
                                                                  "integrity": "sha256-372994ed4b18b7f4252626a48c510aa170cb3a04717d2501ccf1982ed85a9b12",
                                                                  "version": "22.10"
                                                                },
                                                                {
                                                                  "type": "gx:compliance",
                                                                  "id": "https://example.com/125/participant.json#1",
                                                                  "integrity": "sha256-20059a7a182d8a840ee25f8773446aa2f0564c0ff82d359b8a1b194bf1f98045",
                                                                  "version": "22.10"
                                                                },
                                                                {
                                                                  "type": "gx:compliance",
                                                                  "id": "https://example.com/125/participant.json#2",
                                                                  "integrity": "sha256-f897639cb8a0236874ec395a3b0443986fcf5e3b1ac4ac1f3978e72e685dea2f",
                                                                  "version": "22.10"
                                                                },
                                                                {
                                                                  "type": "gx:compliance",
                                                                  "id": "https://example.com/125/service_Fmai.json",
                                                                  "integrity": "sha256-7fd1168cf7fd7c3e36c8293db3e04938c750b06992b779cbc7b828fc7eb060c3",
                                                                  "version": "22.10"
                                                                },
                                                                {
                                                                  "type": "gx:compliance",
                                                                  "id": "https://example.com/125/service_S7GZ.json",
                                                                  "integrity": "sha256-70515740d132923073c25fba85b1af920164ac7ec94d9cd08cb99b5374d49edf",
                                                                  "version": "22.10"
                                                                }
                                                              ],
                                                              "proof": {
                                                                "type": "JsonWebSignature2020",
                                                                "created": "2023-08-21T08:40:15.041Z",
                                                                "proofPurpose": "assertionMethod",
                                                                "jws": "",
                                                                "verificationMethod": "did:web:compliance.lab.gaia-x.eu:development#X509-JWK2020"
                                                              }
                                                            }
                                                          }
                                                        ]
                                                      }
                                                    }
                                                    """)
                                    }
                            )
                    }),
            @ApiResponse(responseCode = "400", description = "Invalid (Participant,Dependence On,Aggregation of )",
                    content = {
                            @Content(examples = {
                                    @ExampleObject(name = "Validation failed", value = """
                                            {
                                              "message": "Validation has been failed.",
                                              "status": 400,
                                              "payload": {
                                                "error": {
                                                  "message": "Validation has been failed.",
                                                  "status": 400,
                                                  "timeStamp": 1692342059774
                                                }
                                              }
                                            }
                                            """)
                            })
                    }),
    })
    public CommonResponse<ServiceOfferResponse> createServiceOffering(@Valid @RequestBody CreateServiceOfferingRequest request, Principal principal) throws IOException {
        return CommonResponse.of(this.serviceOfferService.createServiceOffering(request, this.requestForClaim(StringPool.ID, principal).toString(), false), this.messageSource.getMessage("entity.creation.successful", new String[]{"Service offer"}, LocaleContextHolder.getLocale()));
    }

    @Operation(summary = "Validate Service offering for enterprise, role = enterprise")
    @PostMapping(path = VALIDATE_SERVICE_OFFER, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(examples = {
                    @ExampleObject(name = "Create Service offer", value = """
                            {
                              "name": "name of service",
                              "description": "test service data",
                              "credentialSubject": {
                                "gx:termsAndConditions": {
                                  "gx:URL": "https://aws.amazon.com/service-terms/"
                                },
                                "gx:policy": {
                                  "gx:location": [
                                    "BE-BRU"
                                  ],
                                  "gx:customAttribute": "https://www.smartsensesolutions.com/"
                                },
                                "gx:dataAccountExport": {
                                  "gx:requestType": "API",
                                  "gx:accessType": "physical",
                                  "gx:formatType": "pdf"
                                },
                                "gx:aggregationOf": [
                                  {
                                    "id": "https://exmaple.com/12081064-8878-477e-8092-564a240c69e2/resource_9a40cafd-43ed-41b0-a53e-4e2af164fde5.json"
                                  }
                                ],
                                "gx:dependsOn": [
                                  {
                                    "id": "https://exmaple.com/12081064-8878-477e-8092-564a240c69e2/service_S7GZ.json"
                                  }
                                ],
                                "gx:dataProtectionRegime": "GDPR2016",
                                "type": "gx:ServiceOffering"
                              }
                            }
                            """)
            })
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service offering successfully.",
                    content = {
                            @Content(
                                    examples = {
                                            @ExampleObject(name = "Success Response", value = """
                                                    {"message":"Validate Successfully."
                                                      "status": 200,
                                                      "payload": {
                                                    }
                                                    """)
                                    }
                            )
                    }),
            @ApiResponse(responseCode = "400", description = "Invalid (Participant,Dependence On,Aggregation of )",
                    content = {
                            @Content(examples = {
                                    @ExampleObject(name = "Validation failed", value = """
                                            {
                                              "message": "Validation has been failed.",
                                              "status": 400,
                                              "payload": {
                                                "error": {
                                                  "message": "Validation has been failed.",
                                                  "status": 400,
                                                  "timeStamp": 1692342059774
                                                }
                                              }
                                            }
                                            """)
                            })
                    }),
    })
    public void validateServiceOfferRequest(@Valid @RequestBody CreateServiceOfferingRequest request) throws IOException {
        this.serviceOfferService.validateServiceOfferMainRequest(request);
    }

    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(examples = {
                    @ExampleObject(name = "Create Service offer", value = """
                            {
                              "name": "name of service",
                              "verificationMethod": "did:web:exmaple.com",
                              "participantJsonUrl": "https://example.com/12081064-8878-477e-8092-564a240c69e2/participant.json",
                              "description": "test service data",
                              "privateKey": "-----BEGIN PRIVATE KEY---  ----END PRIVATE KEY-----",
                              "credentialSubject": {
                                   "gx:termsAndConditions": {
                                     "gx:URL": "https://aws.amazon.com/service-terms/"
                                   },
                                   "gx:policy": {
                                     "gx:location": [
                                       "BE-BRU"
                                     ],
                                     "gx:customAttribute": [
                                       "BE-BRU",
                                       "IN-GJ"
                                     ]
                                   },
                                   "gx:dataAccountExport": {
                                     "gx:requestType": "API",
                                     "gx:accessType": "physical",
                                     "gx:formatType": "pdf"
                                   },
                                   "gx:aggregationOf": [
                                     {
                                       "id": "https://exmaple.com/12081064-8878-477e-8092-564a240c69e2/resource_9a40cafd-43ed-41b0-a53e-4e2af164fde5.json"
                                     }
                                   ],
                                   "gx:dependsOn": [
                                     {
                                       "id": "https://exmaple.com/12081064-8878-477e-8092-564a240c69e2/service_S7GZ.json"
                                     }
                                   ],
                                   "gx:dataProtectionRegime": "GDPR2016",
                                   "type": "gx:ServiceOffering",
                                   "gx:criteria": {
                                     "P1.1.1": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P1.1.2": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P1.1.3": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P1.1.4": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P1.2.1": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P1.2.2": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P1.2.3": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P1.2.4": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P1.2.5": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P1.2.6": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P1.2.7": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P1.2.8": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P1.2.9": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P1.2.10": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P1.3.1": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P1.3.2": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P1.3.3": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P1.3.4": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P1.3.5": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P2.1.1": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P2.1.2": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P2.1.3": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P2.2.1": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P2.2.2": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P2.2.3": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P2.2.4": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P2.2.5": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P2.2.6": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P2.2.7": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P2.3.1": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P2.3.2": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P2.3.3": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P3.1.1": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P3.1.2": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P3.1.3": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P3.1.4": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P3.1.5": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P3.1.6": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P3.1.7": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P3.1.8": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P3.1.9": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P3.1.10": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P3.1.11": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P3.1.12": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P3.1.13": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P3.1.14": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P3.1.15": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P3.1.16": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P3.1.17": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P3.1.18": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P3.1.19": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P3.1.20": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P4.1.1": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P4.1.2": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P5.1.1": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P5.1.2": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P5.1.3": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P5.1.4": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P5.1.5": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P5.1.6": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P5.1.7": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     },
                                     "P5.2.1": {
                                       "evidence": {
                                         "website": "",
                                         "pdf": {},
                                         "vc": {}
                                       },
                                       "response": "Confirm",
                                       "reason": ""
                                     }
                                   }
                                 }
                            }
                            """)
            })
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service offering successfully.",
                    content = {
                            @Content(
                                    examples = {
                                            @ExampleObject(name = "Success Response", value = """
                                                    {
                                                      "status": 200,
                                                      "payload": {
                                                        "name": "Service_soft_testttt2",
                                                        "description": "test service data",
                                                        "vcUrl": "https://exmaple.json",
                                                        "vcJson": [
                                                          {
                                                            "selfDescriptionCredential": {
                                                              "@context": "https://www.w3.org/2018/credentials/v1",
                                                              "type": [
                                                                "VerifiablePresentation"
                                                              ],
                                                              "verifiableCredential": [
                                                                {
                                                                  "credentialSubject": {
                                                                    "gx:legalName": "Green World",
                                                                    "gx:headquarterAddress": {
                                                                      "gx:countrySubdivisionCode": "BE-BRU"
                                                                    },
                                                                    "gx:legalAddress": {
                                                                      "gx:countrySubdivisionCode": "BE-BRU"
                                                                    },
                                                                    "id": "https://exmaple/participant.json#0",
                                                                    "type": "gx:LegalParticipant",
                                                                    "gx:legalRegistrationNumber": {
                                                                      "id": "https://example.com/125/participant.json#1"
                                                                    }
                                                                  },
                                                                  "@context": [
                                                                    "https://www.w3.org/2018/credentials/v1",
                                                                    "https://w3id.org/security/suites/jws-2020/v1",
                                                                    "https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#"
                                                                  ],
                                                                  "type": [
                                                                    "VerifiableCredential"
                                                                  ],
                                                                  "id": "did:web:casio50.smart-x.smartsenselabs.com",
                                                                  "issuer": "did:web:casio50.smart-x.smartsenselabs.com",
                                                                  "issuanceDate": "2023-08-07T16:04:30.307219451Z",
                                                                  "proof": {
                                                                    "type": "JsonWebSignature2020",
                                                                    "created": "2023-08-07T10:34:33.838Z",
                                                                    "proofPurpose": "assertionMethod",
                                                                    "verificationMethod": "did:web:casio50.smart-x.smartsenselabs.com",
                                                                    "jws": ""
                                                                  }
                                                                },
                                                                {
                                                                  "@context": [
                                                                    "https://www.w3.org/2018/credentials/v1",
                                                                    "https://w3id.org/security/suites/jws-2020/v1"
                                                                  ],
                                                                  "type": "VerifiableCredential",
                                                                  "id": "https://example.com/125/participant.json#1",
                                                                  "issuer": "did:web:registration.lab.gaia-x.eu:development",
                                                                  "issuanceDate": "2023-08-07T10:34:32.101Z",
                                                                  "credentialSubject": {
                                                                    "@context": "https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#",
                                                                    "type": "gx:legalRegistrationNumber",
                                                                    "id": "https://example.com/125/participant.json#1",
                                                                    "gx:leiCode": "9695007586GCAKPYJ703",
                                                                    "gx:leiCode-countryCode": "FR"
                                                                  },
                                                                  "evidence": [
                                                                    {
                                                                      "gx:evidenceURL": "https://api.gleif.org/api/v1/lei-records/",
                                                                      "gx:executionDate": "2023-08-07T10:34:32.101Z",
                                                                      "gx:evidenceOf": "gx:leiCode"
                                                                    }
                                                                  ],
                                                                  "proof": {
                                                                    "type": "JsonWebSignature2020",
                                                                    "created": "2023-08-07T10:34:33.102Z",
                                                                    "proofPurpose": "assertionMethod",
                                                                    "verificationMethod": "did:web:registration.lab.gaia-x.eu:development#X509-JWK2020",
                                                                    "jws": ""
                                                                  }
                                                                },
                                                                {
                                                                  "@context": [
                                                                    "https://www.w3.org/2018/credentials/v1",
                                                                    "https://w3id.org/security/suites/jws-2020/v1",
                                                                    "https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#"
                                                                  ],
                                                                  "credentialSubject": {
                                                                    "@Context": [
                                                                      "https://www.w3.org/2018/credentials/v1",
                                                                      "https://w3id.org/security/suites/jws-2020/v1",
                                                                      "https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#"
                                                                    ],
                                                                    "gx:termsAndConditions": "The PARTICIPANT signing the Self-Description agrees as follows:\\n- to update its descriptions about any changes, be it technical, organizational, or legal - especially but not limited to contractual in regards to the indicated attributes present in the descriptions.\\n\\nThe keypair used to sign Verifiable Credentials will be revoked where Gaia-X Association becomes aware of any inaccurate statements in regards to the claims which result in a non-compliance with the Trust Framework and policy rules defined in the Policy Rules and Labelling Document (PRLD).",
                                                                    "id": "https://example.com/125/participant.json#2",
                                                                    "type": "gx:GaiaXTermsAndConditions"
                                                                  },
                                                                  "id": "did:web:casio50.smart-x.smartsenselabs.com",
                                                                  "issuanceDate": "2023-08-07T16:04:30.307219451Z",
                                                                  "issuer": "did:web:casio50.smart-x.smartsenselabs.com",
                                                                  "type": [
                                                                    "VerifiableCredential"
                                                                  ],
                                                                  "proof": {
                                                                    "type": "JsonWebSignature2020",
                                                                    "created": "2023-08-07T10:34:34.513Z",
                                                                    "proofPurpose": "assertionMethod",
                                                                    "verificationMethod": "did:web:casio50.smart-x.smartsenselabs.com",
                                                                    "jws": ""
                                                                  }
                                                                },
                                                                {
                                                                  "type": "VerifiableCredential",
                                                                  "id": "did:web:casio50.smart-x.smartsenselabs.com",
                                                                  "issuer": "did:web:casio50.smart-x.smartsenselabs.com",
                                                                  "issuanceDate": "2023-08-21T14:09:58.805414431Z",
                                                                  "credentialSubject": {
                                                                    "gx:termsAndConditions": {
                                                                      "gx:URL": "https://aws.amazon.com/service-terms/",
                                                                      "gx:hash": "bc9f9f66fa34a52a8da4d0d14f4594cbb7cc6d929c5de1e161dbbd6f0b1985b9"
                                                                    },
                                                                    "gx:policy": [
                                                                      "https://example.com/125/service_Fmai_policy.json"
                                                                    ],
                                                                    "gx:dataAccountExport": {
                                                                      "gx:requestType": "API",
                                                                      "gx:accessType": "physical",
                                                                      "gx:formatType": "pdf"
                                                                    },
                                                                    "gx:aggregationOf": [
                                                                      {
                                                                        "id": "https://example.com/125/resource_9a40cafd-43ed-41b0-a53e-4e2af164fde5.json"
                                                                      }
                                                                    ],
                                                                    "gx:dependsOn": [
                                                                      {
                                                                        "id": "https://example.com/125/service_S7GZ.json"
                                                                      }
                                                                    ],
                                                                    "gx:dataProtectionRegime": "GDPR2016",
                                                                    "type": "gx:ServiceOffering",
                                                                    "gx:providedBy": {
                                                                      "id": "https://example.com/125/participant.json#0"
                                                                    },
                                                                    "id": "https://example.com/125/service_Fmai.json",
                                                                    "gx:name": "Service_soft_testttt2",
                                                                    "gx:description": "test service data"
                                                                  },
                                                                  "@context": [
                                                                    "https://www.w3.org/2018/credentials/v1",
                                                                    "https://w3id.org/security/suites/jws-2020/v1"
                                                                  ],
                                                                  "proof": {
                                                                    "type": "JsonWebSignature2020",
                                                                    "created": "2023-08-21T08:39:59.839Z",
                                                                    "proofPurpose": "assertionMethod",
                                                                    "verificationMethod": "did:web:casio50.smart-x.smartsenselabs.com",
                                                                    "jws": ""
                                                                  }
                                                                },
                                                                {
                                                                  "type": "VerifiableCredential",
                                                                  "id": "did:web:casio50.smart-x.smartsenselabs.com",
                                                                  "issuer": "did:web:casio50.smart-x.smartsenselabs.com",
                                                                  "issuanceDate": "2023-08-19T18:14:15.643722304Z",
                                                                  "credentialSubject": {
                                                                    "gx:termsAndConditions": {
                                                                      "gx:URL": "https://aws.amazon.com/service-terms/",
                                                                      "gx:hash": "bc9f9f66fa34a52a8da4d0d14f4594cbb7cc6d929c5de1e161dbbd6f0b1985b9"
                                                                    },
                                                                    "gx:policy": [
                                                                      "https://example.com/125/service_S7GZ_policy.json"
                                                                    ],
                                                                    "gx:location": [
                                                                      "BE-BRU"
                                                                    ],
                                                                    "gx:customAttribute": "https://www.smartsensesolutions.com/",
                                                                    "gx:dataAccountExport": {
                                                                      "gx:requestType": "API",
                                                                      "gx:accessType": "physical",
                                                                      "gx:formatType": "pdf"
                                                                    },
                                                                    "gx:aggregationOf": [
                                                                      {
                                                                        "id": "https://example.com/125/resource_86e8a7a9-c341-4049-abc7-dcf20e1736fe.json"
                                                                      }
                                                                    ],
                                                                    "gx:dataProtectionRegime": "GDPR2016",
                                                                    "type": "gx:ServiceOffering",
                                                                    "gx:providedBy": {
                                                                      "id": "https://example.com/125/participant.json#0"
                                                                    },
                                                                    "id": "https://example.com/125/service_S7GZ.json",
                                                                    "gx:name": "Service_soft_res_1",
                                                                    "gx:description": "test service data"
                                                                  },
                                                                  "@context": [
                                                                    "https://www.w3.org/2018/credentials/v1",
                                                                    "https://w3id.org/security/suites/jws-2020/v1"
                                                                  ],
                                                                  "proof": {
                                                                    "type": "JsonWebSignature2020",
                                                                    "created": "2023-08-19T12:44:16.295Z",
                                                                    "proofPurpose": "assertionMethod",
                                                                    "verificationMethod": "did:web:casio50.smart-x.smartsenselabs.com",
                                                                    "jws": ""
                                                                  }
                                                                }
                                                              ]
                                                            },
                                                            "complianceCredential": {
                                                              "@context": [
                                                                "https://www.w3.org/2018/credentials/v1",
                                                                "https://w3id.org/security/suites/jws-2020/v1",
                                                                "https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#"
                                                              ],
                                                              "type": [
                                                                "VerifiableCredential"
                                                              ],
                                                              "id": "https://compliance.lab.gaia-x.eu/development/credential-offers/55427329-5d44-43cb-a425-308f874a4dc9",
                                                              "issuer": "did:web:compliance.lab.gaia-x.eu:development",
                                                              "issuanceDate": "2023-08-21T08:40:14.393Z",
                                                              "expirationDate": "2023-11-19T08:40:14.393Z",
                                                              "credentialSubject": [
                                                                {
                                                                  "type": "gx:compliance",
                                                                  "id": "https://example.com/125/participant.json#0",
                                                                  "integrity": "sha256-372994ed4b18b7f4252626a48c510aa170cb3a04717d2501ccf1982ed85a9b12",
                                                                  "version": "22.10"
                                                                },
                                                                {
                                                                  "type": "gx:compliance",
                                                                  "id": "https://example.com/125/participant.json#1",
                                                                  "integrity": "sha256-20059a7a182d8a840ee25f8773446aa2f0564c0ff82d359b8a1b194bf1f98045",
                                                                  "version": "22.10"
                                                                },
                                                                {
                                                                  "type": "gx:compliance",
                                                                  "id": "https://example.com/125/participant.json#2",
                                                                  "integrity": "sha256-f897639cb8a0236874ec395a3b0443986fcf5e3b1ac4ac1f3978e72e685dea2f",
                                                                  "version": "22.10"
                                                                },
                                                                {
                                                                  "type": "gx:compliance",
                                                                  "id": "https://example.com/125/service_Fmai.json",
                                                                  "integrity": "sha256-7fd1168cf7fd7c3e36c8293db3e04938c750b06992b779cbc7b828fc7eb060c3",
                                                                  "version": "22.10"
                                                                },
                                                                {
                                                                  "type": "gx:compliance",
                                                                  "id": "https://example.com/125/service_S7GZ.json",
                                                                  "integrity": "sha256-70515740d132923073c25fba85b1af920164ac7ec94d9cd08cb99b5374d49edf",
                                                                  "version": "22.10"
                                                                }
                                                              ],
                                                              "proof": {
                                                                "type": "JsonWebSignature2020",
                                                                "created": "2023-08-21T08:40:15.041Z",
                                                                "proofPurpose": "assertionMethod",
                                                                "jws": "",
                                                                "verificationMethod": "did:web:compliance.lab.gaia-x.eu:development#X509-JWK2020"
                                                              }
                                                            }
                                                          }
                                                        ]
                                                      }
                                                    }
                                                    """)
                                    }
                            )
                    }),
            @ApiResponse(responseCode = "400", description = "Invalid (Participant,Dependence On,Aggregation of )",
                    content = {
                            @Content(examples = {
                                    @ExampleObject(name = "Validation failed", value = """
                                            {
                                              "message": "Validation has been failed.",
                                              "status": 400,
                                              "payload": {
                                                "error": {
                                                  "message": "Validation has been failed.",
                                                  "status": 400,
                                                  "timeStamp": 1692342059774
                                                }
                                              }
                                            }
                                            """)
                            })
                    }),
    })
    @Operation(summary = "Create Service offering for enterprise, role = enterprise")
    @PostMapping(path = PUBLIC_SERVICE_OFFER, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<ServiceOfferResponse> createServiceOfferingPublic(@Valid @RequestBody CreateServiceOfferingRequest request) throws IOException {
        return CommonResponse.of(this.serviceOfferService.createServiceOffering(request, null, true), this.messageSource.getMessage("entity.creation.successful", new String[]{"Service offer"}, LocaleContextHolder.getLocale()));
    }

    @Operation(summary = "Get service locations from policy")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(examples = {
                    @ExampleObject(name = "Get location from service offering", value = """
                            {
                               "id": "https://wizard-api.smart-x.smartsenselabs.com/12081064-8878-477e-8092-564a240c69e2/service_EgOk.json"
                            }"""
                    )
            })
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service offering location displayed successfully.", content = {
                    @Content(examples = {
                            @ExampleObject(name = "Successful request", value = """
                                    {
                                       "status": 200,
                                       "payload": {
                                         "serviceAvailabilityLocation": [
                                           "BE-BRU"
                                         ]
                                       }
                                    }"""
                            )
                    })
            }),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = {
                    @Content(examples = {
                            @ExampleObject(name = "Invalid data type", value = """
                                    {
                                       "message": "Please enter service Id",
                                       "status": 400,
                                       "payload": {
                                         "error": {
                                           "message": "Validation failed",
                                           "status": 400,
                                           "timeStamp": 1692620670223,
                                           "fieldErrors": {
                                             "id": "Please enter service Id"
                                           }
                                         }
                                       }
                                    }"""
                            )
                    })
            }),
    })
    @PostMapping(path = SERVICE_OFFER_LOCATION, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<ServiceOfferingLocationResponse> getServiceOfferingLocation(@Valid @RequestBody ServiceIdRequest serviceIdRequest) {
        ServiceOfferingLocationResponse serviceOfferingLocationResponse = new ServiceOfferingLocationResponse(this.serviceOfferService.getLocationFromService(serviceIdRequest));
        return CommonResponse.of(serviceOfferingLocationResponse);
    }

    @Operation(summary = "Public service list")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(examples = {
                    @ExampleObject(name = "Filter request with sort",
                            value = """
                                    {
                                      "page": 0,
                                      "size": 5,
                                      "sort": {
                                        "column": "name",
                                        "sortType": "ASC"
                                      }
                                    }"""
                    ),
                    @ExampleObject(name = "Filter request with sort and search",
                            value = """
                                    {
                                      "page": 0,
                                      "size": 5,
                                      "sort": {
                                        "column": "name",
                                        "sortType": "ASC"
                                      },
                                      "criteriaOperator": "AND",
                                      "criteria": [
                                        {
                                          "column": "name",
                                          "operator": "CONTAIN",
                                          "values": [
                                            "xyz"
                                          ]
                                        }
                                      ]
                                    }"""
                    ),
            })
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Master data fetched successfully.", content = {
                    @Content(examples = {
                            @ExampleObject(name = "Successful request", value = """
                                    {
                                       "status": 200,
                                       "payload": {
                                         "content": [
                                           {
                                             "id": "0fa1180c-a8bf-4994-8798-66744886acea",
                                             "name": "Storage service",
                                             "vcUrl": "https://wizard-api.smart-x.smartsenselabs.com/12081064-8878-477e-8092-564a240c69e2/Storage service.json"
                                           },
                                           {
                                             "id": "ab06e01c-978a-459c-9104-1018cb5e1ec9",
                                             "name": "Clould service",
                                             "vcUrl": "https://wizard-api.smart-x.smartsenselabs.com/12081064-8878-477e-8092-564a240c69e2/Clould_service.json"
                                           },
                                           {
                                             "id": "0749b43f-b187-4a57-8119-8d72ea7dd01f",
                                             "name": "Database service",
                                             "vcUrl": "https://wizard-api.smart-x.smartsenselabs.com/12081064-8878-477e-8092-564a240c69e2/Database_service.json"
                                           },
                                           {
                                             "id": "c6a04238-fa3a-45b9-af1d-1d92872bcaf3",
                                             "name": "Sertvice_offer_1",
                                             "vcUrl": "https://wizard-api.smart-x.smartsenselabs.com/12081064-8878-477e-8092-564a240c69e2/service_QTJG.json"
                                           },
                                           {
                                             "id": "bcb1e1e1-347e-4aa8-ba38-ac35d331e2c6",
                                             "name": "Sertvice_of_1",
                                             "vcUrl": "https://wizard-api.smart-x.smartsenselabs.com/12081064-8878-477e-8092-564a240c69e2/service_6vSP.json"
                                           }
                                         ],
                                         "pageable": {
                                           "pageSize": 5,
                                           "totalPages": 4,
                                           "pageNumber": 0,
                                           "numberOfElements": 5,
                                           "totalElements": 18
                                         }
                                       }
                                    }"""
                            )
                    })
            }),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = {
                    @Content()
            }),
    })
    @PostMapping(path = SERVICE_OFFER_FILTER, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<PageResponse<ServiceFilterResponse>> getServiceOfferingFilter(@Valid @RequestBody FilterRequest filterRequest) {
        return CommonResponse.of(this.serviceOfferService.filterServiceOffering(filterRequest, null));
    }

    @Operation(summary = "Get service list for logged in participant")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(examples = {
                    @ExampleObject(name = "Filter request with sort",
                            value = """
                                    {
                                      "page": 0,
                                      "size": 5,
                                      "sort": {
                                        "column": "name",
                                        "sortType": "ASC"
                                      }
                                    }"""
                    ),
                    @ExampleObject(name = "Filter request with sort and search",
                            value = """
                                    {
                                      "page": 0,
                                      "size": 5,
                                      "sort": {
                                        "column": "name",
                                        "sortType": "ASC"
                                      },
                                      "criteriaOperator": "AND",
                                      "criteria": [
                                        {
                                          "column": "name",
                                          "operator": "CONTAIN",
                                          "values": [
                                            "xyz"
                                          ]
                                        }
                                      ]
                                    }"""
                    ),
            })
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service list fetched successfully.", content = {
                    @Content(examples = {
                            @ExampleObject(name = "Successful request", value = """
                                    {
                                       "status": 200,
                                       "payload": {
                                         "content": [
                                           {
                                             "id": "0fa1180c-a8bf-4994-8798-66744886acea",
                                             "name": "Storage service",
                                             "vcUrl": "https://wizard-api.smart-x.smartsenselabs.com/12081064-8878-477e-8092-564a240c69e2/Storage service.json"
                                           },
                                           {
                                             "id": "ab06e01c-978a-459c-9104-1018cb5e1ec9",
                                             "name": "Clould service",
                                             "vcUrl": "https://wizard-api.smart-x.smartsenselabs.com/12081064-8878-477e-8092-564a240c69e2/Clould_service.json"
                                           },
                                           {
                                             "id": "0749b43f-b187-4a57-8119-8d72ea7dd01f",
                                             "name": "Database service",
                                             "vcUrl": "https://wizard-api.smart-x.smartsenselabs.com/12081064-8878-477e-8092-564a240c69e2/Database_service.json"
                                           },
                                           {
                                             "id": "c6a04238-fa3a-45b9-af1d-1d92872bcaf3",
                                             "name": "Sertvice_offer_1",
                                             "vcUrl": "https://wizard-api.smart-x.smartsenselabs.com/12081064-8878-477e-8092-564a240c69e2/service_QTJG.json"
                                           },
                                           {
                                             "id": "bcb1e1e1-347e-4aa8-ba38-ac35d331e2c6",
                                             "name": "Sertvice_of_1",
                                             "vcUrl": "https://wizard-api.smart-x.smartsenselabs.com/12081064-8878-477e-8092-564a240c69e2/service_6vSP.json"
                                           }
                                         ],
                                         "pageable": {
                                           "pageSize": 5,
                                           "totalPages": 4,
                                           "pageNumber": 0,
                                           "numberOfElements": 5,
                                           "totalElements": 18
                                         }
                                       }
                                    }"""
                            )
                    })
            }),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = {
                    @Content()
            }),
    })
    @PostMapping(path = PARTICIPANT_SERVICE_OFFER_FILTER, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<PageResponse<ServiceFilterResponse>> getServiceOfferingList(@PathVariable(value = "participantId") String participantId, @Valid @RequestBody FilterRequest filterRequest, Principal principal) {
        this.validateParticipantId(participantId, principal);
        return CommonResponse.of(this.serviceOfferService.filterServiceOffering(filterRequest, participantId));
    }

    @GetMapping(path = PARTICIPANT_SERVICE_OFFER_DETAILS, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<ServiceDetailResponse> getServiceOfferingDetails(@PathVariable(value = "participantId") String participantId, @PathVariable(value = "serviceOfferId") UUID serviceOfferId, Principal principal) {
        this.validateParticipantId(participantId, principal);
        return CommonResponse.of(this.serviceOfferService.getServiceOfferingById(serviceOfferId));
    }

}
