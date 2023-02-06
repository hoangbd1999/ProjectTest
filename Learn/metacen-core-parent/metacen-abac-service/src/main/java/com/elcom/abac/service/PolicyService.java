package com.elcom.abac.service;

import com.elcom.abac.dto.PolicyTemplate;
import com.elcom.abac.model.Policy;

import java.util.List;
import java.util.Optional;

public interface PolicyService {

    public Optional<Policy> findById(Integer id);

    public List<String> findBySubjectValueGroupBy(String subjectValue);

    public List<Policy> findPolicySubjectValueAnhResourceCode(String subjectValue, String resourceCode);

    public List<Policy> updateRoleCode(String roleCode, String roleCodeUpdate);

    public  List<String> findResourceCode(List<String> subjectValue);

    public List<Policy> findRbacByRoleCode(String resourceCode,String method,String roleCode);

    public List<Policy> findBySubjectValue(String roleCode);

    public Optional<Policy> findUnique(String resourceCode, String roleCode, String subjectCondition,String subjectType,String effect,String method, String policyType);

    public List<Policy> findByResourceCodeAndSubjectValueIn(String resourceCode, List<String> subjectValues);

    public List<Policy> findAll();

    public Policy savePolicy(Policy policy);

    public List<Policy> saveList(List<Policy> policyLists);

    public Policy updatePolicy(Policy policy);

    public boolean deletePolicyId(Policy policy);

    public boolean deleteId(Integer policy);

    void deleteBySubjectValue(String subjectValue);

    public List<Policy> UpdateListPolicy(List<Policy> policies);

    public List<PolicyTemplate> findTemplatePolicyResource(String resourceCode, String method);
}
