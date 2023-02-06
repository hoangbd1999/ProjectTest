/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.saga.repository;

import com.elcom.metacen.saga.model.TransactionData;
import java.util.Date;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Admin
 */
@Repository
public interface TransactionDataRepository extends CrudRepository<TransactionData, String> {

    long deleteByTransactionDataPK_TransactionId(String transactionId);

    List<TransactionData> findByTransactionDataPK_TransactionId(String transactionId);

    TransactionData findOneByTransactionDataPK_TransactionIdAndIsNewest(String transactionId, boolean isNewest);

    List<TransactionData> findByCreatedDateBeforeAndIsNewest(Date createDate, boolean isNewest);

    TransactionData findOneByTransactionDataPK_TransactionIdAndTransactionDataPK_NodeName(String transactionId, String nodeName);

}
