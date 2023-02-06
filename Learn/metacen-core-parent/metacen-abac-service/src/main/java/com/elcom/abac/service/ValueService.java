package com.elcom.abac.service;

import com.elcom.abac.model.RoleEvent;
import com.elcom.abac.model.RoleNotify;
import com.elcom.abac.model.RoleState;

import java.util.List;

public interface ValueService {
    public List<RoleEvent> findRoleEvent();

    public List<RoleState> findRoleState();

    public List<RoleNotify> findRoleNotify();

}
