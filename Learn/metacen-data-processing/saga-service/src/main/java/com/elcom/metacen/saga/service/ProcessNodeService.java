/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.saga.service;

import com.elcom.metacen.saga.model.ProcessNode;
import java.util.List;

/**
 *
 * @author Admin
 */
public interface ProcessNodeService {

    public ProcessNode findByProcessNameAndNodeName(String processName, String nodeName);

    public ProcessNode findNextNode(ProcessNode processNode);

    List<ProcessNode> findByProcessName(String processName);
}
