package com.elcom.abac.repository;

import com.elcom.abac.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends CrudRepository<Role,Integer>, PagingAndSortingRepository<Role, Integer> {

    List<Role> findByIsAdminAndIdIn(Integer isAdmin, List<Long> ids);

    List<Role> findByIdIn(List<Long> ids);

    Optional<Role> findByRoleCode(String roleCode);

    List<Role> findByRoleCodeIn(List<String> roleCodes);

    @Query(value = "select role_code from role ",nativeQuery = true)
    List<String> findAl();

    @Query(value = "select role_code from role  where is_admin = 1",nativeQuery = true)
    List<String> findIsAdmin();

    List<Role> findAll();

    List<Role>  findByRoleCodeContainingIgnoreCaseOrRoleNameContainingIgnoreCase(String roleCode, String roleName);

    Page<Role> findAll(Pageable pageable);

    Page<Role> searchRoleByRoleCodeContainingIgnoreCaseOrRoleNameContainingIgnoreCase(String roleCode, String roleName, Pageable pageable);

}
