/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.saga.repository;

import com.elcom.metacen.saga.model.ProcessNode;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Admin
 */
@Repository
public interface ProcessNodeRepository extends CrudRepository<ProcessNode, String> {

    ProcessNode findOneByProcessNameAndNodeName(String processName, String nodeName);

    List<ProcessNode> findByProcessNameOrderByOrderDesc(String processName);

    ProcessNode findOneByProcessNameAndOrder(String processName, int order);
}
