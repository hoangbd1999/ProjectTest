package com.elcom.metacen.id.repository;

import com.elcom.metacen.id.model.dto.TokenCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RedisRepository {

    private static final String  hashKeyAccessToken= "BearerAccessToken";

    private static final String  hashKeyRefreshToken= "BearerRefreshToken";

    @Autowired
    private RedisTemplate redisTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisRepository.class);

    public void saveAccessTokenRedis(TokenCache tokenCache){
        redisTemplate.opsForHash().put(hashKeyAccessToken,tokenCache.getUuid(),tokenCache);
    }
    public List<TokenCache> findAllAccessTokenRedis(){
        return (List<TokenCache>) redisTemplate.opsForHash().values(hashKeyAccessToken);
    }

    public TokenCache findUuidAccessTokenRedis(String uuid){
        return (TokenCache) redisTemplate.opsForHash().get(hashKeyAccessToken,uuid);
    }
    public void removeAccessTokenRedis(String uuid){
        LOGGER.info(" remove cache AccessToken" +uuid);
        redisTemplate.opsForHash().delete(hashKeyAccessToken,uuid);
    }
    public void saveRefreshTokenRedis(TokenCache tokenCache){
        redisTemplate.opsForHash().put(hashKeyRefreshToken,tokenCache.getUuid(),tokenCache);
    }
    public List<TokenCache> findAllRefreshTokenRedis(){
        return (List<TokenCache>) redisTemplate.opsForHash().values(hashKeyRefreshToken);
    }
    public TokenCache findUuidRefreshTokenRedis(String uuid){
        return (TokenCache) redisTemplate.opsForHash().get(hashKeyRefreshToken,uuid);
    }
    public void removeRefreshTokenRedis(String uuid){
        LOGGER.info(" remove cache RefreshToken" +uuid);
        redisTemplate.opsForHash().delete(hashKeyRefreshToken,uuid);
    }


}
