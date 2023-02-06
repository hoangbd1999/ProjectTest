package com.elcom.abac.service.impl;

import com.elcom.abac.dto.ConditionPolicy;
import com.elcom.abac.dto.PolicyAuthenticationRedis;
import com.elcom.abac.dto.PolicyTemplate;
import com.elcom.abac.model.Policy;
import com.elcom.abac.repository.PolicyRepository;
import com.elcom.abac.repository.RedisRepository;
import com.elcom.abac.service.PolicyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import java.io.IOException;
import java.util.*;

@Service
public class PolicyServiceImpl implements PolicyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyServiceImpl.class);

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private RedisRepository redisRepository;

    @Override
    public List<Policy> findRbacByRoleCode(String resourceCode,String method,String roleCode) {
        return policyRepository.findByResourceCodeAndMethodAndSubjectValue(resourceCode,method,roleCode);
    }

    @Override
    public List<Policy> findBySubjectValue(String roleCode) {
        return policyRepository.findBySubjectValue(roleCode);
    }

    @Override
    public Optional<Policy> findUnique(String resourceCode, String roleCode, String subjectCondition, String subjectType, String effect, String method, String policyType) {
        return policyRepository.findByResourceCodeAndSubjectValueAndSubjectConditionAndSubjectTypeAndEffectAndMethodAndPolicyType(resourceCode,roleCode,subjectCondition,subjectType,effect,method,policyType);
    }

    @Override
    public List<Policy> findByResourceCodeAndSubjectValueIn(String resourceCode, List<String> subjectValues) {
        return policyRepository.findByResourceCodeAndSubjectValueIn(resourceCode,subjectValues);
    }


    @Override
    public Optional<Policy> findById(Integer id) {
        return policyRepository.findById(id);
    }

    @Override
    public List<String> findBySubjectValueGroupBy(String subjectValue) {
        return policyRepository.findBySubjectValueGroupByResourceCode(subjectValue);
    }

    @Override
    public List<Policy> findPolicySubjectValueAnhResourceCode(String subjectValue, String resourceCode) {
        return policyRepository.findPolicySubjectValueAnhResourceCode(subjectValue,resourceCode);
    }

    @Override
    public List<Policy> updateRoleCode(String roleCode, String roleCodeUpdate) {
        return policyRepository.updateRoleCode(roleCode,roleCodeUpdate);
    }

    @Override
    public List<String> findResourceCode(List<String> subjectValue) {
        return policyRepository.findResourceCode(subjectValue);
    }

    @Override
    public List<Policy> findAll() {
        return policyRepository.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Policy savePolicy(Policy policy) {
        policy.setCreatedAt(new Date());
        ConditionPolicy conditionPolicy = new ConditionPolicy();
        if(!policy.getPolicyType().equals("*")) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                conditionPolicy = mapper.readValue(policy.getRules(), ConditionPolicy.class);
                policy = policyRepository.save(policy);

            } catch (JsonMappingException e) {
                e.printStackTrace();
                return null;
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return null;
            }
        }
        Policy policy1 = policyRepository.save(policy);
        policy.setConditionPolicy(conditionPolicy);
        policy.setId(policy1.getId());
        PolicyAuthenticationRedis policyAuthenticationRedis = redisRepository.findPolicyRedis(policy.getSubjectValue());
        if(policyAuthenticationRedis == null){
            policyAuthenticationRedis = new PolicyAuthenticationRedis();
            policyAuthenticationRedis.setRoleCode(policy.getSubjectValue());
            Map<String,Map<String,List<Policy>>> policyMap = new HashMap<>();
            Map<String,List<Policy>> resourcePolicy = new HashMap<>();
            List<Policy> policyList = new ArrayList<>();
            policyList.add(policy);
            resourcePolicy.put(policy.getMethod(),policyList);
            policyMap.put(policy.getResourceCode(),resourcePolicy);
            policyAuthenticationRedis.setPolicies(policyMap);

            redisRepository.savePolicyRedis(policyAuthenticationRedis);
            LOGGER.info("Cập nhập redis policy thay đổi");
        }else {
            Map<String,Map<String,List<Policy>>> policyMap = policyAuthenticationRedis.getPolicies();
            Map<String,List<Policy>> resourcePolicy = policyMap.get(policy.getResourceCode());
            if(resourcePolicy ==null || resourcePolicy.isEmpty()){
                resourcePolicy = new HashMap<>();
                List<Policy> policyList = new ArrayList<>();
                policyList.add(policy);
                resourcePolicy.put(policy.getMethod(),policyList);
                policyMap.put(policy.getResourceCode(),resourcePolicy);
                policyAuthenticationRedis.setPolicies(policyMap);
                redisRepository.savePolicyRedis(policyAuthenticationRedis);
                LOGGER.info("Cập nhập redis policy thay đổi");
            }else {
                List<Policy> policyList = resourcePolicy.get(policy.getMethod());
                if(policyList==null || policyList.isEmpty()){
                    policyList = new ArrayList<>();
                    policyList.add(policy);
                    resourcePolicy.put(policy.getMethod(),policyList);
                    policyMap.replace(policy.getResourceCode(),resourcePolicy);
                    policyAuthenticationRedis.setPolicies(policyMap);
                    redisRepository.savePolicyRedis(policyAuthenticationRedis);
                    LOGGER.info("Cập nhập redis policy thay đổi");

                }else {
                    policyList.add(policy);
                    resourcePolicy.replace(policy.getMethod(),policyList);
                    policyMap.replace(policy.getResourceCode(),resourcePolicy);
                    policyAuthenticationRedis.setPolicies(policyMap);
                    redisRepository.savePolicyRedis(policyAuthenticationRedis);
                    LOGGER.info("Cập nhập redis policy thay đổi");
                }
            }
        }
        policy.setConditionPolicy(null);
        return policy1;
    }


    @Override
    @Transactional(rollbackFor = {Exception.class})
    public List<Policy> saveList(List<Policy> policyLists) {
        ConditionPolicy conditionPolicy = new ConditionPolicy();
        for (Policy policy: policyLists
        ) {
            if (!policy.getPolicyType().equals("*")) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    conditionPolicy = mapper.readValue(policy.getRules(), ConditionPolicy.class);

                } catch (JsonMappingException e) {
                    e.printStackTrace();
                    return null;
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
        List<Policy> policyList1 = (List<Policy>) policyRepository.saveAll(policyLists);
        List<Policy> policys = new ArrayList<>(policyList1);
        for (Policy policy: policys
        ) {
            if(!policy.getPolicyType().equals("*")) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    conditionPolicy = mapper.readValue(policy.getRules(), ConditionPolicy.class);
                    policy.setConditionPolicy(conditionPolicy);

                } catch (JsonMappingException e) {
                    e.printStackTrace();
                    return null;
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            PolicyAuthenticationRedis policyAuthenticationRedis = redisRepository.findPolicyRedis(policy.getSubjectValue());
            if(policyAuthenticationRedis == null){
                policyAuthenticationRedis = new PolicyAuthenticationRedis();
                policyAuthenticationRedis.setRoleCode(policy.getSubjectValue());
                Map<String,Map<String,List<Policy>>> policyMap = new HashMap<>();
                Map<String,List<Policy>> resourcePolicy = new HashMap<>();
                List<Policy> policyList = new ArrayList<>();
                policyList.add(policy);
                resourcePolicy.put(policy.getMethod(),policyList);
                policyMap.put(policy.getResourceCode(),resourcePolicy);
                policyAuthenticationRedis.setPolicies(policyMap);
                redisRepository.savePolicyRedis(policyAuthenticationRedis);
                LOGGER.info("Cập nhập redis policy thay đổi");
            }else {
                Map<String,Map<String,List<Policy>>> policyMap = policyAuthenticationRedis.getPolicies();
                Map<String,List<Policy>> resourcePolicy = policyMap.get(policy.getResourceCode());
                if(resourcePolicy ==null || resourcePolicy.isEmpty()){
                    resourcePolicy = new HashMap<>();
                    List<Policy> policyList = new ArrayList<>();
                    policyList.add(policy);
                    resourcePolicy.put(policy.getMethod(),policyList);
                    policyMap.put(policy.getResourceCode(),resourcePolicy);
                    policyAuthenticationRedis.setPolicies(policyMap);
                    redisRepository.savePolicyRedis(policyAuthenticationRedis);
                    LOGGER.info("Cập nhập redis policy thay đổi");
                }else {
                    List<Policy> policyList = resourcePolicy.get(policy.getMethod());
                    if(policyList==null || policyList.isEmpty()){
                        policyList = new ArrayList<>();
                        policyList.add(policy);
                        resourcePolicy.put(policy.getMethod(),policyList);
                        policyMap.replace(policy.getResourceCode(),resourcePolicy);
                        policyAuthenticationRedis.setPolicies(policyMap);
                        redisRepository.savePolicyRedis(policyAuthenticationRedis);
                        LOGGER.info("Cập nhập redis policy thay đổi");

                    }else {
                        policyList.add(policy);
                        resourcePolicy.replace(policy.getMethod(),policyList);
                        policyMap.replace(policy.getResourceCode(),resourcePolicy);
                        policyAuthenticationRedis.setPolicies(policyMap);
                        redisRepository.savePolicyRedis(policyAuthenticationRedis);
                        LOGGER.info("Cập nhập redis policy thay đổi");
                    }
                }
            }
            policy.setConditionPolicy(null);
        }
        return policys;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Policy updatePolicy(Policy policy) {
        Optional<Policy> policyOptional = policyRepository.findById(policy.getId());
        if(policyOptional.isPresent()) {
            Policy policyUpdate = policyOptional.get();
            ConditionPolicy conditionPolicy = new ConditionPolicy();
            if (!policy.getPolicyType().equals("*")) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    conditionPolicy = mapper.readValue(policy.getRules(), ConditionPolicy.class);
                    policy.setConditionPolicy(null);
                    policy.setCreatedAt(policyUpdate.getCreatedAt());

                } catch (JsonMappingException e) {
                    e.printStackTrace();
                    return null;
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            policy = policyRepository.save(policy);
//            policy.setConditionPolicy(conditionPolicy);
            String roleCode = policyUpdate.getSubjectValue();
            String roleCodeUpdate = policy.getSubjectValue();
            List<String> roleCodes = new ArrayList<>();
            roleCodes.add(roleCode);
            if(!roleCode.equals(roleCodeUpdate)){
                roleCodes.add(roleCodeUpdate);
            }
//            policy = policyRepository.save(policy);
//            policy.setConditionPolicy(conditionPolicy);
            updateReis(roleCodes);
            return policy;
        }
//        Policy policy1 = new Policy();
        return null;
    }

    public void updateReis(List<String> roleCodes){
        for (String roleCode: roleCodes
        ) {
            List<String> resourceCodes = policyRepository.findBySubjectValueGroupByResourceCode(roleCode);
            Map<String,Map<String,List<Policy>>> mapRedis = new HashMap<>();
            for (String resourceCode: resourceCodes
            ) {
                List<Policy> policyList = policyRepository.findPolicySubjectValueAnhResourceCode(roleCode,resourceCode);
                List<Policy> postPolicy = new ArrayList<>();
                List<Policy> putPolicy = new ArrayList<>();
                List<Policy> detailPolicy = new ArrayList<>();
                List<Policy> listPolicy = new ArrayList<>();
                List<Policy> deletePolicy = new ArrayList<>();
                for (Policy  policy: policyList ) {
                    ObjectMapper mapper = new ObjectMapper();
                    ConditionPolicy conditionPolicy;
                    try {
                        if(!policy.getPolicyType().equals("*")) {
                            conditionPolicy = mapper.readValue(policy.getRules(), ConditionPolicy.class);
                        }else {
                            conditionPolicy = new ConditionPolicy();
                        }
                        Policy policy1 = new Policy();
                        policy1.setConditionPolicy(conditionPolicy);
                        policy1.setId(policy.getId());
                        policy1.setPolicyType(policy.getPolicyType());
                        policy1.setSubjectValue(policy.getSubjectValue());
                        policy1.setSubjectCondition(policy.getSubjectCondition());
                        policy1.setResourceCode(policy.getResourceCode());
                        policy1.setMethod(policy.getMethod());
                        policy1.setEffect(policy.getEffect());

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
                        e.printStackTrace();
                    }
                }
                Map<String,List<Policy>> resourceMap = new HashMap<>();
                if (!postPolicy.isEmpty())
                    resourceMap.put("POST", postPolicy);
                if (!putPolicy.isEmpty())
                    resourceMap.put("PUT", putPolicy);
                if (!deletePolicy.isEmpty())
                    resourceMap.put("DELETE", deletePolicy);
                if (!detailPolicy.isEmpty())
                    resourceMap.put("DETAIL", detailPolicy);
                if (!listPolicy.isEmpty())
                    resourceMap.put("LIST", listPolicy);
                mapRedis.put(resourceCode,resourceMap);
            }
            PolicyAuthenticationRedis policyAuthenticationRedis1 = new PolicyAuthenticationRedis();
            policyAuthenticationRedis1.setRoleCode(roleCode);
            policyAuthenticationRedis1.setPolicies(mapRedis);
            redisRepository.savePolicyRedis(policyAuthenticationRedis1);
            LOGGER.info("Cập nhập redis policy thay đổi");

        }
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public boolean deletePolicyId(Policy policy) {
            List<String> roleCode = new ArrayList<>();
            roleCode.add(policy.getSubjectValue());
            policyRepository.deleteById(policy.getId());
            updateReis(roleCode);
            return true;
    }

    @Override
    public boolean deleteId(Integer id) {
        policyRepository.deleteById(id);
        return false;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void deleteBySubjectValue(String subjectValue) {
        policyRepository.deleteBySubjectValue(subjectValue);
    }

    @Override
    public List<Policy> UpdateListPolicy(List<Policy> policies) {
        return  (List<Policy>) policyRepository.saveAll(policies);
    }

    @Override
    public List<PolicyTemplate> findTemplatePolicyResource(String resourceCode, String method) {
        List<PolicyTemplate> policyTemplates = new ArrayList<>();
        PolicyAuthenticationRedis policyAuthenticationRedis = redisRepository.findPolicyRedis("TEMPLATE");
        if(policyAuthenticationRedis == null){
            return policyTemplates;
        }else {
            Map<String,Map<String,List<Policy>>> policyMap = policyAuthenticationRedis.getPolicies();
            Map<String,List<Policy>> resourcePolicy = policyMap.get(resourceCode);
            if(resourcePolicy ==null || resourcePolicy.isEmpty()){
                return policyTemplates;
            }else {
                List<Policy> policyList = resourcePolicy.get(method);
                if(policyList==null || policyList.isEmpty()){
                    return policyTemplates;
                }else {
                    for (Policy policy: policyList
                         ) {
                        PolicyTemplate policyTemplate = new PolicyTemplate(policy);
                        policyTemplates.add(policyTemplate);

                    }
                    return policyTemplates;
                }
            }
        }
    }

}
