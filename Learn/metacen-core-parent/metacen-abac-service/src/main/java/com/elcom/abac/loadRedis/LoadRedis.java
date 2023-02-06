package com.elcom.abac.loadRedis;

import com.elcom.abac.dto.*;
import com.elcom.abac.model.Policy;
import com.elcom.abac.model.Role;
import com.elcom.abac.repository.RedisRepository;
import com.elcom.abac.service.PolicyService;
import com.elcom.abac.service.ResourceServiceAbac;
import com.elcom.abac.service.RoleService;
import com.elcom.abac.service.RoleUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class LoadRedis {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadRedis.class);

    @Autowired
    private RedisRepository redisRepository;

    @Autowired
    private ResourceServiceAbac resourceServiceAbac;

    @Autowired
    private PolicyService policyService;

    @Autowired
    private RoleUserService userService;

    @Autowired
    private RoleService roleService;

    @Bean
    public void loading(){
        List<RoleCodeUuidRedis> roleCodeUuidRedisLoad = redisRepository.findRedisRoleCode();
        if(roleCodeUuidRedisLoad==null|| roleCodeUuidRedisLoad.isEmpty()){
            RoleCodeUuidRedis roleCodeUuidRedis = new RoleCodeUuidRedis();
            List<String> uuids = userService.findAllUuid();
            for (String uuid: uuids
                 ) {
                List<String> roleCodes = userService.findByUuid(uuid);
                roleCodeUuidRedis.setUuid(uuid);
                roleCodeUuidRedis.setRoleCode(roleCodes);
                redisRepository.saveRoleUuidRedis(roleCodeUuidRedis);

            }
        }
        AdminRoleCode adminRoleCode = redisRepository.findAdmin();
        if(adminRoleCode==null){
            adminRoleCode = new AdminRoleCode();
            List<String> roleCodes = roleService.findIsAdmin();
            Map<String,String> mapRoleAmin = new HashMap<>();
            for (String roleCode: roleCodes
                 ) {
                mapRoleAmin.put(roleCode,roleCode);
            }
            adminRoleCode.setRoleCodeAdmin(mapRoleAmin);
            redisRepository.saveAdminAbac(adminRoleCode);
        }


        List<PolicyAuthenticationRedis> policyAuthenticationRedis = redisRepository.findRedis();
        if(policyAuthenticationRedis==null || policyAuthenticationRedis.isEmpty()){
            List<String> roleCodes = roleService.findAll();
            for (String roleCode: roleCodes
                 ) {
                List<String> resourceCodes = policyService.findBySubjectValueGroupBy(roleCode);
                Map<String,Map<String,List<Policy>>> mapRedis = new HashMap<>();
                for (String resourceCode: resourceCodes
                     ) {
                    List<Policy> policyList = policyService.findPolicySubjectValueAnhResourceCode(roleCode,resourceCode);
                    List<Policy> postPolicy = new ArrayList<>();
                    List<Policy> putPolicy = new ArrayList<>();
                    List<Policy> detailPolicy = new ArrayList<>();
                    List<Policy> listPolicy = new ArrayList<>();
                    List<Policy> deletePolicy = new ArrayList<>();
                    for (Policy  policy: policyList ) {
                        ObjectMapper mapper = new ObjectMapper();
                        ConditionPolicy conditionPolicy = new ConditionPolicy();
                        try {
                            if(policy.getRules()!=null) {
                                conditionPolicy = mapper.readValue(policy.getRules(), ConditionPolicy.class);
                            }

                            Policy policy1 = new Policy();
                            policy1.setConditionPolicy(conditionPolicy);
                            policy1.setId(policy.getId());
                            policy1.setPolicyType(policy.getPolicyType());
                            policy1.setSubjectValue(policy.getSubjectValue());
                            policy1.setResourceCode(policy.getResourceCode());
                            policy1.setMethod(policy.getMethod());
                            policy1.setEffect(policy.getEffect());

                            policy1.setConditionPolicy(conditionPolicy);
                            if (policy.getMethod().equals("POST")) {
                                postPolicy.add(policy1);
                            } else if (policy.getMethod().equals("PUT")) {
                                putPolicy.add(policy1);
                            } else if (policy.getMethod().equals("DELETE")) {
                                deletePolicy.add(policy1);
                            } else if (policy.getMethod().equals("DETAIL")) {
                                detailPolicy.add(policy1);
                            } else if (policy.getMethod().equals("LIST")) {
                                listPolicy.add(policy1);
                            }
                        } catch (IOException e) {
                            LOGGER.info("Lỗi paser json rule");
                            e.printStackTrace();
                        }
                    }
                    Map<String,List<Policy>> resourceMap = new HashMap<>();
                    if (!postPolicy.isEmpty()) {
                        resourceMap.put("POST", postPolicy);
                    }
                    if (!putPolicy.isEmpty()) {
                        resourceMap.put("PUT", putPolicy);
                    }
                    if (!deletePolicy.isEmpty()) {
                        resourceMap.put("DELETE", deletePolicy);
                    }
                    if (!detailPolicy.isEmpty()) {
                        resourceMap.put("DETAIL", detailPolicy);
                    }
                    if (!listPolicy.isEmpty()) {
                        resourceMap.put("LIST", listPolicy);
                    }
                    mapRedis.put(resourceCode,resourceMap);
                }
                PolicyAuthenticationRedis policyAuthenticationRedis1 = new PolicyAuthenticationRedis();
                policyAuthenticationRedis1.setRoleCode(roleCode);
                policyAuthenticationRedis1.setPolicies(mapRedis);
                redisRepository.savePolicyRedis(policyAuthenticationRedis1);
            }
        }

    }
}
