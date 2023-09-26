/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.config.swagger;

import eu.gaiax.wizard.config.security.model.SecurityConfigProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

/**
 * The type Platform swagger config.
 */
@Configuration
@RequiredArgsConstructor
public class PlatformSwaggerConfig {
    private final SecurityConfigProperties properties;
    private final ApplicationInfo applicationInfo;

    /**
     * Spring identity open api.
     *
     * @return the open api
     */
    @Bean
    public OpenAPI springIdentityOpenAPI() {
        Info info = this.apiInfo();
        OpenAPI openAPI = new OpenAPI();
        if (Boolean.TRUE.equals(this.properties.enabled())) {
            openAPI = this.enableSecurity(openAPI);
        }
        return openAPI.info(info);
    }

    private Info apiInfo() {
        return new Info()
                .title(this.applicationInfo.name())
                .description(this.applicationInfo.description())
                .version(this.applicationInfo.version())
                .contact(new Contact()
                        .name(this.applicationInfo.contact().name())
                        .email(this.applicationInfo.contact().email())
                        .url(this.applicationInfo.contact().url())
                );
    }

    private OpenAPI enableSecurity(OpenAPI openAPI) {
        Components components = new Components();
        components.addSecuritySchemes(
                "gaia-x-open-api",
                new SecurityScheme()
                        .type(SecurityScheme.Type.OAUTH2)
                        .flows(new OAuthFlows()
                                .authorizationCode(new OAuthFlow()
                                        .authorizationUrl(this.properties.authUrl())
                                        .tokenUrl(this.properties.tokenUrl())
                                        .refreshUrl(this.properties.refreshTokenUrl()
                                        )
                                )
                        )
        );

        return openAPI.components(components)
                .addSecurityItem(new SecurityRequirement().addList("gaia-x-open-api", Collections.emptyList()));
    }
}
