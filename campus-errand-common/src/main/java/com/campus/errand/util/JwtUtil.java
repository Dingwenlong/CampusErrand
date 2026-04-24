package com.campus.errand.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${app.jwt.secret:campus_errand_secret_key_2024}")
    private String secret;

    @Value("${app.jwt.expiration-ms:604800000}")
    private long expiration;

    @Value("${app.jwt.issuer:campus-errand-system}")
    private String issuer;

    public String generateToken(Long userId, String openid) {
        return generateToken(userId, openid, "user");
    }

    public String generateToken(Long userId, String openid, String role) {
        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + expiration);

        Map<String, Object> header = new HashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        return JWT.create()
                .withHeader(header)
                .withIssuer(issuer)
                .withIssuedAt(issuedAt)
                .withExpiresAt(expiresAt)
                .withClaim("userId", userId)
                .withClaim("openid", openid)
                .withClaim("role", role)
                .sign(Algorithm.HMAC256(secret));
    }

    public DecodedJWT verifyToken(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withIssuer(issuer)
                .build();
        return verifier.verify(token);
    }

    public Long getUserId(String token) {
        try {
            DecodedJWT jwt = verifyToken(token);
            return jwt.getClaim("userId").asLong();
        } catch (JWTVerificationException e) {
            log.error("Token解析失败: {}", e.getMessage());
            return null;
        }
    }

    public String getOpenid(String token) {
        try {
            DecodedJWT jwt = verifyToken(token);
            return jwt.getClaim("openid").asString();
        } catch (JWTVerificationException e) {
            log.error("Token解析失败: {}", e.getMessage());
            return null;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            DecodedJWT jwt = verifyToken(token);
            return jwt.getExpiresAt().before(new Date());
        } catch (JWTVerificationException e) {
            return true;
        }
    }

    public Long getUserIdFromToken(String token) {
        return getUserId(token);
    }

    public String getRole(String token) {
        try {
            DecodedJWT jwt = verifyToken(token);
            String role = jwt.getClaim("role").asString();
            return role == null ? "user" : role;
        } catch (JWTVerificationException e) {
            log.error("Token解析失败: {}", e.getMessage());
            return null;
        }
    }
}
