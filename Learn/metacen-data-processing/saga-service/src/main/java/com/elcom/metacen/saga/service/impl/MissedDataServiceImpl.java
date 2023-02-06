/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.saga.service.impl;

import com.elcom.metacen.saga.message.SagaMessage;
import com.elcom.metacen.saga.model.MissedData;
import com.elcom.metacen.saga.model.ProcessNode;
import com.elcom.metacen.saga.model.TransactionData;
import com.elcom.metacen.saga.repository.MissedDataRepository;
import com.elcom.metacen.saga.service.MissedDataService;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Admin
 */
@Service
public class MissedDataServiceImpl implements MissedDataService {

    @Autowired
    private MissedDataRepository missedDataRepository;

    @Override
    public void saveMissedMessage(SagaMessage message) {
        MissedData missedData = new MissedData();
        missedData.setId(UUID.randomUUID().toString());
        missedData.setNodeName(message.getNodeName());
        missedData.setProcesName(message.getProcessName());
        missedData.setReceivedData(message.getReceivedData());
        missedData.setSentData(message.getSentData());
        missedDataRepository.save(missedData);

    }

    @Override
    public void saveMissedTransaction(TransactionData transactionData, ProcessNode processNode) {
        MissedData missedData = new MissedData();
        missedData.setId(UUID.randomUUID().toString());
        missedData.setNodeName(transactionData.getTransactionDataPK().getNodeName());
        missedData.setProcesName(processNode.getProcessName());
        missedData.setReceivedData(transactionData.getReceivedData());
        missedData.setSentData(transactionData.getSentData());
        missedDataRepository.save(missedData);
    }

}
