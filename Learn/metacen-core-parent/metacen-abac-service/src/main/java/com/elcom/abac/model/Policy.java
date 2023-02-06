package com.elcom.abac.model;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

import com.elcom.abac.dto.ConditionPolicy;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "policy", schema = "metacen_abac",uniqueConstraints={
        @UniqueConstraint(columnNames = {"subject_type", "subject_condition","subject_value","method","effect","policy_type","resource_code"})
})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.cache.annotation.Cacheable
public class Policy implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "subject_type")
    private String subjectType;

    @Column(name = "subject_condition")
    private String subjectCondition;

    @Column(name = "subject_value")
    private String subjectValue;

    @Column(name = "method")
    private String method;

    @Column(name = "effect")
    private String effect;

    @Column(name = "policy_type")
    private String policyType;

    @Column(name = "resource_code")
    private String resourceCode;

    @Column(name = "rules")
    private String rules;


    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @org.springframework.data.annotation.Transient
    private ConditionPolicy conditionPolicy;

    public String getResourceCode() {
        return resourceCode;
    }

    public void setResourceCode(String resourceCode) {
        this.resourceCode = resourceCode;
    }

    public ConditionPolicy getConditionPolicy() {
        return conditionPolicy;
    }

    public void setConditionPolicy(ConditionPolicy conditionPolicy) {
        this.conditionPolicy = conditionPolicy;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public String getPolicyType() {
        return policyType;
    }

    public void setPolicyType(String policyType) {
        this.policyType = policyType;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rule) {
        this.rules = rule;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }

    public String getSubjectCondition() {
        return subjectCondition;
    }

    public void setSubjectCondition(String subjectCondition) {
        this.subjectCondition = subjectCondition;
    }

    public String getSubjectValue() {
        return subjectValue;
    }

    public void setSubjectValue(String subjectValue) {
        this.subjectValue = subjectValue;
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
        if (!(object instanceof Policy)) {
            return false;
        }
        Policy other = (Policy) object;
        return !((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id)));
    }

}
