package elcom.com.neo4j.dto;

import lombok.*;
import elcom.com.neo4j.clickhouse.model.VsatMedia;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Builder
@AllArgsConstructor
@Setter
@Getter
@ToString
public class VsatMediaPagingDTO implements Serializable {
    private List<VsatMedia> dataRows;
    private Long totalRows;

    public VsatMediaPagingDTO() {
        totalRows = Long.valueOf("0");
        dataRows = new ArrayList<>();
    }

}
