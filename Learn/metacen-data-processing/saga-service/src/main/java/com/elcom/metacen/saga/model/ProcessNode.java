/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.saga.model;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Admin
 */
@Entity
@Table(name = "process_node", schema = "saga")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ProcessNode.findAll", query = "SELECT p FROM ProcessNode p")})
public class ProcessNode implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 36)
    @Column(name = "id")
    private String id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "process_name")
    private String processName;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "node_name")
    private String nodeName;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "node_receive_queue")
    private String nodeReceiveQueue;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "node_rollback_queue")
    private String nodeRollbackQueue;
    @Basic(optional = false)
    @NotNull
    @Column(name = "order")
    private int order;
    @Basic(optional = false)
    @NotNull
    @Column(name = "is_last_node")
    private boolean lastNode;
    @Basic(optional = false)
    @NotNull
    @Column(name = "is_rollback")
    private boolean rollback;
    @Basic(optional = false)
    @NotNull
    @Column(name = "is_retry")
    private boolean retry;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "message_broker_type")
    private String messageBrokerType;

    public ProcessNode() {
    }

    public ProcessNode(String nodeId) {
        this.id = nodeId;
    }

    public ProcessNode(String nodeId, String processName, String nodeName, String nodeReceiveQueue, String nodeRollbackQueue, int order, boolean isEnd, boolean isRollback, boolean isRetry, String messageBrokerType) {
        this.id = nodeId;
        this.processName = processName;
        this.nodeName = nodeName;
        this.nodeReceiveQueue = nodeReceiveQueue;
        this.nodeRollbackQueue = nodeRollbackQueue;
        this.order = order;
        this.lastNode = isEnd;
        this.rollback = isRollback;
        this.retry = isRetry;
        this.messageBrokerType = messageBrokerType;
    }

    public String getNodeId() {
        return id;
    }

    public void setNodeId(String nodeId) {
        this.id = nodeId;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeReceiveQueue() {
        return nodeReceiveQueue;
    }

    public void setNodeReceiveQueue(String nodeReceiveQueue) {
        this.nodeReceiveQueue = nodeReceiveQueue;
    }

    public String getNodeRollbackQueue() {
        return nodeRollbackQueue;
    }

    public void setNodeRollbackQueue(String nodeRollbackQueue) {
        this.nodeRollbackQueue = nodeRollbackQueue;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isLastNode() {
        return lastNode;
    }

    public void setIsLastNode(boolean lastNode) {
        this.lastNode = lastNode;
    }

    public boolean isRollback() {
        return rollback;
    }

    public void setRollback(boolean rollback) {
        this.rollback = rollback;
    }

    public boolean isRetry() {
        return retry;
    }

    public void setRetry(boolean retry) {
        this.retry = retry;
    }

    public String getMessageBrokerType() {
        return messageBrokerType;
    }

    public void setMessageBrokerType(String messageBrokerType) {
        this.messageBrokerType = messageBrokerType;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ProcessNode)) {
            return false;
        }
        ProcessNode other = (ProcessNode) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.elcom.metacen.sagga.model.ProcessNode[ nodeId=" + id + " ]";
    }

}
