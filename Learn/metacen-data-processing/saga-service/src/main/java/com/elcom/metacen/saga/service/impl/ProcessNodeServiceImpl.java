/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.saga.service.impl;

import com.elcom.metacen.saga.model.ProcessNode;
import com.elcom.metacen.saga.repository.ProcessNodeRepository;
import com.elcom.metacen.saga.service.ProcessNodeService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

/**
 *
 * @author Admin
 */
@Service
public class ProcessNodeServiceImpl implements ProcessNodeService {

    @Autowired
    private ProcessNodeRepository processNodeRepository;

    @Override
    @Cacheable(value = "processNode", key = "#processName.concat('-').concat(#nodeName)", unless = "#result == null")
    public ProcessNode findByProcessNameAndNodeName(String processName, String nodeName) {
        return processNodeRepository.findOneByProcessNameAndNodeName(processName, nodeName);
    }

    @Override
    @Cacheable(value = "listProcessNode", key = "#processName", unless = "#result == null")
    public List<ProcessNode> findByProcessName(String processName) {
        return processNodeRepository.findByProcessNameOrderByOrderDesc(processName);
    }

    @Override
    public ProcessNode findNextNode(ProcessNode processNode) {
        ProcessNode node = processNodeRepository.findOneByProcessNameAndOrder(processNode.getProcessName(), processNode.getOrder() + 1);
        return node;
    }

}
