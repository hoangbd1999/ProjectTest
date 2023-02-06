package elcom.com.neo4j.dto;

import java.io.Serializable;

public class KeytoObject implements Serializable {
    private String key;
    private String objectInfoKey;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getObjectInfo() {
        return objectInfoKey;
    }

    public void setObjectInfo(String objectInfo) {
        this.objectInfoKey = objectInfo;
    }

}
