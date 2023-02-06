/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.saga.model;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
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
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

/**
 *
 * @author Admin
 */
@Entity
@Table(name = "missed_data", schema = "saga")
@TypeDef(
        name = "jsonb",
        typeClass = JsonBinaryType.class
)
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MissedData.findAll", query = "SELECT m FROM MissedData m")})
public class MissedData implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 36)
    @Column(name = "id")
    private String id;
    @Size(max = 100)
    @Column(name = "proces_name")
    private String procesName;
    @Size(max = 100)
    @Column(name = "node_name")
    private String nodeName;
    @Type(type = "jsonb")
    @Column(name = "received_data")
    private Object receivedData;
    @Type(type = "jsonb")
    @Column(name = "sent_data")
    private Object sentData;

    public MissedData() {
    }

    public MissedData(String processId) {
        this.id = processId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProcesName() {
        return procesName;
    }

    public void setProcesName(String procesName) {
        this.procesName = procesName;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Object getReceivedData() {
        return receivedData;
    }

    public void setReceivedData(Object receivedData) {
        this.receivedData = receivedData;
    }

    public Object getSentData() {
        return sentData;
    }

    public void setSentData(Object sentData) {
        this.sentData = sentData;
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
        if (!(object instanceof MissedData)) {
            return false;
        }
        MissedData other = (MissedData) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.elcom.metacen.sagga.model.MissedData[ processId=" + id + " ]";
    }

}
