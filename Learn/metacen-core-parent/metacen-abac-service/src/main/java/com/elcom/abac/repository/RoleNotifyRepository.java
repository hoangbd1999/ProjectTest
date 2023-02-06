package com.elcom.abac.repository;

import com.elcom.abac.model.RoleEvent;
import com.elcom.abac.model.RoleNotify;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleNotifyRepository extends JpaRepository<RoleNotify,Integer> {
    List<RoleNotify> findAll();
}
