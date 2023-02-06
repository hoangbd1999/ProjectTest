package com.elcom.abac.repository;

import com.elcom.abac.model.Policy;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface PolicyRepository extends CrudRepository<Policy,Integer> {
    List<Policy> findByIdIn(List<Integer> listId);
    List<Policy> findAll();

    List<Policy> findByResourceCodeAndMethodAndSubjectValue(String resourceCode,String method,String roleCode);

    List<Policy> findByResourceCodeAndSubjectValueIn(String resourceCode, List<String> subjectValues);

    @Query(value = "select resource_code from policy where subject_value =?1 group by resource_code ",nativeQuery = true)
    List<String> findBySubjectValueGroupByResourceCode(String subjectValue);

    @Query(value = "select * from policy where subject_value =?1 and resource_code =?2",nativeQuery = true)
    List<Policy> findPolicySubjectValueAnhResourceCode(String subjectValue, String resourceCode);

    @Query(value = "update policy set subject_value =?2 WHERE id = 38 and subject_value = ?1",nativeQuery = true)
    List<Policy> updateRoleCode(String roleCode, String roleCodeUpdate);

    @Query(value = "select resource_code from policy where subject_value in ?1 group by resource_code ",nativeQuery = true)
    List<String> findResourceCode(List<String> subjectValue);

    void deleteBySubjectValue(String subjectValue);

    List<Policy> findBySubjectValueAndResourceCodeAndMethod(String subjectValue, String resourceCode, String method);

    List<Policy> findBySubjectValue(String roleCode);
    Optional<Policy> findByResourceCodeAndSubjectValueAndSubjectConditionAndSubjectTypeAndEffectAndMethodAndPolicyType(String resourceCode, String roleCode, String subjectCondition,
                                                                                                                       String subjectType, String effect, String method, String policyType);

}
