package elcom.com.neo4j.dto;

import lombok.Data;

@Data
public class NodeInfoDTO {
    String idObject;
    String ip;
    int type;
    Integer dataSource;
    Integer countDay;


}
