package com.elcom.abac.repository;

import com.elcom.abac.dto.PolicyAuthenticationRedis;
import com.elcom.abac.dto.RoleCodeUuidRedis;
import com.elcom.abac.dto.AdminRoleCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class RedisRepository {

    private static final String hashKeyAdminAbac = "BearerMetaCENAdminAbac_v1.0@elcom.com.vn";
    private static final String hashKeyPolicy = "BearerMetaCENPolicyAbac_v1@elcom.com.vn";
    private static final String hashKeyRoleCodeuuid = "BearerMetaCENRoleCodeuuidAbac_v1@elcom.com.vn";

    @Autowired
    private RedisTemplate redisTemplate;
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisRepository.class);

    public void saveAdminAbac(AdminRoleCode roleRbacPolicy) {
        redisTemplate.opsForHash().put(hashKeyAdminAbac, "admin", roleRbacPolicy);
    }

    public AdminRoleCode findAdmin() {
        return (AdminRoleCode) redisTemplate.opsForHash().get(hashKeyAdminAbac, "admin");
    }

    public void removeRoleRbacRedis() {
        redisTemplate.opsForHash().delete(hashKeyAdminAbac, "admin");
    }

    public void saveRoleUuidRedis(RoleCodeUuidRedis roleCodeUuidRedis) {
        redisTemplate.opsForHash().put(hashKeyRoleCodeuuid, roleCodeUuidRedis.getUuid(), roleCodeUuidRedis);
    }

    public List<RoleCodeUuidRedis> findRedisRoleCode() {
        return (List<RoleCodeUuidRedis>) redisTemplate.opsForHash().values(hashKeyRoleCodeuuid);
    }

    public RoleCodeUuidRedis findRoleCodeRedis(String uuid) {
        return (RoleCodeUuidRedis) redisTemplate.opsForHash().get(hashKeyRoleCodeuuid, uuid);
    }

    public void removeRoleCodeRedis(String uuid) {
        LOGGER.info(" remove cache role code " + uuid);
        redisTemplate.opsForHash().delete(hashKeyRoleCodeuuid, uuid);
    }

    public void savePolicyRedis(PolicyAuthenticationRedis policyAuthenticationRedis) {
        redisTemplate.opsForHash().put(hashKeyPolicy, policyAuthenticationRedis.getRoleCode(), policyAuthenticationRedis);
    }

    public List<PolicyAuthenticationRedis> findRedis() {
        return (List<PolicyAuthenticationRedis>) redisTemplate.opsForHash().values(hashKeyPolicy);
    }

    public PolicyAuthenticationRedis findPolicyRedis(String roleCode) {
        return (PolicyAuthenticationRedis) redisTemplate.opsForHash().get(hashKeyPolicy, roleCode);
    }

    public void removePolicyRedis(String key) {
        LOGGER.info(" remove cache Policy " + key);
        redisTemplate.opsForHash().delete(hashKeyPolicy, key);
    }

    public void removeKeysPolicyRedis(List<String> keys) {
        LOGGER.info(" remove cache Policy");
        redisTemplate.opsForHash().delete(hashKeyPolicy, keys);
    }

}
