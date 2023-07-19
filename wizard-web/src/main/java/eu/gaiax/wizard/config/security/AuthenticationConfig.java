package eu.gaiax.wizard.config.security;

import eu.gaiax.wizard.config.security.model.SecurityConfigProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static eu.gaiax.wizard.utils.RoleConstant.ADMIN_ROLE;
import static eu.gaiax.wizard.utils.RoleConstant.ENTERPRISE_ROLE;
import static eu.gaiax.wizard.utils.WizardRestConstant.*;
import static org.springframework.http.HttpMethod.*;

@Slf4j
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@Configuration
@RequiredArgsConstructor
public class AuthenticationConfig {
    private final SecurityConfigProperties configProperties;
    
    @Bean
    @ConditionalOnProperty(value = "wizard.security.enabled", havingValue = "true", matchIfMissing = true)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.cors(Customizer.withDefaults())
                .csrf(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authRequest -> {
                    authRequest.requestMatchers("/actuator/health").permitAll();
                    authRequest.requestMatchers("/webjars/**").permitAll();
                    authRequest.requestMatchers("/", "/docs/api-docs/**", "/ui/swagger-ui/**",
                            "/actuator/health/**", "/error").permitAll();
                    authRequest.requestMatchers("/login").permitAll();
                    authRequest.requestMatchers("/register").permitAll();
                    authRequest.requestMatchers("/ingress/**").permitAll();
                    authRequest.requestMatchers("/did/**").permitAll();
                    authRequest.requestMatchers("/certificate/**").permitAll();
                    authRequest.requestMatchers("/.well-known/**").permitAll();
                    authRequest.requestMatchers(CREATE_PARTICIPANT_JSON).permitAll();
                    authRequest.requestMatchers(CREATE_SUBDOMAIN).permitAll();
                    authRequest.requestMatchers(ENTERPRISE_LIST).hasRole(ADMIN_ROLE);
                    authRequest.requestMatchers(ENTERPRISE_BY_ID).hasRole(ADMIN_ROLE);
                    authRequest.requestMatchers(ENTERPRISE).hasRole(ENTERPRISE_ROLE);
                    authRequest.requestMatchers(ENTERPRISE + "/**").hasRole(ENTERPRISE_ROLE);
                    authRequest.requestMatchers(CATALOGUE).hasRole(ENTERPRISE_ROLE);
                })
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(new CustomAuthenticationConverter(this.configProperties.clientId()))))
                .build();
    }

    @Bean
    @ConditionalOnProperty(value = "wizard.security.enabled", havingValue = "false")
    public WebSecurityCustomizer securityCustomizer() {
        log.warn("AuthenticationConfig(securityCustomizer) : Disable security -> This is not recommended to use in production environments.");
        return web -> web.ignoring()
                .requestMatchers(new AntPathRequestMatcher("**"));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(this.configProperties.corsOrigins());
        configuration.setAllowedMethods(List.of(HEAD.name(), OPTIONS.name(), GET.name(), POST.name(), PUT.name(), DELETE.name()));
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}


