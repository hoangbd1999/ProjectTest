/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.report.model;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Admin
 */
@Entity
@Getter
@Setter
@Table(name = "data_analyzed")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "DataAnalyzed.findAll", query = "SELECT da FROM DataAnalyzed da")})
public class DataAnalyzed implements Serializable {

    @Id
    @Column(name = "uuidKey", length = 36, nullable = false)
    private String uuidKey;

    @Column(name = "refUuidKey")
    private String refUuidKey;

    @Column(name = "processType")
    private String processType;

    @Column(name = "eventTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date eventTime;

    @Column(name = "processTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date processTime;

    @Column(name = "ingestTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ingestTime;

    @Column(name = "processStatus")
    private Integer processStatus;

    public DataAnalyzed() {
    }

    public DataAnalyzed(String uuidKey) {
        this.uuidKey = uuidKey;
    }

    @Override
    public String toString() {
        return "com.elcom.metacen.report.model.DataAnalyzed[ uuidKey=" + uuidKey + " ]";
    }
}
