package com.elcom.metacen.data.process.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 *
 * @author hoangbd
 */
@Document(collection = "data_process_config")
@SuperBuilder
@Setter
@Getter
@RequiredArgsConstructor
@ToString
//@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class DataProcessConfig extends AbstractDocument {

    @Size(max = 36)
    @NotNull
    @Field("uuid")
    private String uuid;

    @Field("name")
    private String name;

    @Field("data_type")
    private String dataType;

    @Field("process_type")
    private String processType;

    @Field("data_vendor")
    private String dataVendor;

    @Field("detail_config")
    private Object detailConfig;

    @Field("status")
    private Integer status;

    @Field(name = "start_time")
    private Date startTime;

    @Field(name = "end_time")
    private Date endTime;

}
