/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.id.utils;

/**
 *
 * @author Admin
 */
import java.util.Date;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.elcom.metacen.id.auth.jwt.TokenParse;
import com.elcom.metacen.id.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class JWTutils {

    @Autowired
    private TokenService tokenService;

    private static final Logger LOGGER = LoggerFactory.getLogger(JWTutils.class);
    
    private final static String SECRET_KEY = "elcom_wq3Dr8O5wrkCSybDkQ==1_2020@)@)";
    private static final long REFRESH_JWT_EXPIRATION = 2592000000L;//30day
//    private static final long REFRESH_JWT_EXPIRATION = 60000L;

    public static String createToken(String content) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + REFRESH_JWT_EXPIRATION);
            return JWT.create()
                    .withIssuer("auth0")
                    .withClaim("content", content)
                    .withExpiresAt(expiryDate)
                    .sign(Algorithm.HMAC256(SECRET_KEY));
        } catch (Exception ex) {
            LOGGER.info(ex.toString());
        }
        return null;
    }

    public static String createToken(String iss, String sub, String aud, String claimName, String claimValue, String secretKey, Date exp) {
        try {
            return JWT.create()
                    .withIssuer(iss) // iss
                    .withExpiresAt(exp) // exp
                    .withSubject(sub) // sub
                    .withAudience(aud) //aud
                    .withClaim(claimName, claimValue) //room	
                    .sign(Algorithm.HMAC256(secretKey));
        } catch (Exception ex) {
            LOGGER.info(ex.toString());
        }
        return null;
    }

    public TokenParse getContentInToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET_KEY))
                    .withIssuer("auth0")
                    .build();
            DecodedJWT jwt = verifier.verify(token);
            try {
                JWTVerifier verifier1 = JWT.require(Algorithm.HMAC256(SECRET_KEY))
                        .withIssuer("auth0")
                        .build();

                String refreshTokenCache = tokenService.getRefreshTokenUpdate(jwt.getClaim("content").asString());
                if(refreshTokenCache!=null){
                    DecodedJWT jwtCache = verifier.verify(refreshTokenCache);
                    long timeRefreshToken = jwt.getExpiresAt().getTime();
                    long timeRefreshTokenCache = jwtCache.getExpiresAt().getTime();
                    if(timeRefreshToken<timeRefreshTokenCache){
                        LOGGER.error("Expired JWT refreshToken(Password just changed)");
                        return new TokenParse(425,"Expired JWT refreshToken(Password just changed)",null);
                    }else {
                        return new TokenParse(HttpStatus.OK.value(), HttpStatus.OK.toString(), jwt.getClaim("content").asString());
                    }

                }else
                    return new TokenParse(HttpStatus.OK.value(), HttpStatus.OK.toString(),jwt.getClaim("content").asString());
            } catch (TokenExpiredException ex) {
                LOGGER.error("RefreshTokenCache Expired");
                tokenService.removeRefreshTokenUpdate(jwt.getClaim("content").asString());
                return new TokenParse(421,"RefreshTokenToken Expired ",null);
            } catch ( Exception ex) {

                return new TokenParse(HttpStatus.OK.value(), HttpStatus.OK.toString(),jwt.getClaim("content").asString());
            }

        }  catch ( SignatureVerificationException ex) {
            LOGGER.error("Invalid RefreshTokenToken");
            return new TokenParse(420,"Invalid RefreshTokenToken",null);
        } catch (TokenExpiredException ex) {
            LOGGER.error("RefreshTokenToken Expired");
            return new TokenParse(421,"RefreshTokenToken Expired ",null);
        } catch (JWTVerificationException ex) {
            LOGGER.error("JWT claims string is empty.");
            return new TokenParse(423,"claims string is empty",null);
        }
    }
    public Boolean checkExpiredToken(String uuid){
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET_KEY))
                    .withIssuer("auth0")
                    .build();
            return false;
        }catch (Exception ex){
            return true;
        }
    }
    public static String getToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET_KEY))
                    .withIssuer("auth0")
                    .build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim("content").asString();
        } catch (Exception ex) {
            LOGGER.info(ex.toString());
        }
        return null;
    }
}