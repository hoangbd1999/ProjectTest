/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.id.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.elcom.metacen.id.model.dto.TokenCache;
import com.elcom.metacen.id.repository.RedisRepository;
import com.elcom.metacen.id.service.TokenService;
import com.elcom.metacen.id.utils.JWTutils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 *
 * @author Admin
 */
@Service
@Transactional
@EnableAsync
public class TokenServiceImpl implements TokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenServiceImpl.class);

    private final String JWT_SECRET = "elcom@123_2020";

    private final static String SECRET_KEY = "elcom_wq3Dr8O5wrkCSybDkQ==1_2020@)@)";

    private final long ACCESS_JWT_EXPIRATION = 604800000L;//7 day

    @Autowired
    private RedisRepository redisRepository;

    @Autowired
    private CacheManager cacheManager;

    @Override
    @Cacheable(value = "BearerToken")
    public String getAccessToken() {
     //   LOGGER.info("BearerToken missed => Get New Token");
     //   MetaCENCoreLoginRequest loginRequest = new MetaCENCoreLoginRequest();
      //  loginRequest.setUsername(ApplicationConfig.ITS_USERNAME);
      //  loginRequest.setPassword(ApplicationConfig.ITS_PASSWORD);

    //    HttpHeaders headers = new HttpHeaders();
     //   headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
     //   headers.setContentType(MediaType.APPLICATION_JSON);
      //  RestTemplate restTemplate = new RestTemplate();
        // Dữ liệu đính kèm theo yêu cầu.
     //   HttpEntity<MetaCENCoreLoginRequest> requestBody = new HttpEntity<>(loginRequest, headers);
        // Gửi yêu cầu với phương thức POST.
//        MetaCENCoreLoginResponse respsonse = restTemplate.postForObject(ApplicationConfig.ITS_AUTHEN_URL,
//                requestBody, MetaCENCoreLoginResponse.class);
//        if (respsonse != null) {
//            return respsonse.getData().getTokenType() + " " + respsonse.getData().getAccessToken();
//        } else {
//            return null;
//        }
        return null;
    }

    @Override
    public boolean removeAccessToken() {
        cacheManager.getCache("BearerToken").clear();
        return true;
    }

    @Override
    //@CachePut(value = "BearerUpdateToken",key = "#uuid")
    public String setAccessTokenUpdate(String uuid) {
        LOGGER.info("lưu vào cache accessToken");
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ACCESS_JWT_EXPIRATION);
        // Tạo chuỗi json web token từ id của user.
        String accessToken = Jwts.builder()
                .setSubject(uuid)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, JWT_SECRET)
                .compact();
        TokenCache tokenCache = new TokenCache();
        tokenCache.setToken(accessToken);
        tokenCache.setUuid(uuid);
        redisRepository.saveAccessTokenRedis(tokenCache);
        return accessToken;
    }

    @Override
    //@Cacheable(value = "BearerUpdateToken",key = "#uuid")
    public String getAccessTokenUpdate(String uuid) {
        return redisRepository.findUuidAccessTokenRedis(uuid).getToken();
    }

    @Override
    //@CacheEvict(value = "BearerUpdateToken",key = "#uuid")
    public boolean removeAccessTokenUpdate(String uuid) {
        redisRepository.removeAccessTokenRedis(uuid);
        return true;
    }

    @Override
    public boolean removeTokenServer() {
        LOGGER.info("server remove access token expired");
        List<TokenCache> tokenCaches = redisRepository.findAllAccessTokenRedis();
        for (TokenCache tokenCache : tokenCaches) {
            //Boolean check = jwtTokenProvider.checkTokenExpired(tokenCache.getToken());
            try {
                Claims claims = Jwts.parser()
                        .setSigningKey(JWT_SECRET)
                        .parseClaimsJws(tokenCache.getToken())
                        .getBody();
                return false;
            } catch (Exception ex) {
                redisRepository.removeAccessTokenRedis(tokenCache.getUuid());
            }
        }
        tokenCaches = redisRepository.findAllRefreshTokenRedis();
        for (TokenCache tokenCache : tokenCaches) {
            try {
                JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET_KEY))
                        .withIssuer("auth0")
                        .build();
                return false;
            } catch (Exception ex) {
                redisRepository.removeAccessTokenRedis(tokenCache.getUuid());
            }
        }
        return true;
    }

    @Override
    public String setRefreshTokenUpdate(String uuid) {
        LOGGER.info("lưu vào cache RefreshToken");
        String refreshToken = JWTutils.createToken(uuid);
        TokenCache tokenCache = new TokenCache();
        tokenCache.setUuid(uuid);
        tokenCache.setToken(refreshToken);
        redisRepository.saveRefreshTokenRedis(tokenCache);
        return refreshToken;
    }

    @Override
    public String getRefreshTokenUpdate(String uuid) {
        return redisRepository.findUuidRefreshTokenRedis(uuid).getToken();
    }

    @Override
    public boolean removeRefreshTokenUpdate(String uuid) {
        redisRepository.removeRefreshTokenRedis(uuid);
        return true;
    }
}
