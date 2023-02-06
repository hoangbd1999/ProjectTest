package elcom.com.neo4j.clickhouse.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import elcom.com.neo4j.model.NodeToNodePk;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

public class TopologyNodeToNodeMonth implements Serializable {

    @Column(name = "id")
    private String id;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd HH:mm:ss"
    )
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;
    @Column(name = "node_ids")
    private String nodeIds;
    @Column(name = "node_size")
    private Long nodeSize;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getNodeIds() {
        return nodeIds;
    }

    public void setNodeIds(String nodeIds) {
        this.nodeIds = nodeIds;
    }

    public Long getNodeSize() {
        return nodeSize;
    }

    public void setNodeSize(Long nodeSize) {
        this.nodeSize = nodeSize;
    }


}
