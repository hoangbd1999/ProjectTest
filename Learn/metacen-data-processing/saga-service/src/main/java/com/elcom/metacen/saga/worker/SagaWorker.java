/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.saga.worker;

import com.elcom.metacen.saga.kafka.KafkaClient;
import com.elcom.metacen.saga.message.SagaMessage;
import com.elcom.metacen.saga.model.ProcessNode;
import com.elcom.metacen.saga.model.TransactionData;
import com.elcom.metacen.saga.model.TransactionDataPK;
import com.elcom.metacen.saga.rabbitmq.RabbitMQClient;
import com.elcom.metacen.saga.service.MissedDataService;
import com.elcom.metacen.saga.service.ProcessNodeService;
import com.elcom.metacen.saga.service.TransactionDataService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Admin
 */
@Service
public class SagaWorker {

    @Autowired
    private KafkaClient kafkaClient;
    @Autowired
    private RabbitMQClient rabbitMQClient;
    @Autowired
    private TransactionDataService transactionDataService;
    @Autowired
    private MissedDataService missedDataService;
    @Autowired
    private ProcessNodeService processNodeService;

    private static final int retryTimeLimit = 3;

    public void handleSuccessMessage(SagaMessage message, ProcessNode processNode) {
        if (processNode.isLastNode()) {
            transactionDataService.deleteTransactionDataByTransactionId(message.getTransactionId());
        } else {
            transactionDataService.saveSagaMessage(message, 0, true);
        }
    }

    public void handleFailedMessage(SagaMessage message, ProcessNode processNode) throws JsonProcessingException {
        TransactionData oldTransactionData = transactionDataService.findByTransactionIdAndNodeName(message.getTransactionId(), message.getNodeName());
        if (processNode.isRetry() && oldTransactionData == null) {
            retry(message.getReceivedData(), processNode);
            transactionDataService.saveSagaMessage(message, 1, false);
        } else if (processNode.isRetry() && oldTransactionData.getRetryTime() <= retryTimeLimit) {
            retry(message.getReceivedData(), processNode);
            oldTransactionData.setRetryTime(oldTransactionData.getRetryTime() + 1);
            transactionDataService.saveTransactionData(oldTransactionData);
        } else if (processNode.isRollback()) {
            rollbackFromNode(processNode, message.getTransactionId());
            transactionDataService.deleteTransactionDataByTransactionId(message.getTransactionId());
        } else {
            missedDataService.saveMissedMessage(message);
            transactionDataService.deleteTransactionDataByTransactionId(message.getTransactionId());
        }
    }

    private void rollbackFromNode(ProcessNode node, String transactionId) throws JsonProcessingException {
        List<ProcessNode> listProcessNodes = processNodeService.findByProcessName(node.getProcessName());
        List<TransactionData> listTransactionDatas = transactionDataService.findByTransactionId(transactionId);
        Map<String, List<TransactionData>> transactionDataGroupByNode = listTransactionDatas.parallelStream()
                .collect(Collectors.groupingBy(item -> item.getTransactionDataPK().getNodeName()));
        for (ProcessNode processNode : listProcessNodes) {
            List<TransactionData> transactionDataByNode = transactionDataGroupByNode.get(processNode.getNodeName());
            if (transactionDataByNode == null || transactionDataByNode.isEmpty()) {
                continue;
            }
            if (!processNode.isRollback()) {
                break;
            }
            TransactionData transactionData = transactionDataByNode.get(0);
            callRollbackData(processNode, transactionData.getReceivedData());

        }
    }

    private void callRollbackData(ProcessNode node, Object data) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String message = mapper.writeValueAsString(data);
        if (node.getMessageBrokerType().equalsIgnoreCase("KAFKA")) {
            kafkaClient.callKafkaServerWorker(node.getNodeRollbackQueue(), message);
        } else {
            rabbitMQClient.callWorkerService(node.getNodeRollbackQueue(), message);
        }

    }

    private void retry(Object dataMessage, ProcessNode processNode) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String messageCall = mapper.writeValueAsString(dataMessage);
        if (processNode.getMessageBrokerType().equalsIgnoreCase("KAFKA")) {
            kafkaClient.callKafkaServerWorker(processNode.getNodeReceiveQueue(), messageCall);
        } else {
            rabbitMQClient.callWorkerService(processNode.getNodeReceiveQueue(), messageCall);
        }
    }

    public void handleTimeOutTransaction(TransactionData transactionData) throws JsonProcessingException {
        ProcessNode processNode = processNodeService.findByProcessNameAndNodeName(transactionData.getProcessName(), transactionData.getTransactionDataPK().getNodeName());
        if (transactionData.getStatus()) {
            ProcessNode nextNode = processNodeService.findNextNode(processNode);
            TransactionData transactionDataNextNode = new TransactionData();
            transactionDataNextNode.setTransactionDataPK(new TransactionDataPK(transactionData.getTransactionDataPK().getTransactionId(), nextNode.getNodeName()));
            transactionDataNextNode.setCreatedDate(new Date());
            transactionDataNextNode.setProcessName(nextNode.getProcessName());
            transactionDataNextNode.setSentData(null);
            transactionDataNextNode.setReceivedData(transactionData.getSentData());
            transactionDataNextNode.setRetryTime(0);
            transactionDataNextNode.setStatus(false);
            transactionDataNextNode.setIsNewest(true);

            transactionData.setIsNewest(false);
            transactionDataService.saveTransactionData(transactionData);
            transactionDataService.saveTransactionData(transactionDataNextNode);
            handleTimeOutTransaction(transactionDataNextNode);

        } else {
            if (transactionData.getRetryTime() <= retryTimeLimit) {
                retry(transactionData.getReceivedData(), processNode);
                transactionData.setRetryTime(transactionData.getRetryTime() + 1);
                transactionDataService.saveTransactionData(transactionData);
            } else if (processNode.isRollback()) {
                kafkaClient.recreateTopic(processNode.getNodeReceiveQueue());
                rollbackFromNode(processNode, transactionData.getTransactionDataPK().getTransactionId());
                transactionDataService.deleteTransactionDataByTransactionId(transactionData.getTransactionDataPK().getTransactionId());
            } else {
                kafkaClient.recreateTopic(processNode.getNodeReceiveQueue());
                missedDataService.saveMissedTransaction(transactionData, processNode);
                transactionDataService.deleteTransactionDataByTransactionId(transactionData.getTransactionDataPK().getTransactionId());
            }
        }

    }
}
