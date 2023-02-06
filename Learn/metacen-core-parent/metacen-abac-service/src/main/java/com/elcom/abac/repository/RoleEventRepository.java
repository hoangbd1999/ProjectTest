package com.elcom.abac.repository;

import com.elcom.abac.model.Resource;
import com.elcom.abac.model.RoleEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleEventRepository extends JpaRepository<RoleEvent,Integer> {
    List<RoleEvent> findAll();
}
