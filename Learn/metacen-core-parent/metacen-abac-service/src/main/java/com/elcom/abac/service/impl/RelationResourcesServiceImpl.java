package com.elcom.abac.service.impl;

import com.elcom.abac.model.RelationResources;
import com.elcom.abac.repository.RelationResourcesRepository;
import com.elcom.abac.service.RelationResourcesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RelationResourcesServiceImpl implements RelationResourcesService {

    @Autowired
    private RelationResourcesRepository relationResourcesRepository;

    @Override
    public List<RelationResources> findAllRelationResources() {
        return relationResourcesRepository.findAll();
    }
}
