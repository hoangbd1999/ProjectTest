/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.abac.service;

import com.elcom.abac.model.RelationResources;

import java.util.List;

/**
 *
 * @author Admin
 */
public interface RelationResourcesService {
    
    public List<RelationResources> findAllRelationResources();
}
