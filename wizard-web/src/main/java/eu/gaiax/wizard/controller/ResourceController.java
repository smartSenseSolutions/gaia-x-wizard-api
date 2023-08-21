package eu.gaiax.wizard.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.smartsensesolutions.java.commons.FilterRequest;
import eu.gaiax.wizard.api.model.CommonResponse;
import eu.gaiax.wizard.api.model.PageResponse;
import eu.gaiax.wizard.api.model.ServiceAndResourceListDTO;
import eu.gaiax.wizard.api.model.StringPool;
import eu.gaiax.wizard.api.model.service_offer.CreateResourceRequest;
import eu.gaiax.wizard.core.service.service_offer.ResourceService;
import eu.gaiax.wizard.dao.entity.resource.Resource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

import static eu.gaiax.wizard.utils.WizardRestConstant.RESOURCE_LIST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
@Tag(name = "resources")
public class ResourceController extends BaseController {

    private final ResourceService resourceService;


    @Operation(summary = "Create Resource")
    @PostMapping(path = "/resource", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(examples = {
                    @ExampleObject(name = "Create physical resource", value = """
                            {
                                  "verificationMethod": "did:web:example.com",
                                "email":"exmaple@gmail.com",
                                 "privateKey": "-----BEGIN PRIVATE KEY---  ----END PRIVATE KEY-----",
                                  "credentialSubject": {
                                    "type": "gx:PhysicalResource",
                                    "gx:name": "Test Resource 1",
                                    "gx:description": "Test Resource 1 description",
                                    "gx:maintainedBy": [
                                      {
                                        "id": "https://wizard.lab.gaia-x.eu/api/credentials/2d37wbGvQzbAQ84yRouh2m2vBKkN8s5AfH9Q75HZRCUQmJW7yAVSNKzjJj6gcjE2mDNDUHCichXWdMH3S2c8AaDLm3kXmf5R8DFPWTYo5iRYkn8kvgU3AjMXc2qTbhuMHCpucKGgT1ZMkcHUygZkt11iD3T8VJNKYwsdk4MGoZwdqoFUuTKVcsXVTBA4ofD1Dtqzjavyng5WUpvJf4gRyfGkMvYYuHCgay8TK8Dayt6Rhcs3r2d1gRCg2UV419S9CpWZGwKQNEXdYbaB2eTiNbQ83KMd4mj1oSJgF7LLDZLJtKJbhwLzR3x35QUqEGevRxnRDKoPdHrEZN7r9TVAmvr9rt7Xq8eB4zGMTza59hisEAUaHsmWQNaVDorqFyZgN5bXswMK1irVQ5SVR9osCCRrKUKkntxfakjmSqapPfveMP39vkgTXfEhsfLUZXGwFcpgLpWxWRn1QLnJY11BVymS7DyaSvbSKotNFQxyV6vghfM2Jetw1mLxU5qsQqDYnDYJjPZQSmkwxjX3yenPVCz6N2ox83tj9AuuQrzg5p2iukNdunDd2QCsHaMEtTq9JVLzXtWs2eZbPkxCBEQwoKTGGVhKu5yxZjCtQGc#9894e9b0a38aa105b50bb9f4e7d0975641273416e70f166f4bd9fd1b00dfe81d"
                                      }
                                    ],
                                    "gx:ownedBy": [
                                      {
                                        "id": "https://wizard.lab.gaia-x.eu/api/credentials/2d37wbGvQzbAQ84yRouh2m2vBKkN8s5AfH9Q75HZRCUQmJW7yAVSNKzjJj6gcjE2mDNDUHCichXWdMH3S2c8AaDLm3kXmf5R8DFPWTYo5iRYkn8kvgU3AjMXc2qTbhuMHCpucKGgT1ZMkcHUygZkt11iD3T8VJNKYwsdk4MGoZwdqoFUuTKVcsXVTBA4ofD1Dtqzjavyng5WUpvJf4gRyfGkMvYYuHCgay8TK8Dayt6Rhcs3r2d1gRCg2UV419S9CpWZGwKQNEXdYbaB2eTiNbQ83KMd4mj1oSJgF7LLDZLJtKJbhwLzR3x35QUqEGevRxnRDKoPdHrEZN7r9TVAmvr9rt7Xq8eB4zGMTza59hisEAUaHsmWQNaVDorqFyZgN5bXswMK1irVQ5SVR9osCCRrKUKkntxfakjmSqapPfveMP39vkgTXfEhsfLUZXGwFcpgLpWxWRn1QLnJY11BVymS7DyaSvbSKotNFQxyV6vghfM2Jetw1mLxU5qsQqDYnDYJjPZQSmkwxjX3yenPVCz6N2ox83tj9AuuQrzg5p2iukNdunDd2QCsHaMEtTq9JVLzXtWs2eZbPkxCBEQwoKTGGVhKu5yxZjCtQGc#9894e9b0a38aa105b50bb9f4e7d0975641273416e70f166f4bd9fd1b00dfe81d"
                                      }
                                    ],
                                    "gx:manufacturedBy": [
                                      {
                                        "id": "https://wizard.lab.gaia-x.eu/api/credentials/2d37wbGvQzbAQ84yRouh2m2vBKkN8s5AfH9Q75HZRCUQmJW7yAVSNKzjJj6gcjE2mDNDUHCichXWdMH3S2c8AaDLm3kXmf5R8DFPWTYo5iRYkn8kvgU3AjMXc2qTbhuMHCpucKGgT1ZMkcHUygZkt11iD3T8VJNKYwsdk4MGoZwdqoFUuTKVcsXVTBA4ofD1Dtqzjavyng5WUpvJf4gRyfGkMvYYuHCgay8TK8Dayt6Rhcs3r2d1gRCg2UV419S9CpWZGwKQNEXdYbaB2eTiNbQ83KMd4mj1oSJgF7LLDZLJtKJbhwLzR3x35QUqEGevRxnRDKoPdHrEZN7r9TVAmvr9rt7Xq8eB4zGMTza59hisEAUaHsmWQNaVDorqFyZgN5bXswMK1irVQ5SVR9osCCRrKUKkntxfakjmSqapPfveMP39vkgTXfEhsfLUZXGwFcpgLpWxWRn1QLnJY11BVymS7DyaSvbSKotNFQxyV6vghfM2Jetw1mLxU5qsQqDYnDYJjPZQSmkwxjX3yenPVCz6N2ox83tj9AuuQrzg5p2iukNdunDd2QCsHaMEtTq9JVLzXtWs2eZbPkxCBEQwoKTGGVhKu5yxZjCtQGc#9894e9b0a38aa105b50bb9f4e7d0975641273416e70f166f4bd9fd1b00dfe81d"
                                      }
                                    ],
                                    "gx:locationAddress": [
                                      {
                                        "gx:countryCode": "FR-DE"
                                      }
                                    ],
                                    "gx:location": [
                                      {
                                        "gx:gps": "35.89421911 139.94637467"
                                      }
                                    ]
                                  }
                                }
                            """),
                    @ExampleObject(name = "Create virtual resource , Software resource", value = """
                                                        
                                 {
                                  "email":"exmaple@gmail.com",
                                   "verificationMethod": "did:web:example.com",
                                 "privateKey": "-----BEGIN PRIVATE KEY---  ----END PRIVATE KEY-----",
                                   "credentialSubject": {
                                     "type": "VirtualResource",
                                     "subType":"VirtualSoftwareResource",
                                     "gx:name": "Soft_res_sing_2",
                                    "gx:description": "sign Test Resource 2 description",
                                     "gx:copyrightOwnedBy": [
                                     {"id":"https://wizard-api.smart-x.smartsenselabs.com/12081064-8878-477e-8092-564a240c69e2/participant.json" }
                                     ],
                                     "gx:license": [ "http://smartproof.in/.well-known/license"
                             ],     
                               "gx:aggregationOf": [{"id":"https://wizard-api.smart-x.smartsenselabs.com/12081064-8878-477e-8092-564a240c69e2/resource_b5b7e6b0-ae24-4458-b3f9-27572abc39e7.json"}]
                             
                                   }
                                                        
                                                        }
                            """),
                    @ExampleObject(name = "Create virtual resource , Data resource", value = """
                               {
                                 "email": "exmaple@gmail.com",
                                    "verificationMethod": "did:web:example.com",
                                 "privateKey": "-----BEGIN PRIVATE KEY---  ----END PRIVATE KEY-----",
                                 "credentialSubject": {
                                   "type": "VirtualResource",
                                   "subType": "VirtualDataResource",
                                   "gx:name": "Test Resource 1",
                                   "gx:description": "Test Resource 1 description",
                                   "gx:copyrightOwnedBy": [
                                     {
                                       "id": "https://wizard.lab.gaia-x.eu/api/credentials/2d37wbGvQzbAQ84yRouh2m2vBKkN8s5AfH9Q75HZRCUQmJW7yAVSNKzjJj6gcjE2mDNDUHCichXWdMH3S2c8AaDLm3kXmf5R8DFPWTYo5iRYkn8kvgU3AjMXc2qTbhuMHCpucKGgT1ZMkcHUygZkt11iD3T8VJNKYwsdk4MGoZwdqoFUuTKVcsXVTBA4ofD1Dtqzjavyng5WUpvJf4gRyfGkMvYYuHCgay8TK8Dayt6Rhcs3r2d1gRCg2UV419S9CpWZGwKQNEXdYbaB2eTiNbQ83KMd4mj1oSJgF7LLDZLJtKJbhwLzR3x35QUqEGevRxnRDKoPdHrEZN7r9TVAmvr9rt7Xq8eB4zGMTza59hisEAUaHsmWQNaVDorqFyZgN5bXswMK1irVQ5SVR9osCCRrKUKkntxfakjmSqapPfveMP39vkgTXfEhsfLUZXGwFcpgLpWxWRn1QLnJY11BVymS7DyaSvbSKotNFQxyV6vghfM2Jetw1mLxU5qsQqDYnDYJjPZQSmkwxjX3yenPVCz6N2ox83tj9AuuQrzg5p2iukNdunDd2QCsHaMEtTq9JVLzXtWs2eZbPkxCBEQwoKTGGVhKu5yxZjCtQGc#9894e9b0a38aa105b50bb9f4e7d0975641273416e70f166f4bd9fd1b00dfe81d"
                                     }
                                   ],
                                   "gx:license": [
                                     "http://smartproof.in/.well-known/license"
                                   ],
                                   "gx:producedBy": {
                                     "id": "https://wizard.lab.gaia-x.eu/api/credentials/2d37wbGvQzbAQ84yRouh2m2vBKkN8s5AfH9Q75HZRCUQmJW7yAVSNKzjJj6gcjE2mDNDUHCic"
                                   },
                                   "gx:exposedThrough": [
                                     "http://smartproof.in/api/test-resource-1"
                                   ],
                                   "gx:containsPII": false
                                 }
                               }                   
                            """)
            })
    })
    public CommonResponse<Resource> createResource(@Valid @RequestBody CreateResourceRequest request, Principal principal) throws JsonProcessingException {
        return CommonResponse.of(this.resourceService.createResource(request, this.requestForClaim(StringPool.ID, principal).toString()));
    }

    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(examples = {
                    @ExampleObject(name = "Create physical resource", value = """
                            {
                                  "verificationMethod": "did:web:example.com",
                                 "privateKey": "-----BEGIN PRIVATE KEY---  ----END PRIVATE KEY-----",
                                 "participantJsonUrl": "https://example.com/12081064-8878-477e-8092-564a240c69e2/participant.json",
                                  "credentialSubject": {
                                    "type": "gx:PhysicalResource",
                                    "gx:name": "Test Resource 1",
                                    "gx:description": "Test Resource 1 description",
                                    "gx:maintainedBy": [
                                      {
                                        "id": "https://wizard.lab.gaia-x.eu/api/credentials/2d37wbGvQzbAQ84yRouh2m2vBKkN8s5AfH9Q75HZRCUQmJW7yAVSNKzjJj6gcjE2mDNDUHCichXWdMH3S2c8AaDLm3kXmf5R8DFPWTYo5iRYkn8kvgU3AjMXc2qTbhuMHCpucKGgT1ZMkcHUygZkt11iD3T8VJNKYwsdk4MGoZwdqoFUuTKVcsXVTBA4ofD1Dtqzjavyng5WUpvJf4gRyfGkMvYYuHCgay8TK8Dayt6Rhcs3r2d1gRCg2UV419S9CpWZGwKQNEXdYbaB2eTiNbQ83KMd4mj1oSJgF7LLDZLJtKJbhwLzR3x35QUqEGevRxnRDKoPdHrEZN7r9TVAmvr9rt7Xq8eB4zGMTza59hisEAUaHsmWQNaVDorqFyZgN5bXswMK1irVQ5SVR9osCCRrKUKkntxfakjmSqapPfveMP39vkgTXfEhsfLUZXGwFcpgLpWxWRn1QLnJY11BVymS7DyaSvbSKotNFQxyV6vghfM2Jetw1mLxU5qsQqDYnDYJjPZQSmkwxjX3yenPVCz6N2ox83tj9AuuQrzg5p2iukNdunDd2QCsHaMEtTq9JVLzXtWs2eZbPkxCBEQwoKTGGVhKu5yxZjCtQGc#9894e9b0a38aa105b50bb9f4e7d0975641273416e70f166f4bd9fd1b00dfe81d"
                                      }
                                    ],
                                    "gx:ownedBy": [
                                      {
                                        "id": "https://wizard.lab.gaia-x.eu/api/credentials/2d37wbGvQzbAQ84yRouh2m2vBKkN8s5AfH9Q75HZRCUQmJW7yAVSNKzjJj6gcjE2mDNDUHCichXWdMH3S2c8AaDLm3kXmf5R8DFPWTYo5iRYkn8kvgU3AjMXc2qTbhuMHCpucKGgT1ZMkcHUygZkt11iD3T8VJNKYwsdk4MGoZwdqoFUuTKVcsXVTBA4ofD1Dtqzjavyng5WUpvJf4gRyfGkMvYYuHCgay8TK8Dayt6Rhcs3r2d1gRCg2UV419S9CpWZGwKQNEXdYbaB2eTiNbQ83KMd4mj1oSJgF7LLDZLJtKJbhwLzR3x35QUqEGevRxnRDKoPdHrEZN7r9TVAmvr9rt7Xq8eB4zGMTza59hisEAUaHsmWQNaVDorqFyZgN5bXswMK1irVQ5SVR9osCCRrKUKkntxfakjmSqapPfveMP39vkgTXfEhsfLUZXGwFcpgLpWxWRn1QLnJY11BVymS7DyaSvbSKotNFQxyV6vghfM2Jetw1mLxU5qsQqDYnDYJjPZQSmkwxjX3yenPVCz6N2ox83tj9AuuQrzg5p2iukNdunDd2QCsHaMEtTq9JVLzXtWs2eZbPkxCBEQwoKTGGVhKu5yxZjCtQGc#9894e9b0a38aa105b50bb9f4e7d0975641273416e70f166f4bd9fd1b00dfe81d"
                                      }
                                    ],
                                    "gx:manufacturedBy": [
                                      {
                                        "id": "https://wizard.lab.gaia-x.eu/api/credentials/2d37wbGvQzbAQ84yRouh2m2vBKkN8s5AfH9Q75HZRCUQmJW7yAVSNKzjJj6gcjE2mDNDUHCichXWdMH3S2c8AaDLm3kXmf5R8DFPWTYo5iRYkn8kvgU3AjMXc2qTbhuMHCpucKGgT1ZMkcHUygZkt11iD3T8VJNKYwsdk4MGoZwdqoFUuTKVcsXVTBA4ofD1Dtqzjavyng5WUpvJf4gRyfGkMvYYuHCgay8TK8Dayt6Rhcs3r2d1gRCg2UV419S9CpWZGwKQNEXdYbaB2eTiNbQ83KMd4mj1oSJgF7LLDZLJtKJbhwLzR3x35QUqEGevRxnRDKoPdHrEZN7r9TVAmvr9rt7Xq8eB4zGMTza59hisEAUaHsmWQNaVDorqFyZgN5bXswMK1irVQ5SVR9osCCRrKUKkntxfakjmSqapPfveMP39vkgTXfEhsfLUZXGwFcpgLpWxWRn1QLnJY11BVymS7DyaSvbSKotNFQxyV6vghfM2Jetw1mLxU5qsQqDYnDYJjPZQSmkwxjX3yenPVCz6N2ox83tj9AuuQrzg5p2iukNdunDd2QCsHaMEtTq9JVLzXtWs2eZbPkxCBEQwoKTGGVhKu5yxZjCtQGc#9894e9b0a38aa105b50bb9f4e7d0975641273416e70f166f4bd9fd1b00dfe81d"
                                      }
                                    ],
                                    "gx:locationAddress": [
                                      {
                                        "gx:countryCode": "FR-DE"
                                      }
                                    ],
                                    "gx:location": [
                                      {
                                        "gx:gps": "35.89421911 139.94637467"
                                      }
                                    ]
                                  }
                                }
                            """),
                    @ExampleObject(name = "Create virtual resource , Software resource", value = """
                                                        
                                 {
                                  "participantJsonUrl": "https://example.com/12081064-8878-477e-8092-564a240c69e2/participant.json",
                                   "verificationMethod": "did:web:example.com",
                                 "privateKey": "-----BEGIN PRIVATE KEY---  ----END PRIVATE KEY-----",
                                   "credentialSubject": {
                                     "type": "VirtualResource",
                                     "subType":"VirtualSoftwareResource",
                                     "gx:name": "Soft_res_sing_2",
                                    "gx:description": "sign Test Resource 2 description",
                                     "gx:copyrightOwnedBy": [
                                     {"id":"https://wizard-api.smart-x.smartsenselabs.com/12081064-8878-477e-8092-564a240c69e2/participant.json" }
                                     ],
                                     "gx:license": [ "http://smartproof.in/.well-known/license"
                             ],     
                               "gx:aggregationOf": [{"id":"https://wizard-api.smart-x.smartsenselabs.com/12081064-8878-477e-8092-564a240c69e2/resource_b5b7e6b0-ae24-4458-b3f9-27572abc39e7.json"}]
                             
                                   }
                                                        
                                                        }
                            """),
                    @ExampleObject(name = "Create virtual resource , Data resource", value = """
                               {
                                 "participantJsonUrl": "https://example.com/12081064-8878-477e-8092-564a240c69e2/participant.json",
                                 "verificationMethod": "did:web:example.com",
                                 "privateKey": "-----BEGIN PRIVATE KEY---  ----END PRIVATE KEY-----",
                                 "credentialSubject": {
                                   "type": "VirtualResource",
                                   "subType": "VirtualDataResource",
                                   "gx:name": "Test Resource 1",
                                   "gx:description": "Test Resource 1 description",
                                   "gx:copyrightOwnedBy": [
                                     {
                                       "id": "https://wizard.lab.gaia-x.eu/api/credentials/2d37wbGvQzbAQ84yRouh2m2vBKkN8s5AfH9Q75HZRCUQmJW7yAVSNKzjJj6gcjE2mDNDUHCichXWdMH3S2c8AaDLm3kXmf5R8DFPWTYo5iRYkn8kvgU3AjMXc2qTbhuMHCpucKGgT1ZMkcHUygZkt11iD3T8VJNKYwsdk4MGoZwdqoFUuTKVcsXVTBA4ofD1Dtqzjavyng5WUpvJf4gRyfGkMvYYuHCgay8TK8Dayt6Rhcs3r2d1gRCg2UV419S9CpWZGwKQNEXdYbaB2eTiNbQ83KMd4mj1oSJgF7LLDZLJtKJbhwLzR3x35QUqEGevRxnRDKoPdHrEZN7r9TVAmvr9rt7Xq8eB4zGMTza59hisEAUaHsmWQNaVDorqFyZgN5bXswMK1irVQ5SVR9osCCRrKUKkntxfakjmSqapPfveMP39vkgTXfEhsfLUZXGwFcpgLpWxWRn1QLnJY11BVymS7DyaSvbSKotNFQxyV6vghfM2Jetw1mLxU5qsQqDYnDYJjPZQSmkwxjX3yenPVCz6N2ox83tj9AuuQrzg5p2iukNdunDd2QCsHaMEtTq9JVLzXtWs2eZbPkxCBEQwoKTGGVhKu5yxZjCtQGc#9894e9b0a38aa105b50bb9f4e7d0975641273416e70f166f4bd9fd1b00dfe81d"
                                     }
                                   ],
                                   "gx:license": [
                                     "http://smartproof.in/.well-known/license"
                                   ],
                                   "gx:producedBy": {
                                     "id": "https://wizard.lab.gaia-x.eu/api/credentials/2d37wbGvQzbAQ84yRouh2m2vBKkN8s5AfH9Q75HZRCUQmJW7yAVSNKzjJj6gcjE2mDNDUHCic"
                                   },
                                   "gx:exposedThrough": [
                                     "http://smartproof.in/api/test-resource-1"
                                   ],
                                   "gx:containsPII": false
                                 }
                               }                   
                            """)
            })
    })
    @Operation(summary = "Create Resource")
    @PostMapping(path = "public/resource", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<Resource> createPublicResource(@Valid @RequestBody CreateResourceRequest request) throws JsonProcessingException {
        return CommonResponse.of(this.resourceService.createResource(request, null));
    }

    @Tag(name = "Resources")
    @Operation(summary = "Get service list for dropdown")
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
                                             "id": "1b155537-950e-4227-85ce-dfdfc8bf949f",
                                             "name": "Test Resource 1",
                                             "vcUrl": "https://wizard-api.smart-x.smartsenselabs.com/12081064-8878-477e-8092-564a240c69e2/resource_4e37e259-eb8c-4c80-aa01-55dbd7974339.json"
                                           },
                                           {
                                             "id": "26b8f13d-fa14-47a8-a471-412c7f69349a",
                                             "name": "Test Resource 1",
                                             "vcUrl": "https://wizard-api.smart-x.smartsenselabs.com/12081064-8878-477e-8092-564a240c69e2/resource_05a3e56b-8837-4cc8-9eff-cc55271435f5.json"
                                           },
                                           {
                                             "id": "00ef1b08-fe08-4692-ab50-3dbec8304a90",
                                             "name": "Data Center",
                                             "vcUrl": "https://wizard-api.smart-x.smartsenselabs.com/12081064-8878-477e-8092-564a240c69e2/resource_19c2bf19-dffe-486e-99bf-bec2301eb061.json"
                                           },
                                           {
                                             "id": "9262f62f-fd5e-44ee-aa9a-82dbbccf19da",
                                             "name": "Data Center 2",
                                             "vcUrl": "https://wizard-api.smart-x.smartsenselabs.com/12081064-8878-477e-8092-564a240c69e2/resource_f1b1d420-d44d-439a-8de3-1aeb58f9a493.json"
                                           },
                                           {
                                             "id": "e385f17e-baf3-4949-a8e0-4646279432f3",
                                             "name": "Test Resource 1",
                                             "vcUrl": "https://wizard-api.smart-x.smartsenselabs.com/12081064-8878-477e-8092-564a240c69e2/resource_f288e078-c580-4cfe-9c25-2013174c4324.json"
                                           }
                                         ],
                                         "pageable": {
                                           "pageSize": 5,
                                           "totalPages": 4,
                                           "pageNumber": 0,
                                           "numberOfElements": 5,
                                           "totalElements": 19
                                         }
                                       }
                                    }"""
                            )
                    })
            }),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = {
                    @Content(examples = {})
            }),
    })
    @PostMapping(path = RESOURCE_LIST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<PageResponse<ServiceAndResourceListDTO>> getServiceOfferingLList(@Valid @RequestBody FilterRequest filterRequest) {
        return CommonResponse.of(this.resourceService.getResourceList(filterRequest));
    }

}
