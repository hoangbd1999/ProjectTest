package com.elcom.abac.repository;

import com.elcom.abac.model.Resource;
import com.elcom.abac.model.RoleState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleStateRepository extends JpaRepository<RoleState,Integer> {
    List<RoleState> findAll();
}
