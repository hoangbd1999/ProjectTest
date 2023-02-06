package com.elcom.metacen.content.redis;

import com.elcom.metacen.content.dto.FolderDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RedisRepository {

    private static final String  keyToObject= "keyToObjectFolder@20222023";

    @Autowired
    private RedisTemplate redisTemplate;
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisRepository.class);

    public void saveFolder(FolderDTO keytoObject){
        redisTemplate.opsForHash().put(keyToObject,keytoObject.getName(),keytoObject);
//        redisTemplate.opsForHash().put(keyToObject,keytoObject.getName(),"oek");
    }
    public FolderDTO findNameFolder(String key){
        return (FolderDTO) redisTemplate.opsForHash().get(keyToObject,key);
    }
    public void removeFolderRedis(String key){
        redisTemplate.opsForHash().delete(keyToObject,key);
    }

    @Cacheable(value = "folder")
    public List<FolderDTO> findFolder(){
        return (List<FolderDTO>) redisTemplate.opsForHash().values(keyToObject);
    }


}
