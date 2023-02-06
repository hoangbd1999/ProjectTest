package com.elcom.abac.service.impl;

import com.elcom.abac.model.RoleEvent;
import com.elcom.abac.model.RoleNotify;
import com.elcom.abac.model.RoleState;
import com.elcom.abac.repository.RoleEventRepository;
import com.elcom.abac.repository.RoleNotifyRepository;
import com.elcom.abac.repository.RoleStateRepository;
import com.elcom.abac.service.ValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ValueServiceImpl implements ValueService {

    @Autowired
    private RoleEventRepository roleEventRepository;

    @Autowired
    private RoleStateRepository roleStateRepository;

    @Autowired
    private RoleNotifyRepository roleNotifyRepository;

    @Override
    public List<RoleEvent> findRoleEvent() {
        return roleEventRepository.findAll();
    }

    @Override
    public List<RoleState> findRoleState() {
        return roleStateRepository.findAll();
    }

    @Override
    public List<RoleNotify> findRoleNotify() {
        return roleNotifyRepository.findAll();
    }
}
