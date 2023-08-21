package eu.gaiax.wizard.controller;

import eu.gaiax.wizard.api.model.*;
import eu.gaiax.wizard.api.utils.Validate;
import eu.gaiax.wizard.core.service.domain.DomainService;
import eu.gaiax.wizard.core.service.k8s.K8SService;
import eu.gaiax.wizard.core.service.participant.ParticipantAndKeyResponse;
import eu.gaiax.wizard.core.service.participant.ParticipantService;
import eu.gaiax.wizard.core.service.participant.model.request.ParticipantCreationRequest;
import eu.gaiax.wizard.core.service.participant.model.request.ParticipantRegisterRequest;
import eu.gaiax.wizard.core.service.participant.model.request.ParticipantValidatorRequest;
import eu.gaiax.wizard.core.service.signer.SignerService;
import eu.gaiax.wizard.core.service.ssl.CertificateService;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.UUID;

import static eu.gaiax.wizard.utils.WizardRestConstant.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
public class ParticipantController extends BaseController {

    private final ParticipantService participantService;
    private final DomainService domainService;
    private final CertificateService certificateService;
    private final K8SService k8SService;
    private final SignerService signerService;

    @Operation(
            summary = "Check for user existence",
            description = "This endpoint used to check whether user exists or not."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Participant registered successfully.",
                    content = {
                            @Content(
                                    examples = {
                                            @ExampleObject(name = "Success Response", value = """
                                                    {
                                                       "status": 200,
                                                       "payload": {
                                                         "userRegistered": false
                                                       }
                                                    }                                                                                                        
                                                    """)
                                    }
                            )
                    })
    })
    @GetMapping(value = CHECK_REGISTRATION, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<CheckParticipantRegisteredResponse> checkIfParticipantRegistered(@RequestParam(name = "email") String email) {
        CheckParticipantRegisteredResponse checkParticipantRegisteredResponse = this.participantService.checkIfParticipantRegistered(email);
        String message = checkParticipantRegisteredResponse.userRegistered() ? "User is registered in the application" : "User is not registered in the application";
        return CommonResponse.of(checkParticipantRegisteredResponse, message);
    }

    @Operation(
            summary = "Register Participant",
            description = "This endpoint used to register participants."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(examples = {
                    @ExampleObject(name = "Register participant who don't have DID", value = """
                            {
                              "email": "participant@example.in",
                              "onboardRequest": {
                                "legalName": "Participant Example",
                                "shortName": "ParticipantShortName",
                                "credential": {
                                  "legalParticipant": {
                                    "credentialSubject": {
                                      "gx:legalName": "Participant Example",
                                      "gx:headquarterAddress": {
                                        "gx:countrySubdivisionCode": "BE-BRU"
                                      },
                                      "gx:legalAddress": {
                                        "gx:countrySubdivisionCode": "BE-BRU"
                                      }
                                    }
                                  },
                                  "legalRegistrationNumber": {
                                    "gx:leiCode": "9695007586XZAKPYJ703"
                                  }
                                },
                                "ownDid": false,
                                "entityType": "933fde99-d815-4d10-b414-ea2d88d10474",
                                "acceptedTnC": true
                              }
                            }
                            """),
                    @ExampleObject(name = "Register participant who has DID", value = """
                            {
                              "email": "participant@example.in",
                              "onboardRequest": {
                                "legalName": "Participant Example",
                                "shortName": "ParticipantShortName",
                                "credential": {
                                  "legalParticipant": {
                                    "credentialSubject": {
                                      "gx:legalName": "Participant Example",
                                      "gx:headquarterAddress": {
                                        "gx:countrySubdivisionCode": "BE-BRU"
                                      },
                                      "gx:legalAddress": {
                                        "gx:countrySubdivisionCode": "BE-BRU"
                                      }
                                    }
                                  },
                                  "legalRegistrationNumber": {
                                    "gx:leiCode": "9695007586XZAKPYJ703"
                                  }
                                },
                                "ownDid": true,
                                "entityType": "933fde99-d815-4d10-b414-ea2d88d10474",
                                "acceptedTnC": true
                              }
                            }
                            """)
            })
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Participant registered successfully.",
                    content = {
                            @Content(
                                    examples = {
                                            @ExampleObject(name = "Success Response", value = """
                                                    {
                                                      "status": 200,
                                                      "payload": {
                                                        "id": "785651fd-8292-4555-b05c-77c03cea2312",
                                                        "email": "participant@example.in",
                                                        "legalName": "Participant Example",
                                                        "shortName": "ParticipantShortName",
                                                        "entityType": {
                                                          "id": "933fde99-d815-4d10-b414-ea2d88d10474",
                                                          "type": "Social Enterprise",
                                                          "active": true
                                                        },
                                                        "domain": "ParticipantShortName.smart-x.smartsenselabs.com",
                                                        "participantType": "REGISTERED",
                                                        "status": 0,
                                                        "credential": "{\\"legalParticipant\\":{\\"credentialSubject\\":{\\"gx:legalName\\":\\"Participant Example\\",\\"gx:headquarterAddress\\":{\\"gx:countrySubdivisionCode\\":\\"BE-BRU\\"},\\"gx:legalAddress\\":{\\"gx:countrySubdivisionCode\\":\\"BE-BRU\\"}}},\\"legalRegistrationNumber\\":{\\"gx:leiCode\\":\\"9695007586XZAKPYJ703\\"}}",
                                                        "ownDidSolution": false
                                                      }
                                                    }                                                                                                        
                                                    """)
                                    }
                            )
                    }),
            @ApiResponse(responseCode = "400", description = "Invalid participant registered request.",
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
    @PostMapping(value = REGISTER, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<Participant> registerParticipant(@RequestBody ParticipantRegisterRequest request) {
        return CommonResponse.of(this.participantService.registerParticipant(request));
    }

    @Operation(
            summary = "Onboard Participant",
            description = "This endpoint used to onboard participants."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(examples = {
                    @ExampleObject(name = "Onboard Participant and store PrivateKey", value = """
                            {
                              "issuer": "did:web:eu.gaia-x.com",
                              "verificationMethod": "did:web:casio50.smart-x.smartsenselabs.com",
                              "privateKey": "pk",
                              "store": true,
                              "ownDid": true
                            }
                            """),
                    @ExampleObject(name = "Onboard Participant and do not store PrivateKey", value = """
                            {
                              "issuer": "did:web:eu.gaia-x.com",
                              "verificationMethod": "did:web:casio50.smart-x.smartsenselabs.com",
                              "privateKey": "pk",
                              "store": false,
                              "ownDid": true
                            }
                            """),
                    @ExampleObject(name = "Onboard Participant who selected they had their own DID during registration but do not have a DID solution", value = """
                            {
                               "ownDid": false
                            }
                            """),
                    @ExampleObject(name = "Onboard Participant without DID solution", value = """
                            {
                             
                            }
                            """)
            })
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Participant registered successfully.",
                    content = {
                            @Content(
                                    examples = {
                                            @ExampleObject(name = "Success Response", value = """
                                                    {
                                                      "status": 200,
                                                      "payload": {
                                                        "id": "785651fd-8292-4555-b05c-77c03cea2312",
                                                        "email": "participant@example.in",
                                                        "legalName": "Participant Example",
                                                        "shortName": "ParticipantShortName",
                                                        "entityType": {
                                                          "id": "933fde99-d815-4d10-b414-ea2d88d10474",
                                                          "type": "Social Enterprise",
                                                          "active": true
                                                        },
                                                        "domain": "ParticipantShortName.smart-x.smartsenselabs.com",
                                                        "participantType": "REGISTERED",
                                                        "status": 0,
                                                        "credential": "{\\"legalParticipant\\":{\\"credentialSubject\\":{\\"gx:legalName\\":\\"Participant Example\\",\\"gx:headquarterAddress\\":{\\"gx:countrySubdivisionCode\\":\\"BE-BRU\\"},\\"gx:legalAddress\\":{\\"gx:countrySubdivisionCode\\":\\"BE-BRU\\"}}},\\"legalRegistrationNumber\\":{\\"gx:leiCode\\":\\"9695007586XZAKPYJ703\\"}}",
                                                        "ownDidSolution": false
                                                      }
                                                    }                                                                                                        
                                                    """)
                                    }
                            )
                    }),
            @ApiResponse(responseCode = "400", description = "Invalid participant registered request.",
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
    @PostMapping(value = ONBOARD_PARTICIPANT, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<Participant> registerParticipant(@PathVariable("participantId") String participantId, @RequestBody ParticipantCreationRequest request) {
        return CommonResponse.of(this.participantService.initiateOnboardParticipantProcess(participantId, request));
    }

    @Operation(
            summary = "Validate Participant Json",
            description = "This endpoint used to validate participant json."
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Participant onboarded successfully.")})
    @PostMapping(value = VALIDATE_PARTICIPANT, consumes = APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String validateParticipant(@RequestBody ParticipantValidatorRequest request) {
        this.participantService.validateParticipant(request);
        return "Success";
    }

    @Operation(
            summary = "Fetch .well-known files",
            description = "This endpoint used to fetch well-known files like participant.json, did.json, x509certificate."
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "File Fetched successfully.",
            content = {
                    @Content(examples = {
                            @ExampleObject(name = "Request for did.json", value = """
                                    {
                                      "@context": [
                                        "https://www.w3.org/ns/did/v1"
                                      ],
                                      "id": "did:web:eu.gaia-x.com",
                                      "verificationMethod": [
                                        {
                                          "@context": "https://w3c-ccg.github.io/lds-jws2020/contexts/v1/",
                                          "id": "did:web:eu.gaia-x.com",
                                          "type": "JsonWebKey2020",
                                          "controller": "did:web:eu.gaia-x.com",
                                          "publicKeyJwk": {
                                            "kty": "RSA",
                                            "n": "pn9UgWteejXl-JO-rwbIb8srHIatCnSjBfHfhVL81n3Slavy_fNc-yLrQbCW756_OPtfNB236y3q5nmW03OFOmEsC5MJFjIwoXptr2DCrX-a_XJlmrrAek_FQkLiqkz0bKFiQ",
                                            "e": "AQAB",
                                            "alg": "PS256",
                                            "x5u": "https://eu.gaia-x.com/.well-known/x509CertificateChain.pem"
                                          }
                                        }
                                      ],
                                      "assertionMethod": [
                                        "did:web:eu.gaia-x.com#JWK2020-RSA"
                                      ]
                                    }
                                    """),
                            @ExampleObject(name = "Request for x509CertificateChain.pem", value = """
                                    -----BEGIN CERTIFICATE-----
                                    MIIFEjCCA/qgAwIBAgISBMK/VGNr4y/m2kkRwAsQB8qrMA0GCSqGSIb3DQEBCwUA
                                    MDIxCzAJBgNVBAYTAlVTMRYwFAYDVQQKEw1MZXQncyBFbmNyeXB0MQswCQYDVQQD
                                    EwJSMzAeFw0yMzA4MDcwOTMxMTVaFw0yMzExMDUwOTMxMTRaMC0xKzApBgNVBAMT
                                    CCd+jt6dHsx1QGDpIV038v3jMA0GCSqGSIb3DQEBCwUAA4IBAQBOFsr6uBjE/epc
                                    P/1h7mzCQoEhLNEWNmVMRdLq8Z1PufuIg7+R54eZnRQMMzpFAl1u36wogx/qX9aT
                                    A03kfArYkR568RwLl5rAkG8ZN55U1S/r5L1K3ZnUcILmHC6/Htf/RDG1zUJnq/EO
                                    KHE4II1oawk+xfTegr6LiEOTnITgFVitOAZhKo0DhGlWCRsTorapa8yhcA3M+hqC
                                    3du9zPLuZu86bJMKmBVWT8zjUEC5j7OiKtaibUCZnfxB6We2t94RVDM8lb0n7SPT
                                    YiN+8fxFRMmjOtDDf+oMw2JRUKZmRbTrexc7QlZ6Y84kJPbF7cTnTvjq0ZafNq15
                                    +2bBtwA3
                                    -----END CERTIFICATE-----
                                    """
                            )
                    })
            }
    ),
            @ApiResponse(responseCode = "404", description = "Invalid Host or file name.",
                    content = {
                            @Content(examples = {
                                    @ExampleObject(name = "Invalid Host or file name while requesting", value = """
                                            {
                                              "message": "File or host not found.",
                                              "status": 404,
                                              "payload": {
                                                "error": {
                                                  "message": "File or host not found.",
                                                  "status": 404,
                                                  "timeStamp": 1692351642185
                                                }
                                              }
                                            }                                                    
                                            """)
                            })
                    }
            )
    })
    @GetMapping(path = WELL_KNOWN, produces = APPLICATION_JSON_VALUE)
    public String getWellKnownFiles(@PathVariable(name = "fileName") String fileName, @RequestHeader(name = HttpHeaders.HOST) String host) throws IOException {
        return this.participantService.getWellKnownFiles(host, fileName);
    }


    @Operation(
            summary = "Get requested files like participant.json, service-offering.json and resource.json",
            description = "This endpoint used to fetch participant json details."
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "File Fetched successfully.",
            content = {
                    @Content(examples = {
                            @ExampleObject(name = "Request for did.json", value = """
                                    {
                                      "@context": [
                                        "https://www.w3.org/ns/did/v1"
                                      ],
                                      "id": "did:web:eu.gaia-x.com",
                                      "verificationMethod": [
                                        {
                                          "@context": "https://w3c-ccg.github.io/lds-jws2020/contexts/v1/",
                                          "id": "did:web:eu.gaia-x.com",
                                          "type": "JsonWebKey2020",
                                          "controller": "did:web:eu.gaia-x.com",
                                          "publicKeyJwk": {
                                            "kty": "RSA",
                                            "n": "pn9UgWteejXl-JO-rwbIb8srHIatCnSjBfHfhVL81n3Slavy_fNc-yLrQbCW756_OPtfNB236y3q5nmW03OFOmEsC5MJFjIwoXptr2DCrX-a_XJlmrrAek_FQkLiqkz0bKFiQ",
                                            "e": "AQAB",
                                            "alg": "PS256",
                                            "x5u": "https://eu.gaia-x.com/.well-known/x509CertificateChain.pem"
                                          }
                                        }
                                      ],
                                      "assertionMethod": [
                                        "did:web:eu.gaia-x.com#JWK2020-RSA"
                                      ]
                                    }
                                    """),
                            @ExampleObject(name = "Request for participant.json", value = """
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
                                              "id": "https://did:web:eu.gaia-x.com/5a75-4a88-a97b-730deb535405/participant.json#0",
                                              "type": "gx:LegalParticipant",
                                              "gx:legalRegistrationNumber": {
                                                "id": "https://did:web:eu.gaia-x.com/5a75-4a88-a97b-730deb535405/participant.json#1"
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
                                            "issuanceDate": "2023-08-03T19:48:09.70929462Z",
                                            "proof": {
                                              "type": "JsonWebSignature2020",
                                              "created": "2023-08-03T14:21:13.368Z",
                                              "proofPurpose": "assertionMethod",
                                              "verificationMethod": "did:web:eu.gaia-x.com",
                                              "jws": "eyJhbGci-1SoLy4ozdFz6jqqMixwBk-LnWwZzCt_SHXXLGMvninKIuhpDhbwZEH4AdIO3Dn4B3puNTsB1hAvwqiKF12773fnLsslHQ"
                                            }
                                          },
                                          {
                                            "@context": [
                                              "https://www.w3.org/2018/credentials/v1",
                                              "https://w3id.org/security/suites/jws-2020/v1"
                                            ],
                                            "type": "VerifiableCredential",
                                            "id": "https://eu.gaia-x.com/5a75-4a88-a97b-730deb535405/participant.json#1",
                                            "issuer": "did:web:registration.lab.gaia-x.eu:development",
                                            "issuanceDate": "2023-08-03T14:21:11.607Z",
                                            "credentialSubject": {
                                              "@context": "https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#",
                                              "type": "gx:legalRegistrationNumber",
                                              "id": "https://eu.gaia-x.com/5a75-4a88-a97b-730deb535405/participant.json#1",
                                              "gx:leiCode": "9695007586GCA7878703",
                                              "gx:leiCode-countryCode": "FR"
                                            },
                                            "evidence": [
                                              {
                                                "gx:evidenceURL": "https://api.gleif.org/api/v1/lei-records/",
                                                "gx:executionDate": "2023-08-03T14:21:11.607Z",
                                                "gx:evidenceOf": "gx:leiCode"
                                              }
                                            ],
                                            "proof": {
                                              "type": "JsonWebSignature2020",
                                              "created": "2023-08-03T14:21:12.662Z",
                                              "proofPurpose": "assertionMethod",
                                              "verificationMethod": "did:web:registration.lab.gaia-x.eu:development#X509-JWK2020",
                                              "jws": "ey5TUZsqDmipdzBjinUnhI4I10l9FxTGez-sMhTIIj3nEvclXlBAHvmj8Rqu_mtBwhV094TZU6mdPf8SV2ttW3riWAhkjCuDicIYyivTqfCWiXcIiQL8ilciyyftlv-wCl9vlbZqfd0lvF6A"
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
                                              "gx:termsAndConditions": "The PARTICIPANT signing the Self-Description agrees as follows:\n- to update its descriptions about any changes, be it technical, organizational, or legal - especially but not limited to contractual in regards to the indicated attributes present in the descriptions.\n\nThe keypair used to sign Verifiable Credentials will be revoked where Gaia-X Association becomes aware of any inaccurate statements in regards to the claims which result in a non-compliance with the Trust Framework and policy rules defined in the Policy Rules and Labelling Document (PRLD).",
                                              "id": "https://eu.gaia-x.com/5a75-4a88-a97b-730deb535405/participant.json#2",
                                              "type": "gx:GaiaXTermsAndConditions"
                                            },
                                            "issuanceDate": "2023-08-03T19:48:09.70929462Z",
                                            "type": [
                                              "VerifiableCredential"
                                            ],
                                            "proof": {
                                              "type": "JsonWebSignature2020",
                                              "created": "2023-08-03T14:21:13.951Z",
                                              "proofPurpose": "assertionMethod",
                                              "verificationMethod": "did:web:eu.gaia-x.com",
                                              "jws": "eyJhbGciOiJQUzI1NiIsImI2NCI6ZmFsc2UsImNyaXQiOlsiYjY0Il19..TOYLKLRj9yB2Z33liWTsUJjnmnQKDtg4a5-ahRdFcA1Oj1vvTngbINe-4PNPnY4ICjke25x8TvT0oR_jV2Asc1kz15CGvkNDxHGowjYEbibekXzHbO8oGhu7JWDewXyH74byNXKueFgBp3z0ZM7BUHo07ExkygQPgiEX2UuuhTmEdmcKxrgECdHIgIqJ0UGeRG6KZuHPgv7GhyxESVggu9wLnsU7gIjMAOFO81ita5O7zOwTBt4xwOM0i2K1SNPl16jVaQ54tiHdqWy8HfZTSoKWi1gPig"
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
                                        "id": "https://compliance.lab.gaia-x.eu/development/credential-offers/f3a819c6-d1e3-416a-9bf3-f4bb267cccd8",
                                        "issuer": "did:web:compliance.lab.gaia-x.eu:development",
                                        "issuanceDate": "2023-08-03T14:21:21.709Z",
                                        "expirationDate": "2023-11-01T14:21:21.709Z",
                                        "credentialSubject": [
                                          {
                                            "type": "gx:compliance",
                                            "id": "https://eu.gaia-x.com/d3ef8323-5a75-4a88-a97b-730deb535405/participant.json#0",
                                            "integrity": "sha256-2c99feafb0ee27cbf0dbf6ffa5e783a3ae4cfc8bf851a63594695fe1b000",
                                            "version": "22.10"
                                          },
                                          {
                                            "type": "gx:compliance",
                                            "id": "https://eu.gaia-x.com/d3ef8323-5a75-4a88-a97b-730deb535405/participant.json#1",
                                            "integrity": "sha256-fc633407a537cba7c68b27f1198bc34becbee50a855fd7af03aa37fcdd5e",
                                            "version": "22.10"
                                          },
                                          {
                                            "type": "gx:compliance",
                                            "id": "https://eu.gaia-x.com/d3ef8323-5a75-4a88-a97b-730deb535405/participant.json#2",
                                            "integrity": "sha256-eba06a7c987b800410ee02de36c2c87a76756a23f0d346a5761042690b1",
                                            "version": "22.10"
                                          }
                                        ],
                                        "proof": {
                                          "type": "JsonWebSignature2020",
                                          "created": "2023-08-03T14:21:22.357Z",
                                          "proofPurpose": "assertionMethod",
                                          "jws": "eyJhbGciOiJQUzI1NiIs-mwpFVdVA1UMScaVKUZwKD8f6EkmQvzlyw",
                                          "verificationMethod": "did:web:compliance.lab.gaia-x.eu:development#X509-JWK2020"
                                        }
                                      }
                                    }
                                    """),
                    })
            }
    ),
            @ApiResponse(responseCode = "404", description = "Invalid Host or file name.",
                    content = {
                            @Content(examples = {
                                    @ExampleObject(name = "Invalid Host or file name while requesting", value = """
                                            {
                                              "message": "File or host not found.",
                                              "status": 404,
                                              "payload": {
                                                "error": {
                                                  "message": "File or host not found.",
                                                  "status": 404,
                                                  "timeStamp": 1692351642185
                                                }
                                              }
                                            }""")
                            })
                    }
            )
    })
    @GetMapping(path = PARTICIPANT_JSON, produces = APPLICATION_JSON_VALUE)
    public String getLegalParticipantJson(@PathVariable(name = "participantId") String participantId, @PathVariable("fileName") String fileName) throws IOException {
        return this.participantService.getLegalParticipantJson(participantId, fileName);
    }

    @Operation(summary = "Resume onboarding process from sub domain creation, role Admin, (only used for manual step in case of failure)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subdomain creation started.",
                    content = {
                            @Content(examples = {
                                    @ExampleObject(name = "Subdomain creation started.", value = """
                                            {
                                              "status": 200,
                                              "payload": {
                                                "message": "Subdomain creation started"
                                              }
                                            }""")
                            })
                    }
            ),
            @ApiResponse(responseCode = "404", description = "Participant not found.",
                    content = {
                            @Content(examples = {
                                    @ExampleObject(name = "Participant details not found.", value = """
                                            {
                                              "message": "Participant not found.",
                                              "status": 404,
                                              "payload": {
                                                "error": {
                                                  "message": "Participant not found.",
                                                  "status": 404,
                                                  "timeStamp": 1692352668993
                                                }
                                              }
                                            }""")
                            })
                    }
            )
    })
    @GetMapping(path = PARTICIPANT_SUBDOMAIN, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<Map<String, String>> createSubDomain(@PathVariable(name = "participantId") String participantId) {
        this.domainService.createSubDomain(UUID.fromString(participantId));
        return CommonResponse.of(Map.of("message", "Subdomain creation started"));
    }

    @Operation(summary = "Resume onboarding process from SLL certificate creation, role = admin, (only used for manual step in case of failure)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Certificate issuing process has been started.",
                    content = {
                            @Content(examples = {
                                    @ExampleObject(name = "Subdomain creation started.", value = """
                                            {
                                               "status": 200,
                                               "payload": {
                                                 "id": "b1ab279c-9a77-4528-89b1-abfa6a8ce56a",
                                                 "email": "hexica6252@dusyum.com",
                                                 "legalName": "Hexica",
                                                 "shortName": "hex",
                                                 "entityTypeId": "933fde99-d815-4d10-b414-ea2d88d10474",
                                                 "entityType": {
                                                   "id": "933fde99-d815-4d10-b414-ea2d88d10474",
                                                   "type": "Social Enterprise",
                                                   "active": true,
                                                   "hibernateLazyInitializer": {}
                                                 },
                                                 "domain": "hex.smart-x.smartsenselabs.com",
                                                 "participantType": "REGISTERED",
                                                 "status": 3,
                                                 "credential": "{\\"legalParticipant\\":{\\"credentialSubject\\":{\\"gx:legalName\\":\\"Hex World\\",\\"gx:headquarterAddress\\":{\\"gx:countrySubdivisionCode\\":\\"BE-BRU\\"},\\"gx:legalAddress\\":{\\"gx:countrySubdivisionCode\\":\\"BE-BRU\\"}}},\\"legalRegistrationNumber\\":{\\"gx:leiCode\\":\\"9695007586GCAKPYJ701\\",\\"gx:vatID\\":\\"BE0762747722\\",\\"gx:EORI\\":\\"FR53740792600015\\"}}",
                                                 "ownDidSolution": false
                                               }
                                             }""")
                            })
                    }
            ),
            @ApiResponse(responseCode = "404", description = "Participant not found.",
                    content = {
                            @Content(examples = {
                                    @ExampleObject(name = "Participant details not found.", value = """
                                            {
                                              "message": "Participant not found.",
                                              "status": 404,
                                              "payload": {
                                                "error": {
                                                  "message": "Participant not found.",
                                                  "status": 404,
                                                  "timeStamp": 1692352668993
                                                }
                                              }
                                            }""")
                            })
                    }
            )
    })
    @GetMapping(path = PARTICIPANT_CERTIFICATE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<Participant> createCertificate(@PathVariable(name = "participantId") String participantId) {
        Participant participant = this.participantService.get(UUID.fromString(participantId));
        Validate.isTrue(participant.getStatus() != RegistrationStatus.CERTIFICATE_CREATION_FAILED.getStatus()).launch("Status is not certification creation failed");
        participant = this.participantService.changeStatus(UUID.fromString(participantId), RegistrationStatus.CERTIFICATE_CREATION_IN_PROCESS.getStatus());
        this.certificateService.createSSLCertificate(UUID.fromString(participantId), null);
        return CommonResponse.of(participant);
    }


    @Operation(summary = "Resume onboarding process from ingress creation, role = admin, (only used for manual step in case of failure)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ingress creation started",
                    content = {
                            @Content(examples = {
                                    @ExampleObject(name = "Ingress creation started", value = """
                                            {
                                              "status": 200,
                                              "payload": {
                                                "message": "Ingress creation started"
                                              }
                                            }""")
                            })
                    }
            ),
            @ApiResponse(responseCode = "404", description = "Participant not found.",
                    content = {
                            @Content(examples = {
                                    @ExampleObject(name = "Participant details not found.", value = """
                                            {
                                              "message": "Participant not found.",
                                              "status": 404,
                                              "payload": {
                                                "error": {
                                                  "message": "Participant not found.",
                                                  "status": 404,
                                                  "timeStamp": 1692352668993
                                                }
                                              }
                                            }""")
                            })
                    }
            )
    })
    @GetMapping(path = PARTICIPANT_INGRESS, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<Map<String, String>> createIngress(@PathVariable(name = "participantId") String participantId) {
        this.k8SService.createIngress(UUID.fromString(participantId));
        return CommonResponse.of(Map.of("message", "Ingress creation started"));
    }

    @Operation(summary = "Resume onboarding process from did creation, role-=admin, (only used for manual step in case of failure)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "did creation started",
                    content = {
                            @Content(examples = {
                                    @ExampleObject(name = "did creation started.", value = """
                                            {
                                              "status": 200,
                                              "payload": {
                                                "message": "did creation started"
                                              }
                                            }""")
                            })
                    }
            ),
            @ApiResponse(responseCode = "404", description = "Participant not found.",
                    content = {
                            @Content(examples = {
                                    @ExampleObject(name = "Participant details not found.", value = """
                                            {
                                              "message": "Participant not found.",
                                              "status": 404,
                                              "payload": {
                                                "error": {
                                                  "message": "Participant not found.",
                                                  "status": 404,
                                                  "timeStamp": 1692352668993
                                                }
                                              }
                                            }""")
                            })
                    }
            )
    })
    @GetMapping(path = PARTICIPANT_DID, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<Map<String, String>> createDid(@PathVariable(name = "participantId") String participantId) {
        this.signerService.createDid(UUID.fromString(participantId));
        return CommonResponse.of(Map.of("message", "did creation started"));
    }

    @Operation(summary = "Resume onboarding process from participant credential creation, role Admin, (only used for manual step in case of failure)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Participant json creation started.",
                    content = {
                            @Content(examples = {
                                    @ExampleObject(name = "Participant json creation started.", value = """
                                            {
                                              "status": 200,
                                              "payload": {
                                                "message": "Participant json creation started."
                                              }
                                            }""")
                            })
                    }
            ),
            @ApiResponse(responseCode = "404", description = "Participant not found.",
                    content = {
                            @Content(examples = {
                                    @ExampleObject(name = "Participant details not found.", value = """
                                            {
                                              "message": "Participant not found.",
                                              "status": 404,
                                              "payload": {
                                                "error": {
                                                  "message": "Participant not found.",
                                                  "status": 404,
                                                  "timeStamp": 1692352668993
                                                }
                                              }
                                            }""")
                            })
                    }
            )
    })
    @GetMapping(path = CREATE_PARTICIPANT, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<Map<String, String>> createParticipantJson(@PathVariable(name = "participantId") String participantId) {
        this.signerService.createParticipantJson(UUID.fromString(participantId));
        return CommonResponse.of(Map.of("message", "participant json creation started"));
    }

    @Operation(
            summary = "Participant config",
            description = "This endpoint returns participant's general configuration."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Participant config fetched successfully.",
                    content = {
                            @Content(examples = {
                                    @ExampleObject(name = "Success response.", value = """
                                            {
                                               "status": 200,
                                               "payload": {
                                                 "id": "b1ab279c-9a77-4528-89b1-abfa6a8ce56a",
                                                 "email": "admin@smartsensesolutions.com",
                                                 "legalName": "smartSense Consulting Solutions",
                                                 "participantType": "REGISTERED",
                                                 "ownDidSolution": false,
                                                 "status": 3
                                               }
                                            }""")
                            })
                    }),
            @ApiResponse(responseCode = "401", description = "Unauthorized access."),
            @ApiResponse(responseCode = "403", description = "User does not have access to this API."),
            @ApiResponse(responseCode = "404", description = "Participant not found.",
                    content = {
                            @Content(examples = {
                                    @ExampleObject(name = "Success response.", value = """
                                            {
                                               "message": "Participant not found.",
                                               "status": 404,
                                               "payload": {
                                                 "error": {
                                                   "message": "Participant not found.",
                                                   "status": 404,
                                                   "timeStamp": 1692425548427
                                                 }
                                               }
                                            }""")
                            })
                    })
    })
    @GetMapping(PARTICIPANT_CONFIG)
    public CommonResponse<ParticipantConfigDTO> getConfig(Principal principal) {
        String participantId = (String) this.requestForClaim(StringPool.ID, principal);
        return CommonResponse.of(this.participantService.getParticipantConfig(participantId));
    }

    @Operation(
            summary = "Resend registration email",
            description = "This endpoint sends registration email to the user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email sent successfully."),
            @ApiResponse(responseCode = "400", description = "User not registered.")
    })
    @PostMapping(SEND_REQUIRED_ACTIONS_EMAIL)
    public CommonResponse<Object> sendRequiredActionsEmail(@RequestBody SendRegistrationEmailRequest sendRegistrationEmailRequest) {
        this.participantService.sendRegistrationLink(sendRegistrationEmailRequest.email());
        return CommonResponse.of("Registration email sent successfully");
    }

    @Operation(
            summary = "Participant export",
            description = "This endpoint returns participant json and private key (optional) for newly created participant."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Participant exported successfully."),
            @ApiResponse(responseCode = "401", description = "Unauthorized access."),
            @ApiResponse(responseCode = "403", description = "User does not have access to this API."),
            @ApiResponse(responseCode = "404", description = "Participant not found.")
    })
    @GetMapping(PARTICIPANT_EXPORT)
    public CommonResponse<ParticipantAndKeyResponse> exportParticipantAndKey(@PathVariable(name = StringPool.PARTICIPANT_ID) String participantId) {
        return CommonResponse.of(this.participantService.exportParticipantAndKey(participantId));
    }
}
