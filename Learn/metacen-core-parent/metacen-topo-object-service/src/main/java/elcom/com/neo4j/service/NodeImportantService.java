package elcom.com.neo4j.service;

import elcom.com.neo4j.clickhouse.model.TopologyNodeToNodeDay;
import elcom.com.neo4j.clickhouse.model.TopologyNodeToNodeMonth;

import java.util.List;

public interface NodeImportantService {
    public void saveMultiDay(List<TopologyNodeToNodeDay> datas);
    public void saveMultiMonth(List<TopologyNodeToNodeMonth> datas);
}
