package com.elcom.abac.service.impl;

import com.elcom.abac.dto.RoleCodeUuidRedis;
import com.elcom.abac.messaging.rabbitmq.RpcServer;
import com.elcom.abac.model.RoleUser;
import com.elcom.abac.repository.RedisRepository;
import com.elcom.abac.repository.RoleRepository;
import com.elcom.abac.repository.RoleUserRepository;
import com.elcom.abac.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class RoleUserServiceImpl implements RoleUserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceServiceImpl.class);

    @Autowired
    private RoleUserRepository roleUserRepository;

    @Autowired
    private RedisRepository redisRepository;

    @Override
    public List<String> findByUuid(String uuid) {
        return roleUserRepository.findByUuid(uuid);
    }

    @Override
    public Optional<RoleUser> findById(Integer id) {
        return roleUserRepository.findById(id);
    }

    @Override
    public List<String> findAllUuid() {
        return roleUserRepository.findAllUuid();
    }

    @Override
    public List<RoleUser> findByUuidUser(String uuid) {
        return roleUserRepository.findByUuidUser(uuid);
    }

    @Override
    public Optional<RoleUser> findByUuidUserAndRoleCode(String uuidUser, String roleCode) {
        return roleUserRepository.findByUuidUserAndRoleCode(uuidUser,roleCode);
    }

    @Override
    public List<RoleUser> updateRoleCode(String roleCode, String roleCodeUpdate) {
        return roleUserRepository.updateRoleCode(roleCode,roleCodeUpdate);
    }

    @Override
    public List<RoleUser> UpdateListUser(List<RoleUser> roleUsers) {
        return (List<RoleUser>) roleUserRepository.saveAll(roleUsers);
    }

    @Override
    public List<String> findUuidInRole(String roleCode) {
        return null;
    }


    @Override
    @Transactional(rollbackFor = {Exception.class})
    public RoleUser save(RoleUser roleUser) {
        roleUser.setCreatedAt(new Date());
        roleUser= roleUserRepository.save(roleUser);
        RoleCodeUuidRedis roleCodeUuidRedis = redisRepository.findRoleCodeRedis(roleUser.getUuidUser());
        if(roleCodeUuidRedis== null ){
            roleCodeUuidRedis = new RoleCodeUuidRedis();
            roleCodeUuidRedis.setUuid(roleUser.getUuidUser());
            List<String> roleCodes = new ArrayList<>();
            roleCodes.add(roleUser.getRoleCode());
            roleCodeUuidRedis.setRoleCode(roleCodes);
            redisRepository.saveRoleUuidRedis(roleCodeUuidRedis);
            LOGGER.info("Update redis do role user được tạo");
        }else {
            List<String> roleCodes = roleCodeUuidRedis.getRoleCode();
            roleCodes.add(roleUser.getRoleCode());
            roleCodeUuidRedis.setRoleCode(roleCodes);
            redisRepository.saveRoleUuidRedis(roleCodeUuidRedis);
            LOGGER.info("Update redis do role user được tạo");
        }
        return roleUser;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public RoleUser update(RoleUser roleUser) {
        Optional<RoleUser> roleUser1 = roleUserRepository.findById(roleUser.getId());
        if(roleUser1.isPresent()){
            String uuid = roleUser1.get().getUuidUser();
            String roleCode = roleUser1.get().getRoleCode();
            roleUser.setCreatedAt(new Date());
            roleUser= roleUserRepository.save(roleUser);
            RoleCodeUuidRedis roleCodeUuidRedis = redisRepository.findRoleCodeRedis(roleUser.getUuidUser());
            if(roleCodeUuidRedis== null ){
                roleCodeUuidRedis = new RoleCodeUuidRedis();
                roleCodeUuidRedis.setUuid(roleUser.getUuidUser());
                List<String> roleCodes = new ArrayList<>();
                roleCodes.add(roleUser.getRoleCode());
                roleCodeUuidRedis.setRoleCode(roleCodes);
                redisRepository.saveRoleUuidRedis(roleCodeUuidRedis);
                LOGGER.info("Update redis do role user được tạo");
            }else {
                List<String> roleCodes = roleCodeUuidRedis.getRoleCode();
                roleCodes.add(roleUser.getRoleCode());
                roleCodeUuidRedis.setRoleCode(roleCodes);
                redisRepository.saveRoleUuidRedis(roleCodeUuidRedis);
                LOGGER.info("Update redis do role user được tạo");
            }
            if(!uuid.equals(roleUser.getUuidUser())){
                RoleCodeUuidRedis roleCodeUuidRedis2 = redisRepository.findRoleCodeRedis(uuid);
                if(roleCodeUuidRedis2!= null ){
                    List<String> roleCodes = roleCodeUuidRedis.getRoleCode();
                    roleCodes.remove(roleCode);
                    if(roleCodes.isEmpty()){
                        redisRepository.removeRoleCodeRedis(uuid);
                        LOGGER.info("Update redis do role user được cap nhap lam mat quyen ");
                    }else {
                    roleCodeUuidRedis.setRoleCode(roleCodes);
                    redisRepository.saveRoleUuidRedis(roleCodeUuidRedis);
                    LOGGER.info("Update redis do role user được cap nhap");
                    }
                }
            }else {
                if(!roleCode.equals(roleUser.getRoleCode())){
                    RoleCodeUuidRedis roleCodeUuidRedis2 = redisRepository.findRoleCodeRedis(uuid);
                    if(roleCodeUuidRedis2!= null ){
                        List<String> roleCodes = roleCodeUuidRedis.getRoleCode();
                        roleCodes.remove(roleCode);
                        if(roleCodes.isEmpty()){
                            redisRepository.removeRoleCodeRedis(uuid);
                            LOGGER.info("Update redis do role user được cap nhap lam mat quyen ");
                        }else {
                            roleCodeUuidRedis.setRoleCode(roleCodes);
                            redisRepository.saveRoleUuidRedis(roleCodeUuidRedis);
                            LOGGER.info("Update redis do role user được cap nhap");
                        }
                    }
                }
            }
            return roleUser;
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public boolean deleteRoleUser(RoleUser roleUser) {
            String uuid = roleUser.getUuidUser();
            roleUserRepository.deleteById(roleUser.getId());
            List<String> roleGroup = roleUserRepository.findByUuid(uuid);
            RoleCodeUuidRedis roleCodeUuidRedis = new RoleCodeUuidRedis();
            roleCodeUuidRedis.setUuid(uuid);
            if(roleGroup==null ||  roleGroup.isEmpty() ){
                redisRepository.removeRoleCodeRedis(uuid);
                LOGGER.info("Update redis do user xóa");
            } else {
                roleCodeUuidRedis.setRoleCode(roleGroup);
                redisRepository.saveRoleUuidRedis(roleCodeUuidRedis);
                LOGGER.info("Update redis do user xóa");
            }
            return true;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void deleteByRoleCode(String roleCode) {
        roleUserRepository.deleteRoleCode(roleCode);
    }

    @Override
    public List<RoleUser> findByRoleCode(String roleCode) {
        return roleUserRepository.findByRoleCode(roleCode);
    }

    @Override
    public Optional<RoleUser> findByUser(String uuidUser) {
        return roleUserRepository.findByUser(uuidUser);
    }

    @Override
    public List<String> findRoleCode(List<String> roleCodes) {
        return roleUserRepository.findUuidInRoleCode(roleCodes);
    }
}
