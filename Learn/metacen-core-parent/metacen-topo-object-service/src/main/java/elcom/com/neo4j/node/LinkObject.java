package elcom.com.neo4j.node;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
//import org.springframework.data.neo4j.core.schema.Node;


//@JsonIdentityInfo(generator=JSOGGenerator.class)
@Data
public class LinkObject {
    private String objectUuid;
    private String objectName;
    private String objectId;
    private String objectType;
    private Integer id;


    public LinkObject() {
    }

    public LinkObject(String objectUuid, String objectName, String objectId,String objectType, Integer id) {
        this.objectUuid = objectUuid;
        this.objectName = objectName;
        this.objectId = objectId;
        this.id = id;
        this.objectType=objectType;
    }
}
