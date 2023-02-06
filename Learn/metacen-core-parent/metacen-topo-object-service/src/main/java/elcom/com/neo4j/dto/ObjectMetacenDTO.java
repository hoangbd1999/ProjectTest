package elcom.com.neo4j.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ObjectMetacenDTO {
    private String objectId;
    private String objectUuid;
    private String objectType;
    private String objectName;
    private String objectMmsi;
}
