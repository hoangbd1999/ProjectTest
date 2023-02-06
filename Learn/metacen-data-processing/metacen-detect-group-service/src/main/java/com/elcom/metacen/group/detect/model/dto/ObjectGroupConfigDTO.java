package com.elcom.metacen.group.detect.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.awt.geom.Point2D;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
public class ObjectGroupConfigDTO {
    protected String id;
    private String createdBy;
    private LocalDateTime createdDate;
    private String modifiedBy;
    private LocalDateTime modifiedDate;
    private String uuid;
    private String name;
    private List<Point2D> coordinates;
    private Integer distance;
    private Integer isActive;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
