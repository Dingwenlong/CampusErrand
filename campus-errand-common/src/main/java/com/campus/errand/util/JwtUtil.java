package com.campus.errand.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JwtUtil {

    private static final String SECRET = "campus_errand_secret_key_2024";
    private static final long EXPIRATION = 86400000 * 7; // 7天
    private static final String ISSUER = "campus-errand-system";

    public static String generateToken(Long userId, String openid) {
        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + EXPIRATION);

        Map<String, Object> header = new HashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        return JWT.create()
                .withHeader(header)
                .withIssuer(ISSUER)
                .withIssuedAt(issuedAt)
                .withExpiresAt(expiresAt)
                .withClaim("userId", userId)
                .withClaim("openid", openid)
                .sign(Algorithm.HMAC256(SECRET));
    }

    public static DecodedJWT verifyToken(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET))
                .withIssuer(ISSUER)
                .build();
        return verifier.verify(token);
    }

    public static Long getUserId(String token) {
        try {
            DecodedJWT jwt = verifyToken(token);
            return jwt.getClaim("userId").asLong();
        } catch (JWTVerificationException e) {
            log.error("Token解析失败: {}", e.getMessage());
            return null;
        }
    }

    public static String getOpenid(String token) {
        try {
            DecodedJWT jwt = verifyToken(token);
            return jwt.getClaim("openid").asString();
        } catch (JWTVerificationException e) {
            log.error("Token解析失败: {}", e.getMessage());
            return null;
        }
    }

    public static boolean isTokenExpired(String token) {
        try {
            DecodedJWT jwt = verifyToken(token);
            return jwt.getExpiresAt().before(new Date());
        } catch (JWTVerificationException e) {
            return true;
        }
    }
}
