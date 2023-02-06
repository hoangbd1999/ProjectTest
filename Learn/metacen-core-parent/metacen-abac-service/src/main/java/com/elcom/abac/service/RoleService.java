package com.elcom.abac.service;

import com.elcom.abac.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface RoleService {
    public Optional<Role> findById(Integer id);

    public Role saveRole(Role role);

    public Role updateRole(Role role);

    public boolean deleteRoleById(Role role);

    public List<Role> findByIsAdminAndId(List<Long> ids);

    public List<Role> findByRoleCodeIn(List<String> roleCodes);

    public List<Role> findByIdIn(List<Long> ids);

    public List<String> findAll();

    public List<Role> findAllRole();

    public Optional<Role> findByRoleCode(String roleCode);

    public  List<String> findIsAdmin();

    public  List<Role> search(String keyword);

    public Page<Role> getAll(Pageable pageable);

    public Page<Role> searchBy(String keyword, Pageable pageable);

}

