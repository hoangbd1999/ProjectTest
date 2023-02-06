package elcom.com.neo4j.node;

import lombok.Data;

@Data
public class ValueReport {
    private Long id;
    private String idsSrc ;
    private String ipsSrc ;
    private String nameSrc;

    private String idsDest ;
    private String ipsDest ;
    private String nameDest ;

    private String mediaType;
    private Long fileSize ;
    private Long count  ;
    private String time ;
    private String dateTime ;
    private String key;


}
