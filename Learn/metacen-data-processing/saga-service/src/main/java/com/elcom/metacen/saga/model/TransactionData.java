/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.saga.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

/**
 *
 * @author Admin
 */
@Entity
@Table(name = "transaction_data", schema = "saga")
@TypeDef(
        name = "jsonb",
        typeClass = JsonBinaryType.class
)
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TransactionData.findAll", query = "SELECT t FROM TransactionData t")})
public class TransactionData implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected TransactionDataPK transactionDataPK;
    @Size(max = 36)
    @Column(name = "process_name")
    private String processName;
    @Type(type = "jsonb")
    @Column(name = "received_data")
    private Object receivedData;
    @Column(name = "retry_time")
    private Integer retryTime;
    @Column(name = "is_newest")
    private Boolean isNewest;
    @Column(name = "created_date")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdDate;
    @Type(type = "jsonb")
    @Column(name = "sent_data")
    private Object sentData;
    @Column(name = "status")
    private Boolean status;

    public TransactionData() {
    }

    public TransactionData(TransactionDataPK transactionDataPK) {
        this.transactionDataPK = transactionDataPK;
    }

    public TransactionData(String transactionId, String nodeName) {
        this.transactionDataPK = new TransactionDataPK(transactionId, nodeName);
    }

    public TransactionDataPK getTransactionDataPK() {
        return transactionDataPK;
    }

    public void setTransactionDataPK(TransactionDataPK transactionDataPK) {
        this.transactionDataPK = transactionDataPK;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public Object getReceivedData() {
        return receivedData;
    }

    public void setReceivedData(Object receivedData) {
        this.receivedData = receivedData;
    }

    public Integer getRetryTime() {
        return retryTime;
    }

    public void setRetryTime(Integer retryTime) {
        this.retryTime = retryTime;
    }

    public Boolean getIsNewest() {
        return isNewest;
    }

    public void setIsNewest(Boolean isNewest) {
        this.isNewest = isNewest;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Object getSentData() {
        return sentData;
    }

    public void setSentData(Object sentData) {
        this.sentData = sentData;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (transactionDataPK != null ? transactionDataPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TransactionData)) {
            return false;
        }
        TransactionData other = (TransactionData) object;
        if ((this.transactionDataPK == null && other.transactionDataPK != null) || (this.transactionDataPK != null && !this.transactionDataPK.equals(other.transactionDataPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.elcom.metacen.sagga.model.TransactionData[ transactionDataPK=" + transactionDataPK + " ]";
    }

}
