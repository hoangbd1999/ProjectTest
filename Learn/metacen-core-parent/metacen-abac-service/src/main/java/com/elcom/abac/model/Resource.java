package com.elcom.abac.model;

import com.elcom.abac.dto.Condition;
import com.elcom.abac.dto.ParamValueDto;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "resources", schema = "metacen_abac")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.cache.annotation.Cacheable
public class Resource implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "urlpatterns")
    private String urlpatterns;

    @Column(name = "name")
    private String name;

    @Column(name = "status")
    private Integer status;

    @Column(name = "urlpatterns_length")
    private Integer urlPatternsLength;

    @Column(name = "create_policy_type")
    private String createPolicyType;

    @Column(name = "update_policy_type")
    private String updatePolicyType;

    @Column(name = "delete_policy_type")
    private String deletePolicyType;

    @Column(name = "detail_policy_type")
    private String detailPolicyType;

    @Column(name = "list_policy_type")
    private String listPolicyType;

    @Column(name = "pip_rpc_exchange")
    private String pipRpcExchange;

    @Column(name = "pip_rpc_queue")
    private String pipRpcQueue;

    @Column(name = "pip_rpc_key")
    private String pipRpcKey;

    @Column(name = "pip_rpc_path")
    private String pipRpcPath;

    @Column(name = "param_value_type")
    private String paramValueType;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Transient
    public Condition condition;

    @Transient
    public ParamValueDto paramValueDto;

    public String getParamValueType() {
        return paramValueType;
    }

    public void setParamValueType(String paramValueType) {
        this.paramValueType = paramValueType;
    }

    public ParamValueDto getParamValueDto() {
        return paramValueDto;
    }

    public void setParamValueDto(ParamValueDto paramValueDto) {
        this.paramValueDto = paramValueDto;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public Long getId() {
        return id;
    }

    public String getListPolicyType() {
        return listPolicyType;
    }

    public void setListPolicyType(String listPolicyType) {
        this.listPolicyType = listPolicyType;
    }

    public void setId(Long id) {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrlpatterns() {
        return urlpatterns;
    }

    public void setUrlpatterns(String urlpatterns) {
        this.urlpatterns = urlpatterns;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getUrlpatternsLength() {
        return urlPatternsLength;
    }

    public void setUrlpatternsLength(Integer urlPatternsLength) {
        this.urlPatternsLength = urlPatternsLength;
    }

    public String getCreatePolicyType() {
        return createPolicyType;
    }

    public void setCreatePolicyType(String createPolicyType) {
        this.createPolicyType = createPolicyType;
    }

    public String getUpdatePolicyType() {
        return updatePolicyType;
    }

    public void setUpdatePolicyType(String updatePolicyType) {
        this.updatePolicyType = updatePolicyType;
    }

    public String getDeletePolicyType() {
        return deletePolicyType;
    }

    public void setDeletePolicyType(String deletePolicyType) {
        this.deletePolicyType = deletePolicyType;
    }

    public String getDetailPolicyType() {
        return detailPolicyType;
    }

    public void setDetailPolicyType(String detailPolicyType) {
        this.detailPolicyType = detailPolicyType;
    }

    public String getPipRpcExchange() {
        return pipRpcExchange;
    }

    public void setPipRpcExchange(String pipRpcExchange) {
        this.pipRpcExchange = pipRpcExchange;
    }

    public String getPipRpcQueue() {
        return pipRpcQueue;
    }

    public void setPipRpcQueue(String pipRpcQueue) {
        this.pipRpcQueue = pipRpcQueue;
    }

    public String getPipRpcKey() {
        return pipRpcKey;
    }

    public void setPipRpcKey(String pipRpcKey) {
        this.pipRpcKey = pipRpcKey;
    }

    public String getPipRpcPath() {
        return pipRpcPath;
    }

    public void setPipRpcPath(String pipRpcPath) {
        this.pipRpcPath = pipRpcPath;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
