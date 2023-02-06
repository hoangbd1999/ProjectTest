package com.elcom.abac.repository;

import com.elcom.abac.model.RoleUser;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface RoleUserRepository extends CrudRepository<RoleUser,Integer> {

    @Query(value = "select role_code from role_user WHERE uuid_user =?1",nativeQuery = true)
    List<String> findByUuid(String uuid);

    @Query(value = "select uuid_user from role_user GROUP BY uuid_user",nativeQuery = true)
    List<String> findAllUuid();

    Optional<RoleUser> findByUuidUserAndRoleCode(String uuidUser, String roleCode);

    @Query(value = "select uuid_user from role_user WHERE role_code =?1",nativeQuery = true)
    List<String> findUuidInRole(String roleCode);

    @Query(value = "select uuid_user from role_user WHERE role_code in ?1",nativeQuery = true)
    List<String> findUuidInRoleCode(List<String> roleCodes);

    List<RoleUser> findByRoleCode(String roleCode);

    @Modifying
    @Transactional
    @Query(value = "update role_user set role_code =?2 WHERE role_code =?1",nativeQuery = true)
    List<RoleUser> updateRoleCode(String roleCode, String roleCodeUpdate);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM role_user WHERE role_code=?1",nativeQuery = true)
    void deleteRoleCode(String roleCode);

    List<RoleUser> findByUuidUser(String uuid);

    @Query(value = "select * from role_user where uuid_user=?1 LIMIT 1",nativeQuery = true)
    Optional<RoleUser> findByUser(String uuidUser);
}
