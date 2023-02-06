package elcom.com.neo4j.service.impl;

import elcom.com.neo4j.clickhouse.model.TopologyNodeToNodeDay;
import elcom.com.neo4j.clickhouse.model.TopologyNodeToNodeMonth;
import elcom.com.neo4j.service.NodeImportantService;
import org.apache.flink.api.java.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import ru.yandex.clickhouse.ClickHouseConnection;
import ru.yandex.clickhouse.ClickHouseDataSource;
import ru.yandex.clickhouse.settings.ClickHouseProperties;
import ru.yandex.clickhouse.settings.ClickHouseQueryParam;

import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class NodeImportantServiceImpl implements NodeImportantService {
    @Autowired
    private Environment environment;
    @Override
    public void saveMultiDay(List<TopologyNodeToNodeDay> datas) {
        String url = environment.getProperty("spring.datasource.click_house.url");
        ClickHouseProperties properties = new ClickHouseProperties();
        properties.setUser(environment.getProperty("spring.datasource.click_house.username"));
        properties.setPassword(environment.getProperty("spring.datasource.click_house.password"));
        properties.setSessionId(UUID.randomUUID().toString());
        properties.setMaxQuerySize(100000000000L);
        properties.setMaxThreads(100000);
        ClickHouseDataSource dataSource = new ClickHouseDataSource(url, properties);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            ClickHouseConnection comn = dataSource.getConnection();
            String sql = "insert into metacen_local.topology_node_to_node_day(id,nodeIds,nodeSize,startTime)"
                    + " VALUES " ;
            for (TopologyNodeToNodeDay value: datas
            ) {
                sql = sql +"(?,?,?,?),";
            }
            sql = sql.substring(0,sql.length()-1);
            int index =1 ;
            PreparedStatement pstmt = comn.prepareStatement(sql);
            for (TopologyNodeToNodeDay value: datas
            ) {
                pstmt.setString(index++,value.getId());
                pstmt.setString(index++,value.getNodeIds());
                pstmt.setLong(index++,value.getNodeSize());
                pstmt.setString(index++,df.format(value.getStartTime()));
            }
            pstmt.execute();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void saveMultiMonth(List<TopologyNodeToNodeMonth> datas) {
        String url = environment.getProperty("spring.datasource.click_house.url");
        ClickHouseProperties properties = new ClickHouseProperties();
        properties.setUser(environment.getProperty("spring.datasource.click_house.username"));
        properties.setPassword(environment.getProperty("spring.datasource.click_house.password"));
        properties.setSessionId(UUID.randomUUID().toString());
        properties.setMaxQuerySize(100000000000L);
        properties.setMaxThreads(100000);
        ClickHouseDataSource dataSource = new ClickHouseDataSource(url, properties);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            ClickHouseConnection comn = dataSource.getConnection();
            String sql = "insert into metacen_local.topology_node_to_node_month(id,nodeIds,nodeSize,startTime)"
                    + " VALUES " ;
            for (TopologyNodeToNodeMonth value: datas
            ) {
                sql = sql +"(?,?,?,?),";
            }
            sql = sql.substring(0,sql.length()-1);
            int index =1 ;
            PreparedStatement pstmt = comn.prepareStatement(sql);
            for (TopologyNodeToNodeMonth value: datas
            ) {
                pstmt.setString(index++,value.getId());
                pstmt.setString(index++,value.getNodeIds());
                pstmt.setLong(index++,value.getNodeSize());
                pstmt.setString(index++,df.format(value.getStartTime()));
            }
            pstmt.execute();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
