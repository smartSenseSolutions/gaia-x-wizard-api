/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.utils;

import eu.gaiax.wizard.api.model.SessionDTO;
import eu.gaiax.wizard.api.model.StringPool;
import eu.gaiax.wizard.api.model.setting.JWTSetting;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * The type Jwt util.
 */
@Component
public class JWTUtil {


    /**
     * Authorization header required in the request
     */
    private static final String BEARER = "Bearer ";

    private final JWTSetting jwtSetting;

    /**
     * Instantiates a new Jwt util.
     *
     * @param jwtSetting the jwt setting
     */
    @Autowired
    public JWTUtil(JWTSetting jwtSetting) {
        this.jwtSetting = jwtSetting;
    }

    /**
     * Get all the claims from the given token
     *
     * @param token access token
     * @return claims all claims from token
     */
    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(jwtSetting.getTokenSigningKey()).parseClaimsJws(token).getBody();
    }

    /**
     * Generate token string.
     *
     * @param sessionDTO the session dto
     * @return the string
     */
    public String generateToken(SessionDTO sessionDTO) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(StringPool.EMAIL, sessionDTO.getEmail());
        claims.put(StringPool.ENTERPRISE_ID, sessionDTO.getEnterpriseId());
        claims.put(StringPool.ROLE, sessionDTO.getRole());

        return doGenerateToken(claims, sessionDTO.getEmail());
    }

    /**
     * Generates token for the given claims, username and expiry date
     *
     * @param claims   claims
     * @param username username/email
     * @return access token
     */
    public String doGenerateToken(Map<String, Object> claims, String username) {
        return Jwts.builder()
                .setClaims(claims)
                .setAudience("Smart-X")
                .setIssuer("Smart-X")
                .setNotBefore(new Date())
                .setSubject(username)
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS512, jwtSetting.getTokenSigningKey())
                .compact();
    }

    /**
     * Extracts token from the authorization header
     *
     * @param authorizationHeader authorization header
     * @return access token
     */
    public String extractToken(String authorizationHeader) {
        if (!authorizationHeader.startsWith(BEARER) || authorizationHeader.length() < BEARER.length()) {
            throw new MalformedJwtException("JWT cannot be empty");
        }
        return authorizationHeader.substring(BEARER.length());
    }
}
