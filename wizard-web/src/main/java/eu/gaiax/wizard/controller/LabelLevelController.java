package eu.gaiax.wizard.controller;

import eu.gaiax.wizard.api.model.CommonResponse;
import eu.gaiax.wizard.api.model.service_offer.LabelLevelFileUpload;
import eu.gaiax.wizard.api.model.service_offer.LabelLevelRequest;
import eu.gaiax.wizard.core.service.service_offer.ServiceLabelLevelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static eu.gaiax.wizard.utils.WizardRestConstant.LABEL_LEVEL;
import static eu.gaiax.wizard.utils.WizardRestConstant.LABEL_LEVEL_FILE_UPLOAD;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequiredArgsConstructor
public class LabelLevelController extends BaseController {
    private final ServiceLabelLevelService labelLevelService;

    @Operation(summary = "Create Label-Level VC")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(examples = {
                    @ExampleObject(name = "Create label level vc", value = """
                            {
                              "gx:criteria": {
                                "1.1.1": {
                                  "evidence": {
                                    "pdf": {
                                      "buffer": {},
                                      "Modified": "2023-08-17T07:01:54.095Z",
                                      "name": "child.html",
                                      "type": "text/html",
                                      "size": 896,
                                      "fileExtension": "html"
                                    },
                                    "vc": {
                                      "buffer": {},
                                      "Modified": "2023-08-17T07:09:50.439Z",
                                      "name": "index.html",
                                      "type": "text/html",
                                      "size": 53871,
                                      "fileExtension": "html"
                                    },
                                    "website": "https://jsonblob.com/1134052383935815680"
                                  },
                                  "response": " Deny ",
                                  "reason": "1.1.1 Reasoning"
                                }
                              }
                            }""")
            })
    })
    @PostMapping(path = LABEL_LEVEL, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<Object> createLabelLevelVc(@Valid @RequestBody LabelLevelRequest labelLevelRequest) {
        return CommonResponse.of(this.labelLevelService.createLabelLevelVc(labelLevelRequest, null, null));
    }

    @PostMapping(path = LABEL_LEVEL_FILE_UPLOAD, consumes = MULTIPART_FORM_DATA_VALUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<Object> uploadLabelLevelFile(@Valid @ModelAttribute LabelLevelFileUpload labelLevelFileUpload) throws IOException {
        return CommonResponse.of(this.labelLevelService.uploadLabelLevelFile(labelLevelFileUpload));
    }
}
