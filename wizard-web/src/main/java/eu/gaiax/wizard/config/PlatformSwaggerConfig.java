/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The type Platform swagger config.
 */
@Configuration
public class PlatformSwaggerConfig {

    /**
     * Spring identity open api.
     *
     * @return the open api
     */
    @Bean
    public OpenAPI springIdentityOpenAPI() {
        String authorization = "Authorization";
        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(new SecurityRequirement().addList(authorization))
                .components(new Components().addSecuritySchemes(authorization,
                        new SecurityScheme().name(authorization)
                                .type(SecurityScheme.Type.HTTP).scheme("Bearer")));
    }

    private Info apiInfo() {
        return new Info()
                .title("The Smart-X API Documentation")
                .description("This API documentation contains all the APIs for The Smart-X")
                .version("1.0.0")
                .contact(new Contact()
                        .name("The Smart-X")
                        .email("admin@smartsensesolutions.com")
                        .url("https://gaiaxapi.proofsense.io/")
                );
    }
}
