package com.elcom.metacen.group.detect.model;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "object_group_general_config")
@SuperBuilder
@Setter
@Getter
@RequiredArgsConstructor
@ToString
public class ObjectGroupGeneralConfig {
    @Id
    private String id;

    @Field("distance_level")
    private Integer distanceLevel;

    @Field("modified_by")
    private String modifiedBy;

    @Field("modified_date")
    private String modifiedDate;

    @Field("together_time")
    private Integer togetherTime;
}
