package elcom.com.neo4j.analysis;

import elcom.com.neo4j.dto.KeytoObject;
import elcom.com.neo4j.dto.ObjectInfo;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class KeyObjectDTO {
    public static Map<String, ObjectInfo> mapObjectInfo;
    public static Map<String, String> keyToId;
}
