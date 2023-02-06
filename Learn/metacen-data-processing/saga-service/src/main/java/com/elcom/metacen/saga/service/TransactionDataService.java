/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.saga.service;

import com.elcom.metacen.saga.message.SagaMessage;
import com.elcom.metacen.saga.model.TransactionData;
import java.util.List;

/**
 *
 * @author Admin
 */
public interface TransactionDataService {

    public long deleteTransactionDataByTransactionId(String transactionId);

    public void saveSagaMessage(SagaMessage message, int retryTime, boolean processStatus);

    public void saveTransactionData(TransactionData transactionData);

    public List<TransactionData> findByTransactionId(String transactionId);

    public List<TransactionData> getListTransactionTimeOut();

    public TransactionData findByTransactionIdAndNodeName(String transactionId, String nodeName);

}
