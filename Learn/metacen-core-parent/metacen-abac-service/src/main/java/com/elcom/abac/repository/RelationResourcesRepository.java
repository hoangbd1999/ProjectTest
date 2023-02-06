/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.abac.repository;

import com.elcom.abac.model.RelationResources;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * @author Admin
 */
@Repository
public interface RelationResourcesRepository extends CrudRepository<RelationResources, Integer>{
    List<RelationResources> findAll();
}
