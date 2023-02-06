package elcom.com.neo4j.dto;

import lombok.Data;

@Data
public class ValueSpark {
    private String id;
    private String ips;
    private String name;
    private Double longitude;
    private Double latitude;
    //media
    private String typeName;
    private Long fileSize;
    private Long typeSize;
    private String ip;

    //dest
    private String destId;
    private String destIps;
    private String destName;
    private Double destLongitude;
    private Double destLatitude;

    private String eventTime;
}
