package com.elcom.abac.model;

import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@Entity
@Table(name = "relation_resources", schema = "metacen_abac")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@XmlRootElement
@NamedQueries({
        @NamedQuery(name = "RelationResources.findAll", query = "SELECT m FROM RelationResources m")})
public class RelationResources implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;

    @Column(name = "resource_parent")
    private String resourceParent;

    @Size(max = 100)
    @Column(name = "method_parent")
    private String methodParent;

    @Column(name = "resource_children")
    private String resourceChildren;

    @Size(max = 100)
    @Column(name = "method_children")
    private String methodChildren;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getResourceParent() {
        return resourceParent;
    }

    public void setResourceParent(String resourceParent) {
        this.resourceParent = resourceParent;
    }

    public String getMethodParent() {
        return methodParent;
    }

    public void setMethodParent(String methodParent) {
        this.methodParent = methodParent;
    }

    public String getResourceChildren() {
        return resourceChildren;
    }

    public void setResourceChildren(String resourceChildren) {
        this.resourceChildren = resourceChildren;
    }

    public String getMethodChildren() {
        return methodChildren;
    }

    public void setMethodChildren(String methodChildren) {
        this.methodChildren = methodChildren;
    }
}
