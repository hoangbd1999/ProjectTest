/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.saga.service;

import com.elcom.metacen.saga.message.SagaMessage;
import com.elcom.metacen.saga.model.ProcessNode;
import com.elcom.metacen.saga.model.TransactionData;

/**
 *
 * @author Admin
 */
public interface MissedDataService {

    public void saveMissedMessage(SagaMessage message);

    public void saveMissedTransaction(TransactionData transactionData, ProcessNode processNode);
}
