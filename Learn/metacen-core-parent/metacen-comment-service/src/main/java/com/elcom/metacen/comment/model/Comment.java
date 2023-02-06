/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.comment.model;

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
@Table(name = "comments")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Comment.findAll", query = "SELECT c FROM Comment c")})
public class Comment implements Serializable {

    @Id
    @Column(name = "uuidKey", length = 36, nullable = false)
    private String uuidKey;

    @Column(name = "type")
    private Integer type;

    @Column(name = "refId")
    private String refId;

    @Column(name = "content")
    private String content;

    @Column(name = "contentUnsigned")
    private String contentUnsigned;

    @Column(name = "createdUser")
    private String createdUser;

    @Column(name = "updatedUser")
    private String updatedUser;

    @Column(name = "createdTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTime;

    @Column(name = "updatedTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedTime;

    @Column(name = "ingestTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ingestTime;

    @Column(name = "isDeleted")
    private Integer isDeleted;

    public Comment() {
    }

    public Comment(String uuidKey) {
        this.uuidKey = uuidKey;
    }

    @Override
    public String toString() {
        return "com.elcom.metacen.comment.model.Comment[ uuidKey=" + uuidKey + " ]";
    }
}
