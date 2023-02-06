package elcom.com.neo4j.redis;

import elcom.com.neo4j.dto.ObjectInfo;
import elcom.com.neo4j.dto.KeytoObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RedisRepository {

    private static final String  keyToObject= "keyToObject";
    private static final String  hashKeyObjectInfo= "hashKeyObjectInfo";
    private static final String  hashKeyNode = "hashKeyNode";
    private static final String  hashKeyNodeDay = "hashKeyNodeDay";
    private static final String  hashKeyNodeWeek = "hashKeyNodeWeek";
    private static final String  hashKeyNodeMonth = "hashKeyNodeMonth";
    private static final String  hashKeyNodeYear = "hashKeyNodeYear";

    private static final String  keyTime = "keyTime";

    @Autowired
    private RedisTemplate redisTemplate;
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisRepository.class);

    public void saveHashKeyObject(KeytoObject keytoObject){
        redisTemplate.opsForHash().put(keyToObject,keytoObject.getKey(),keytoObject);
    }
    public KeytoObject findIdObject(String key){
        return (KeytoObject) redisTemplate.opsForHash().get(keyToObject,key);
    }
    public void removeKeyToObjectRedis(String key){
        redisTemplate.opsForHash().delete(keyToObject,key);
    }
    public void saveObjectInfo(ObjectInfo objectInfo){
        redisTemplate.opsForHash().put(hashKeyObjectInfo,objectInfo.getId(),objectInfo);
    }
    public List<KeytoObject> findKeytoObject(){
        return (List<KeytoObject>) redisTemplate.opsForHash().values(keyToObject);
    }
    public List<KeytoObject> findKeytoObject(List<String> key){
        return (List<KeytoObject>) redisTemplate.opsForHash().multiGet(keyToObject,key);
    }
    public List<String> findNode(List<String> key){
        return (List<String>) redisTemplate.opsForHash().multiGet(hashKeyNode,key);
    }
    public void saveNode(String key, String value){
        redisTemplate.opsForHash().put(hashKeyNode,key,value);
    }
    public void removeNode(String key){
        redisTemplate.opsForHash().delete(hashKeyNode,key);
    }
    public List<String> findNodeDay(List<String> key){
        return (List<String>) redisTemplate.opsForHash().multiGet(hashKeyNodeDay,key);
    }
    public void saveNodeDay(String key, String value){
        redisTemplate.opsForHash().put(hashKeyNodeDay,key,value);
    }
    public void removeNodeDay(String key){
        redisTemplate.opsForHash().delete(hashKeyNodeDay,key);
    }
    public List<String> findNodeWeek(List<String> key){
        return (List<String>) redisTemplate.opsForHash().multiGet(hashKeyNodeWeek,key);
    }
    public void saveNodeWeek(String key, String value){
        redisTemplate.opsForHash().put(hashKeyNodeWeek,key,value);
    }
    public void removeNodeWeek(String key){
        redisTemplate.opsForHash().delete(hashKeyNodeWeek,key);
    }

    public List<String> findNodeMonth(List<String> key){
        return (List<String>) redisTemplate.opsForHash().multiGet(hashKeyNodeMonth,key);
    }
    public void saveNodeMonth(String key, String value){
        redisTemplate.opsForHash().put(hashKeyNodeMonth,key,value);
    }
    public void removeNodeMonth(String key){
        redisTemplate.opsForHash().delete(hashKeyNodeMonth,key);
    }

    public List<String> findNodeYear(List<String> key){
        return (List<String>) redisTemplate.opsForHash().multiGet(hashKeyNodeYear,key);
    }
    public void saveNodeYear(String key, String value){
        redisTemplate.opsForHash().put(hashKeyNodeYear,key,value);
    }
    public void removeNodeYear(String key){
        redisTemplate.opsForHash().delete(hashKeyNodeYear,key);
    }


    public void saveKeyTime( String value){
        redisTemplate.opsForHash().put(keyTime,keyTime,value);
    }
    public String getTime(){
        return (String) redisTemplate.opsForHash().get(keyTime,keyTime);
    }
    public List<ObjectInfo> findRedisObjectInfo(){
        return (List<ObjectInfo>) redisTemplate.opsForHash().values(hashKeyObjectInfo);
    }

    public List<ObjectInfo> findRedisObjectInfo(List<String> key){
        return (List<ObjectInfo>) redisTemplate.opsForHash().multiGet(hashKeyObjectInfo,key);
    }
    public ObjectInfo findObjectInfoId(String id){
        return (ObjectInfo) redisTemplate.opsForHash().get(hashKeyObjectInfo,id);
    }
    public void removeObjectInfo(String id){
        LOGGER.info(" remove object info " + id);
        redisTemplate.opsForHash().delete(hashKeyObjectInfo,id);
    }
//    public void removeHashKeyObjectInfo(){
//        LOGGER.info(" remove cache Policy");
//        redisTemplate.opsForHash().delete(hashKeyObjectInfo);
//    }
//    public void removeKeyToObjectRedis(){
//        LOGGER.info(" remove cache Policy");
//        redisTemplate.opsForHash().delete(keyToObject);
//    }


}
