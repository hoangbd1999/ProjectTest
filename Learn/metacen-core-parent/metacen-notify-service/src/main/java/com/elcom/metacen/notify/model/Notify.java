/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.notify.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Admin
 */
@Entity
@Table(name = "notify")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Notify.findAll", query = "SELECT n FROM Notify n")})
public class Notify implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    protected NotifyPK notifyPK;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "icon")
    private String icon;

    @Column(name = "url")
    private String url;

    @Column(name = "status_view")
    private int statusView;

    @Column(name = "type")
    private int type;

    @Column(name = "time_send_notify")
    private long timeSendNotify;

    @Column(name = "updated_at")
    private long updatedAt;

    @Column(name = "object_id")
    private String objectId;


    public Notify() {
    }

    public Notify(NotifyPK notifyPK) {
        this.notifyPK = notifyPK;
    }

    public Notify(NotifyPK notifyPK, String userId, String content, int statusView, int type, long updatedAt, String objectId) {
        this.notifyPK = notifyPK;
        this.userId = userId;
        this.content = content;
        this.statusView = statusView;
        this.type = type;
        this.updatedAt = updatedAt;
        this.objectId = objectId;
    }

    public Notify(String id, long createdAt) {
        this.notifyPK = new NotifyPK(id, createdAt);
    }

    public NotifyPK getNotifyPK() {
        return notifyPK;
    }

    public void setNotifyPK(NotifyPK notifyPK) {
        this.notifyPK = notifyPK;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getStatusView() {
        return statusView;
    }

    public void setStatusView(int statusView) {
        this.statusView = statusView;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getTimeSendNotify() {
        return timeSendNotify;
    }

    public void setTimeSendNotify(long timeSendNotify) {
        this.timeSendNotify = timeSendNotify;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (notifyPK != null ? notifyPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Notify)) {
            return false;
        }
        Notify other = (Notify) object;
        if ((this.notifyPK == null && other.notifyPK != null) || (this.notifyPK != null && !this.notifyPK.equals(other.notifyPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.elcom.metacen.notify.model.Notify[ notifyPK=" + notifyPK + " ]";
    }

}
