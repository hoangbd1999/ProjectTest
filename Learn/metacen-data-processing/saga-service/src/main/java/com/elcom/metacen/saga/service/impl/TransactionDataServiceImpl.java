/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.saga.service.impl;

import com.elcom.metacen.saga.message.SagaMessage;
import com.elcom.metacen.saga.model.TransactionData;
import com.elcom.metacen.saga.model.TransactionDataPK;
import com.elcom.metacen.saga.repository.TransactionDataRepository;
import com.elcom.metacen.saga.service.TransactionDataService;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Admin
 */
@Service
@Transactional
public class TransactionDataServiceImpl implements TransactionDataService {
    
    private static final int timeoutMs = 20000;
    
    @Autowired
    private TransactionDataRepository transactionDataRepository;
    
    @Override
    public long deleteTransactionDataByTransactionId(String transactionId) {
        return transactionDataRepository.deleteByTransactionDataPK_TransactionId(transactionId);
    }
    
    @Override
    @Transactional
    public void saveSagaMessage(SagaMessage message, int retryTime,boolean processStatus) {
        TransactionData oldTransactionData = transactionDataRepository.findOneByTransactionDataPK_TransactionIdAndIsNewest(message.getTransactionId(), true);
        if (oldTransactionData != null) {
            oldTransactionData.setIsNewest(false);
            transactionDataRepository.save(oldTransactionData);
        }
        TransactionData newTransactionData = new TransactionData();
        newTransactionData.setTransactionDataPK(new TransactionDataPK(message.getTransactionId(), message.getNodeName()));
        newTransactionData.setReceivedData(message.getReceivedData());
        newTransactionData.setSentData(message.getSentData());
        newTransactionData.setRetryTime(retryTime);
        newTransactionData.setProcessName(message.getProcessName());
        newTransactionData.setIsNewest(true);
        newTransactionData.setCreatedDate(new Date());
        newTransactionData.setStatus(processStatus);
        transactionDataRepository.save(newTransactionData);
        
    }
    
    @Override
    public void saveTransactionData(TransactionData transactionData) {
        transactionData.setCreatedDate(new Date());
        transactionDataRepository.save(transactionData);
    }
    
    @Override
    public List<TransactionData> findByTransactionId(String transactionId) {
        return transactionDataRepository.findByTransactionDataPK_TransactionId(transactionId);
    }
    
    @Override
    public List<TransactionData> getListTransactionTimeOut() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MILLISECOND, -timeoutMs);
        return transactionDataRepository.findByCreatedDateBeforeAndIsNewest(calendar.getTime(), true);
    }
    
    @Override
    public TransactionData findByTransactionIdAndNodeName(String transactionId, String nodeName) {
        return transactionDataRepository.findOneByTransactionDataPK_TransactionIdAndTransactionDataPK_NodeName(transactionId, nodeName);
    }
    
}
