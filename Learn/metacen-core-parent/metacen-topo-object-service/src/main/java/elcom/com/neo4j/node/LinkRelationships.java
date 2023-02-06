package elcom.com.neo4j.node;

import lombok.Data;

@Data
public class LinkRelationships {
    private Integer start ;
    private Integer end ;
    private String mmsi;
    private String uuidEnd ;
    private String ip;
    private String eventTime ;
    private String objectName;
    private String mediaUuid;
    private String mediaType;
    private String uuidStart ;
    private String dataSource;
    private String objectUuid;
    private String objectId;
    private String key;
    private String mentionName;
    private String mentionId;
    private String mentionUuid;
    private String mentionType;
    private String objectType;
    private Integer type;

    private Integer page ;
    private Integer size;
    private  String id;



}
