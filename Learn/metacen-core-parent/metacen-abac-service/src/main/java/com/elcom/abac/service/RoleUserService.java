package com.elcom.abac.service;

import com.elcom.abac.model.Role;
import com.elcom.abac.model.RoleUser;

import java.util.List;
import java.util.Optional;

public interface RoleUserService {

    public List<String> findByUuid(String uuid);

    public Optional<RoleUser> findById(Integer id);

    public List<String> findAllUuid();

    public List<RoleUser> findByUuidUser(String uuid);

    Optional<RoleUser> findByUuidUserAndRoleCode(String uuidUser, String roleCode);

    public List<RoleUser> updateRoleCode(String roleCode, String roleCodeUpdate);

    public List<RoleUser> UpdateListUser(List<RoleUser> roleUsers);

    public List<String> findUuidInRole(String roleCode);

    public RoleUser save(RoleUser roleUser);

    public RoleUser update(RoleUser roleUser);

    public boolean deleteRoleUser(RoleUser roleUser);

    public void deleteByRoleCode(String roleCode);

    public List<RoleUser> findByRoleCode(String roleCode);

    public Optional<RoleUser> findByUser(String uuidUser);

    public List<String> findRoleCode(List<String> roleCodes);






}
