package eu.gaiax.wizard.controller;

import com.smartsensesolutions.java.commons.FilterRequest;
import com.smartsensesolutions.java.commons.base.service.BaseService;
import eu.gaiax.wizard.api.model.CommonResponse;
import eu.gaiax.wizard.api.model.LabelLevelTypeInterface;
import eu.gaiax.wizard.api.model.PageResponse;
import eu.gaiax.wizard.core.service.data_master.LabelLevelService;
import eu.gaiax.wizard.core.service.data_master.MasterDataServiceFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static eu.gaiax.wizard.utils.WizardRestConstant.LABEL_LEVEL_QUESTIONS;
import static eu.gaiax.wizard.utils.WizardRestConstant.MASTER_DATA_FILTER;


@RestController
@RequiredArgsConstructor
@Tag(name = "master-data", description = "APIs to access master data for dropdowns in forms")
public class DataMasterController extends BaseController {

    private final MasterDataServiceFactory masterDataServiceFactory;

    private final LabelLevelService labelLevelService;

    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(examples = {
                    @ExampleObject(name = "Filter request with sort and without search", value = """
                            {
                              "page": 0,
                              "size": 10,
                              "sort": {
                                "column": "type",
                                "sortType": "ASC"
                              }
                            }"""
                    ),
                    @ExampleObject(name = "Filter request with sort and search", value = """
                            {
                              "page": 0,
                              "size": 5,
                              "sort": {
                                "column": "type",
                                "sortType": "ASC"
                              },
                              "criteriaOperator": "AND",
                              "criteria": [
                                {
                                  "column": "type",
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
                                             "id": "0c9b529a-94ef-4ea7-8aa8-ed51780b16ba",
                                             "type": "application/1d-interleaved-parityfec",
                                             "active": true
                                           },
                                           {
                                             "id": "4821bd62-491f-4efc-b95a-08410a4ddbab",
                                             "type": "application/3gpdash-qoe-report+xml",
                                             "active": true
                                           },
                                           {
                                             "id": "736df04f-2fcd-41e9-9a69-a4811ac194d7",
                                             "type": "application/3gppHalForms+json",
                                             "active": true
                                           },
                                           {
                                             "id": "3b64f941-29e7-402c-b871-82ce5b6d05c2",
                                             "type": "application/3gppHal+json",
                                             "active": true
                                           },
                                           {
                                             "id": "961b359b-47b1-4524-8fdc-13c83ac11baa",
                                             "type": "application/3gpp-ims+xml",
                                             "active": true
                                           }
                                         ],
                                         "pageable": {
                                           "pageSize": 5,
                                           "totalPages": 40,
                                           "pageNumber": 0,
                                           "numberOfElements": 5,
                                           "totalElements": 200,
                                           "sort": {
                                             "column": "type",
                                             "sortType": "ASC"
                                           }
                                         }
                                       }
                                    }"""
                            )
                    })
            }),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = {
                    @Content(examples = {
                            @ExampleObject(name = "Invalid data type", value = """
                                    {
                                      "message": "Master data not found with name 'entities'",
                                      "status": 400,
                                      "payload": {
                                        "error": {
                                          "message": "Master data not found with name 'entities'",
                                          "status": 400,
                                          "timeStamp": 1692356630682
                                        }
                                      }
                                    }"""
                            )
                    })
            }),
    })
    @Operation(
            summary = "Filter API to get master data",
            description = "This endpoint used to fetch master data for participant, service and resource forms."
    )
    @PostMapping(MASTER_DATA_FILTER)
    public CommonResponse<PageResponse> filterTypeMaster(
            @PathVariable(name = "dataType") @Parameter(description = "[access, entity, format, registration, request, standard, subdivision]") String dataType,
            @Valid @RequestBody FilterRequest filterRequest) {
        BaseService service = this.masterDataServiceFactory.getInstance(dataType);
        Page page = service.filter(filterRequest);
        return CommonResponse.of(PageResponse.of(page, filterRequest.getSort()));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Label level questions fetched successfully.", content = {
                    @Content(examples = {
                            @ExampleObject(name = "Successful request", value = """
                                    {
                                        "status": 200,
                                        "payload": [
                                          {
                                            "labelLevelQuestionMasterList": [
                                              {
                                                "question": "The Provider shall clearly identify for which parties the legal act is binding.",
                                                "criterionNumber": "Criterion P1.1.3",
                                                "id": "0bbca81f-582d-417a-aa4a-631b758edc47"
                                              },
                                              {
                                                "question": "The Provider shall ensure that the legally binding act covers the entire provision of the Service Offering",
                                                "criterionNumber": "Criterion P1.1.4",
                                                "id": "10df3918-0cb7-4324-a74c-81b3e979b918"
                                              },
                                              {
                                                "question": "The Provider shall clearly identity for each legally binding act its governing law.",
                                                "criterionNumber": "Criterion P1.1.5",
                                                "id": "1cfc68df-2309-4395-ab40-aec0b2823506"
                                              }
                                            ],
                                            "name": "Contractual governance",
                                            "id": "03965163-30cc-439c-beb1-dbf567b9bf10"
                                          }
                                        ]
                                    }"""
                            )
                    })
            })
    })
    @Operation(
            summary = "API to get label level questions",
            description = "This endpoint used to fetch the label level types and questions."
    )
    @GetMapping(LABEL_LEVEL_QUESTIONS)
    public CommonResponse<List<LabelLevelTypeInterface>> getLabelLevelQuestions() {
        return CommonResponse.of(this.labelLevelService.getLabelLevelTypeAndQuestionList());
    }
}
