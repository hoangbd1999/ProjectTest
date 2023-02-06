package elcom.com.neo4j.Schedule;

import elcom.com.neo4j.dto.KeytoObject;
import elcom.com.neo4j.dto.ObjectInfo;

import java.util.List;

public class Cache {
    public static List<KeytoObject> keytoObjectList;
    public static List<ObjectInfo> objectInfoList;

    public static List<KeytoObject> getKeytoObjectList() {
        return keytoObjectList;
    }

    public static void setKeytoObjectList(List<KeytoObject> keytoObjectList) {
        Cache.keytoObjectList = keytoObjectList;
    }

    public static List<ObjectInfo> getObjectInfoList() {
        return objectInfoList;
    }

    public static void setObjectInfoList(List<ObjectInfo> objectInfoList) {
        Cache.objectInfoList = objectInfoList;
    }
}
