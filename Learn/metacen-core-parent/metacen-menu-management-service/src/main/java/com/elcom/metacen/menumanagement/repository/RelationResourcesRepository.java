/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.menumanagement.repository;

import com.elcom.metacen.menumanagement.model.RelationResources;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Admin
 */
@Repository
public interface RelationResourcesRepository extends CrudRepository<RelationResources, Integer>{
    List<RelationResources> findAll();
}
