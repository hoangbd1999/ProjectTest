package com.elcom.metacen.group.detect.model.dto;

import com.elcom.metacen.group.detect.model.MappingPairInfo;
import com.elcom.metacen.group.detect.model.ObjectGroupMapping;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.*;

@Data
@Accessors(chain = true)
public class ObjectGroupDTO {
    protected String id;
    private String createdBy;
    private LocalDateTime createdDate;
    private String modifiedBy;
    private LocalDateTime modifiedDate;
    private Integer configDistanceLevel;
    private String configName;
    private Integer configTogetherTime;
    private String configUuid;
    private LocalDateTime confirmDate;
    private Integer isConfirmed;
    private Integer isDeleted;
    private String name;
    private String note;
    private String uuid;
    private List<ObjectGroupMapping> ships;
    private Map<String, MappingPairInfo> mappingPairInfos;
    private LocalDateTime firstTogetherTime;
    private LocalDateTime lastTogetherTime;

    public ObjectGroupDTO addMappingPairInfo(String key, MappingPairInfo info) {
        if (this.mappingPairInfos == null) {
            this.mappingPairInfos = new HashMap<>();
        }
        mappingPairInfos.put(key, info);
        return this;
    }
}
