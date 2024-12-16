package com.plaything.api.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.plaything.api.common.constants.Constants;
import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;

@Slf4j
@Component
public class JWTProvider {

    private static String secretKey;
    private static String refreshSecretKey;
    private static long tokenTimeForMinutes;
    private static long refreshTokenTimeForMinutes;

    @Value("${token.secret-key}")
    public void setSecretKey(String secretKey) {
        JWTProvider.secretKey = secretKey;
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

    public static String createToken(String name) {
        return JWT.create()
                .withSubject(name)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + tokenTimeForMinutes * Constants.ON_MINUTE_TO_MILLIS))
                .sign(Algorithm.HMAC256(secretKey));

    }

    public static String createRefreshToken(String name) {

        return JWT.create()
                .withSubject(name)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + refreshTokenTimeForMinutes * Constants.ON_MINUTE_TO_MILLIS))
                .sign(Algorithm.HMAC256(refreshSecretKey));
    }

    public static DecodedJWT checkTokenForRefresh(String token) {

        //토큰 값이 만료돼야만 정상 작동하는 API
        try {
            DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(secretKey)).build().verify(token);
            log.error("token must be expired: {}", decodedJWT.getSubject());
            throw new CustomException(ErrorCode.ACCESS_TOKEN_IS_NOT_EXPIRED);
        } catch (AlgorithmMismatchException | SignatureVerificationException | InvalidClaimException e) {
            throw new CustomException(ErrorCode.TOKEN_IS_INVALID);
        } catch (TokenExpiredException e) {
            return JWT.decode(token);
        }
    }

    public static DecodedJWT decodeAccessToke(String token) {
        return decodeTokenAfterVerify(token, secretKey);
    }

    public static DecodedJWT decodeRefreshToke(String token) {
        return decodeTokenAfterVerify(token, refreshSecretKey);
    }

    private static DecodedJWT decodeTokenAfterVerify(String token, String key) {

        //정상적으로 verify
        try {
            return JWT.require(Algorithm.HMAC256(key)).build().verify(token);
        } catch (AlgorithmMismatchException | SignatureVerificationException | InvalidClaimException e) {
            throw new CustomException(ErrorCode.TOKEN_IS_INVALID);
        } catch (TokenExpiredException e) {
            throw new CustomException(ErrorCode.TOKEN_IS_EXPIRED);
        }

    }

    public static DecodedJWT decodedJWT(String token) {
        return JWT.decode(token);
    }

    public static String extractToken(String header) {
        if (header == null) {
            throw new CustomException(ErrorCode.TOKEN_IS_INVALID);
        }

        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        } else {
            throw new IllegalArgumentException("Invalid Auth Header");
        }
    }

    public static String getUserFromToken(String token) {
        DecodedJWT jwt = decodedJWT(token);
        return jwt.getSubject();
    }
}
