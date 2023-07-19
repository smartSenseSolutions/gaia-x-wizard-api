package eu.gaiax.wizard.config.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter;
    private final String resourceId;

    public CustomAuthenticationConverter(String resourceId) {
        this.resourceId = resourceId;
        this.grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        Collection<GrantedAuthority> authorities = (this.grantedAuthoritiesConverter.convert(source))
                .stream()
                .collect(Collectors.toSet());
        authorities.addAll(this.extractResourceRoles(source, this.resourceId));
        this.extractResourceRoles(source, this.resourceId);
        return new JwtAuthenticationToken(source, authorities);
    }

    @SuppressWarnings("unchecked")
    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt, String resourceId) {
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        Map<String, Object> resource = (Map<String, Object>) resourceAccess.get(resourceId);
        if (Objects.isNull(resource)) {
            return Set.of();
        }
        Collection<String> resourceRoles = (Collection<String>) resource.get("roles");
        if (Objects.isNull(resourceRoles)) {
            return Set.of();
        }
        return resourceRoles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());
    }
}
