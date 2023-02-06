/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.menumanagement.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Admin
 */
@Entity
@Table(name = "resources", schema = "metacen_menu_management")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Resources.findAll", query = "SELECT r FROM Resources r")})
public class Resources implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Size(max = 100)
    @Column(name = "code")
    private String code;

    @Size(max = 255)
    @Column(name = "description")
    private String description;

    @Size(max = 100)
    @Column(name = "name")
    private String name;

    @Column(name = "status")
    private int status;

    @Size(max = 65535)
    @Column(name = "urlpatterns")
    private String urlpatterns;

    @Size(max = 255)
    @Column(name = "append_role")
    private String appendRole;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "urlpatterns_length")
    private Integer urlpatternsLength;

    public Resources() {
    }

    public Resources(Integer id) {
        this.id = id;
    }

    public Resources(Integer id, int status) {
        this.id = id;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getUrlpatterns() {
        return urlpatterns;
    }

    public void setUrlpatterns(String urlpatterns) {
        this.urlpatterns = urlpatterns;
    }

    public String getAppendRole() {
        return appendRole;
    }

    public void setAppendRole(String appendRole) {
        this.appendRole = appendRole;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getUrlpatternsLength() {
        return urlpatternsLength;
    }

    public void setUrlpatternsLength(Integer urlpatternsLength) {
        this.urlpatternsLength = urlpatternsLength;
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
        if (!(object instanceof Resources)) {
            return false;
        }
        Resources other = (Resources) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.elcom.metacen.rbac.model.Resources[ id=" + id + " ]";
    }

}
