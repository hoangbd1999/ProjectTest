package elcom.com.neo4j.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@SuperBuilder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MappingVsatResponseDTO implements Serializable {

    private String uuid;
    private Integer vsatDataSourceId;
    private String vsatDataSourceName;
    private String vsatIpAddress;
    private String objectType;
    private String objectId;
    private String objectUuid;
    private String objectName;

}
