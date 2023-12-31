package eu.gaiax.wizard.config.swagger;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@AllArgsConstructor
@Slf4j
public class LandingPageConfig implements WebMvcConfigurer {

    private final SwaggerUiConfigProperties properties;

    /**
     * Method will use SwaggerUiConfigProperties and redirect user to swagger-ui page.
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        String redirectUri = this.properties.getPath();
        log.info("Set landing page to path {}", redirectUri);
        registry.addRedirectViewController("/", redirectUri);
    }
}
