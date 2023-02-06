package com.elcom.metacen.vsat.collector.model.mongodb;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Document(collection = "data_collector_config")
@SuperBuilder
@Setter
@Getter
@RequiredArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class DataCollectorConfig implements Serializable {

    @Transient
    public static final String SEQUENCE_NAME = "vehicle_sequence";

    @Id
    protected String id;

    @Size(max = 100)
    @NotNull
    @Field(name = "collect_type")
    private String collectType;

    @Field(name = "is_running_process")
    private Boolean isRunningProcess;

    @Size(max = 1000)
    @Field(name = "note")
    private String note;

    @Size(max = 1000)
    @Field(name = "config_value")
    private String configValue;

    @CreatedDate
    @Field("update_time")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @LastModifiedBy
    @Field("update_by")
    private String updateBy;

}
