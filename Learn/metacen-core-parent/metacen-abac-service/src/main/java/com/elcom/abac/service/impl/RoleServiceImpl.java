package com.elcom.abac.service.impl;

import com.elcom.abac.dto.AdminRoleCode;
import com.elcom.abac.dto.PolicyAuthenticationRedis;
import com.elcom.abac.dto.RoleCodeUuidRedis;
import com.elcom.abac.model.Policy;
import com.elcom.abac.model.Role;
import com.elcom.abac.model.RoleUser;
import com.elcom.abac.repository.RedisRepository;
import com.elcom.abac.repository.RoleRepository;
import com.elcom.abac.service.PolicyService;
import com.elcom.abac.service.RoleService;
import com.elcom.abac.service.RoleUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

@Service
public class RoleServiceImpl implements RoleService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoleServiceImpl.class);

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleUserService roleUserService;

    @Autowired
    private RedisRepository redisRepository;

    @Autowired
    private PolicyService policyService;

    @Override
    public Optional<Role> findById(Integer id) {
        return roleRepository.findById(id);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Role saveRole(Role role) {
        role.setCreatedAt(new Date());
        Optional<Role> roleCheck = roleRepository.findByRoleCode(role.getRoleCode());
        if (roleCheck.isPresent())
            return null;
        role = roleRepository.save(role);
        if (role.getIsAdmin() == 1) {
            AdminRoleCode adminRoleCode = redisRepository.findAdmin();
            Map<String, String> roleCodes = adminRoleCode.getRoleCodeAdmin();
            roleCodes.put(role.getRoleCode(), role.getRoleCode());
            adminRoleCode.setRoleCodeAdmin(roleCodes);
            redisRepository.saveAdminAbac(adminRoleCode);
        }
        return role;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Role updateRole(Role role) {
        Optional<Role> roleDb = roleRepository.findById(role.getId());
        if (roleDb.isPresent()) {
            boolean check = false;
            Role roleUpdate = roleDb.get();
            if (roleUpdate.getIsAdmin() == 1) {
                check = true;
            }
            String roleCode = roleUpdate.getRoleCode();
            String roleCodeUpdate = role.getRoleCode();
            if (!roleUpdate.getRoleName().equals(role.getRoleName())) {
                roleUpdate.setRoleName(role.getRoleName());
            }
            if (!roleUpdate.getDescription().equals(role.getDescription())) {
                roleUpdate.setDescription(role.getDescription());
            }
            if (!roleUpdate.getRoleCode().equals(role.getRoleCode())) {
                roleUpdate.setRoleCode(role.getRoleCode());
            }
            if (roleUpdate.getIsAdmin() != role.getIsAdmin()) {
                roleUpdate.setIsAdmin(role.getIsAdmin());
            }
            role = roleRepository.save(roleUpdate);
            if (role.getIsAdmin() == 1) {
                AdminRoleCode adminRoleCode = redisRepository.findAdmin();
                Map<String, String> roleCodes = adminRoleCode.getRoleCodeAdmin();
                roleCodes.put(role.getRoleCode(), role.getRoleCode());
                adminRoleCode.setRoleCodeAdmin(roleCodes);
                redisRepository.saveAdminAbac(adminRoleCode);
            }
            if (!roleCodeUpdate.equals(roleCode)) {
                if (check) {
                    AdminRoleCode adminRoleCode = redisRepository.findAdmin();
                    Map<String, String> roleCodes = adminRoleCode.getRoleCodeAdmin();
                    roleCodes.remove(roleCode);
                    adminRoleCode.setRoleCodeAdmin(roleCodes);
                    redisRepository.saveAdminAbac(adminRoleCode);
                }
                List<String> uuids = roleUserService.findUuidInRole(roleCode);
                if (uuids != null && !uuids.isEmpty()) {
                    List<RoleUser> roleUsers = roleUserService.findByRoleCode(roleCode);
                    for (RoleUser roleUser: roleUsers
                         ) {
                        roleUser.setRoleCode(roleCodeUpdate);
                    }
                    roleUsers = roleUserService.UpdateListUser(roleUsers);
//                    roleUserService.updateRoleCode(roleCode, roleCodeUpdate);
                    for (String uuid : uuids) {
                        RoleCodeUuidRedis roleCodeUuidRedis = redisRepository.findRoleCodeRedis(uuid);
                        List<String> roleCodes = roleCodeUuidRedis.getRoleCode();
                        ;
                        roleCodes.remove(roleCode);
                        roleCodes.add(roleCodeUpdate);
                        roleCodeUuidRedis.setRoleCode(roleCodes);
                        redisRepository.saveRoleUuidRedis(roleCodeUuidRedis);
                        LOGGER.info("Cập nhập redis role thay đổi");
                    }
                }
                PolicyAuthenticationRedis policyAuthenticationRedis = redisRepository.findPolicyRedis(roleCode);
                if (policyAuthenticationRedis != null && policyAuthenticationRedis.getPolicies() != null) {

//                    policyService.updateRoleCode(roleCode, roleCodeUpdate);
                    List<Policy> policieUpdate = policyService.findBySubjectValue(roleCode);
                    if(policieUpdate!=null && !policieUpdate.isEmpty()){
                        for (Policy policy: policieUpdate
                             ) {
                            policy.setSubjectValue(roleCodeUpdate);

                        }
                        policyService.UpdateListPolicy(policieUpdate);
                    }

                    List<String> resourceCodes = policyService.findBySubjectValueGroupBy(roleCode);
                    Map<String, Map<String, List<Policy>>> policies = policyAuthenticationRedis.getPolicies();
                    for (String resourceCode : resourceCodes
                    ) {
                        Map<String, List<Policy>> policyMap = policies.get(resourceCode);
                        List<Policy> postPolicy = policyMap.get("POST");
                        List<Policy> putPolicy = policyMap.get("PUT");
                        List<Policy> detailPolicy = policyMap.get("DETAIL");
                        List<Policy> listPolicy = policyMap.get("LIST");
                        List<Policy> deletePolicy = policyMap.get("DELETE");
                        if (postPolicy != null) {
                            for (int i = 0; i < postPolicy.size(); i++) {
                                Policy policy = postPolicy.get(i);
                                policy.setSubjectValue(roleCodeUpdate);
                                postPolicy.set(i, policy);
                            }
                            policyMap.replace("POST", postPolicy);
                        }
                        if (putPolicy != null) {
                            for (int i = 0; i < putPolicy.size(); i++) {
                                Policy policy = putPolicy.get(i);
                                policy.setSubjectValue(roleCodeUpdate);
                                putPolicy.set(i, policy);
                            }
                            policyMap.replace("PUT", putPolicy);
                        }
                        if (detailPolicy != null) {
                            for (int i = 0; i < detailPolicy.size(); i++) {
                                Policy policy = detailPolicy.get(i);
                                policy.setSubjectValue(roleCodeUpdate);
                                detailPolicy.set(i, policy);
                            }
                            policyMap.replace("DETAIL", detailPolicy);
                        }
                        if (deletePolicy != null) {
                            for (int i = 0; i < deletePolicy.size(); i++) {
                                Policy policy = deletePolicy.get(i);
                                policy.setSubjectValue(roleCodeUpdate);
                                deletePolicy.set(i, policy);
                            }
                            policyMap.replace("DELETE", deletePolicy);
                        }
                        if (listPolicy != null) {
                            for (int i = 0; i < listPolicy.size(); i++) {
                                Policy policy = listPolicy.get(i);
                                policy.setSubjectValue(roleCodeUpdate);
                                listPolicy.set(i, policy);

                            }
                            policyMap.replace("LIST", listPolicy);
                        }
                        policies.replace(resourceCode, policyMap);
                    }
                    redisRepository.removePolicyRedis(roleCode);
                    policyAuthenticationRedis.setRoleCode(roleCodeUpdate);
                    policyAuthenticationRedis.setPolicies(policies);
                    redisRepository.savePolicyRedis(policyAuthenticationRedis);
                    LOGGER.info("Cập nhập redis role thay đổi");
                }
            }
            return role;
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public boolean deleteRoleById(Role role) {
        if (role.getIsAdmin() == 1) {
            AdminRoleCode adminRoleCode = redisRepository.findAdmin();
            Map<String, String> roleCodes = adminRoleCode.getRoleCodeAdmin();
            roleCodes.remove(role.getRoleCode());
            adminRoleCode.setRoleCodeAdmin(roleCodes);
            redisRepository.saveAdminAbac(adminRoleCode);
        }
        String roleCode = role.getRoleCode();
        roleRepository.deleteById(role.getId());
        List<String> uuids = roleUserService.findUuidInRole(roleCode);
        roleUserService.deleteByRoleCode(roleCode);
        policyService.deleteBySubjectValue(roleCode);
        redisRepository.removePolicyRedis(roleCode);
        if (uuids != null) {
            for (String uuid : uuids
            ) {
                RoleCodeUuidRedis roleCodeUuidRedis = redisRepository.findRoleCodeRedis(uuid);
                List<String> roleCodes = roleCodeUuidRedis.getRoleCode();
                roleCodes.remove(roleCode);
                if (roleCodes.isEmpty()) {
                    redisRepository.removeRoleCodeRedis(uuid);
                } else {
                    roleCodeUuidRedis.setRoleCode(roleCodes);
                    redisRepository.saveRoleUuidRedis(roleCodeUuidRedis);
                    LOGGER.info("Cập nhập redis role xóa");
                }
            }
        }
        return true;
    }

    @Override
    public List<Role> findByIsAdminAndId(List<Long> roles) {
        return roleRepository.findByIsAdminAndIdIn(1, roles);
    }

    @Override
    public List<Role> findByRoleCodeIn(List<String> roleCodes) {
        return roleRepository.findByRoleCodeIn(roleCodes);
    }

    @Override
    public List<Role> findByIdIn(List<Long> ids) {
        return roleRepository.findByIdIn(ids);
    }

    @Override
    public List<String> findAll() {
        return roleRepository.findAl();
    }

    @Override
    public List<Role> findAllRole() {
        return roleRepository.findAll();
    }

    @Override
    public Optional<Role> findByRoleCode(String roleCode) {
        return roleRepository.findByRoleCode(roleCode);
    }

    @Override
    public List<String> findIsAdmin() {
        return roleRepository.findIsAdmin();
    }

    @Override
    public List<Role> search(String keyword) {
        try {
            keyword = URLDecoder.decode(keyword, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error(ex.toString());
        }
        return roleRepository.findByRoleCodeContainingIgnoreCaseOrRoleNameContainingIgnoreCase(keyword,keyword);
    }

    @Override
    public Page<Role> getAll(Pageable pageable) {
        return roleRepository.findAll(pageable);
    }

    @Override
    public Page<Role> searchBy(String keyword, Pageable pageable) {
        try {
            keyword = URLDecoder.decode(keyword, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error(ex.toString());
        }
        return roleRepository.searchRoleByRoleCodeContainingIgnoreCaseOrRoleNameContainingIgnoreCase(keyword,keyword, pageable);
    }
}
