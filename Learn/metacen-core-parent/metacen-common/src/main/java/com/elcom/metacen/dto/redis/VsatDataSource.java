package com.elcom.metacen.dto.redis;

import java.io.Serializable;
//import lombok.experimental.SuperBuilder;
//import lombok.*;
//import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
//import org.springframework.data.mongodb.core.mapping.Field;

/**
 *
 * @author anhdv
 */
@Document(collection = "vsat_data_source")
//@SuperBuilder
//@Setter
//@Getter
//@RequiredArgsConstructor
//@ToString
public class VsatDataSource extends Job implements Serializable {

    @Field("id")
    private Long dataSourceId;
    
    @Field("source_name")
    private String dataSourceName;

    public VsatDataSource() {
    }

    public VsatDataSource(Long dataSourceId, String dataSourceName) {
        this.dataSourceId = dataSourceId;
        this.dataSourceName = dataSourceName;
    }

    /**
     * @return the dataSourceId
     */
    public Long getDataSourceId() {
        return dataSourceId;
    }

    /**
     * @param dataSourceId the dataSourceId to set
     */
    public void setDataSourceId(Long dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    /**
     * @return the dataSourceName
     */
    public String getDataSourceName() {
        return dataSourceName;
    }

    /**
     * @param dataSourceName the dataSourceName to set
     */
    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }
}
