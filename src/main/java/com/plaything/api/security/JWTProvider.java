package com.plaything.api.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class JWTProvider {

    private static SecretKey secretKey;
    private final ObjectMapper objectMapper;
    private static String refreshSecretKey;
    private static long tokenTimeForMinutes;
    private static long refreshTokenTimeForMinutes;

    @Value("${token.secret-key}")
    public void setSecretKey(String secretKey) {
        JWTProvider.secretKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    @Value("${token.refresh_secret_key}")
    public void setRefreshSecretKey(String refreshSecretKey) {
        JWTProvider.refreshSecretKey = refreshSecretKey;
    }

    @Value("${token.token-time}")
    public void setTokenTimeForMinutes(long tokenTimeForMinutes) {
        JWTProvider.tokenTimeForMinutes = tokenTimeForMinutes;
    }

    @Value("${token.refresh-token-time}")
    public void setRefreshTokenTimeForMinutes(long refreshTokenTimeForMinutes) {
        JWTProvider.refreshTokenTimeForMinutes = refreshTokenTimeForMinutes;
    }

    public String createToken(String name, String role, Long expiredMs) {

        return Jwts.builder()
                .claim("username", name)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    public String extractToken(String header) {
        if (header == null) {
            throw new CustomException(ErrorCode.TOKEN_IS_INVALID);
        }

        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        } else {
            throw new CustomException(ErrorCode.TOKEN_IS_INVALID);
        }
    }

    public String getUsername(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("username", String.class);
    }

    public String getRole(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    public Boolean isExpired(String token) {
        try {

            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration()
                    .before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    public Map<String, String> parseHeaders(String token) throws JsonProcessingException {
        // JWT의 첫 번째 부분(헤더)을 가져옴
        String header = token.split("\\.")[0];

        // Base64 디코딩 후 JSON을 Map으로 변환
        return objectMapper.readValue(decodeHeader(header), Map.class);
    }

    public String decodeHeader(String token) {
        return new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
    }


    public Claims getTokenClaims(String token, PublicKey publicKey) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

//    public String createRefreshToken(String name) {
//
//        return JWT.create()
//                .withSubject(name)
//                .withIssuedAt(new Date())
//                .withExpiresAt(new Date(System.currentTimeMillis() + refreshTokenTimeForMinutes * Constants.ON_MINUTE_TO_MILLIS))
//                .sign(Algorithm.HMAC256(refreshSecretKey));
//    }
//
//    public static DecodedJWT checkTokenForRefresh(String token) {
//
//        //토큰 값이 만료돼야만 정상 작동하는 API
//        try {
//            DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(secretKey)).build().verify(token);
//            log.error("token must be expired: {}", decodedJWT.getSubject());
//            throw new CustomException(ErrorCode.ACCESS_TOKEN_IS_NOT_EXPIRED);
//        } catch (AlgorithmMismatchException | SignatureVerificationException | InvalidClaimException e) {
//            throw new CustomException(ErrorCode.TOKEN_IS_INVALID);
//        } catch (TokenExpiredException e) {
//            return JWT.decode(token);
//        }
//    }
//
//    public static DecodedJWT decodeAccessToke(String token) {
//        return decodeTokenAfterVerify(token, secretKey);
//    }
//
//    public static DecodedJWT decodeRefreshToke(String token) {
//        return decodeTokenAfterVerify(token, refreshSecretKey);
//    }
//
//    private static DecodedJWT decodeTokenAfterVerify(String token, String key) {
//
//        //정상적으로 verify
//        try {
//            return JWT.require(Algorithm.HMAC256(key)).build().verify(token);
//        } catch (AlgorithmMismatchException | SignatureVerificationException | InvalidClaimException e) {
//            throw new CustomException(ErrorCode.TOKEN_IS_INVALID);
//        } catch (TokenExpiredException e) {
//            throw new CustomException(ErrorCode.TOKEN_IS_EXPIRED);
//        }
//
//    }
//
//    public static DecodedJWT decodedJWT(String token) {
//        return JWT.decode(token);
//    }

//
//    public Boolean isExpired(String token) {
//
//        return JWT.decode()
//
//
//                verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
//    }
//
//    public static String getUserFromToken(String token) {
//        DecodedJWT jwt = decodeTokenAfterVerify(token, secretKey);
//        return jwt.getSubject();
//    }
}
