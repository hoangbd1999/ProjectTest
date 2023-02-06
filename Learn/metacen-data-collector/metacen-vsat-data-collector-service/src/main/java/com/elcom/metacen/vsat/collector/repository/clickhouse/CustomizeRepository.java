package com.elcom.metacen.vsat.collector.repository.clickhouse;

import com.elcom.metacen.vsat.collector.repository.BaseRepository;
import java.time.ZoneId;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class CustomizeRepository extends BaseRepository {
    
//    private static final Logger LOGGER = LoggerFactory.getLogger(CustomizeRepository.class);

    @Autowired
    @Qualifier("clickHouseSession")
    SessionFactory clickHouseSessionFactory;
    
    private static final ZoneId ZONE_ID = ZoneId.systemDefault(); // or: ZoneId.of("Europe/Oslo");
    
    @Autowired
    public CustomizeRepository(@Qualifier("vsatEntityManagerFactory") EntityManagerFactory factory
                        , @Qualifier("vsatChDataSource") DataSource clickHouseDataSource) {
        super(factory, clickHouseDataSource);
    }
    
    /** Get config value by collect type, string value return is represent with JSON format
     * @param collectType
     * @return String */
    /*public String getConfigValue(String collectType) {
        Session session = this.openSession();
        try {
            String sql = " SELECT CAST(config_value AS VARCHAR) || '###' || is_running_process " +
                         " FROM " + this.getSchema() + ".data_collector_config " +
                         " WHERE collect_type = :collectType ";
            return (String) session.createNativeQuery(sql).setParameter("collectType", collectType.trim()).uniqueResult();
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        } finally {
            this.closeSession(session);
        }
        return null;
    }*/
    
    /** Sink objectGroupMappingn data from MongoDB to Clickhouse Dim Table
     * @param objectGroupMappings
     * @return boolean */
    /*public boolean sinkLstObjectGroupMappingToDimTable(List<ObjectGroupMapping> objectGroupMappings) {
        Session session = null;
        try {
            session = this.clickHouseSessionFactory.openSession();
            session.doWork(new Work() {
                @Override
                public void execute(Connection conn) throws SQLException {
                    PreparedStatement pstmt = null;
                    try {
                        String sql = " INSERT INTO " + getSchemaViewName() + ".dim_observed_object_group_mapping ( objId, groupId, isDeleted, updatedTime ) VALUES ( ?, ?, ?, ? ) ";
                        pstmt = conn.prepareStatement(sql);
                        int i = 0;
                        for (ObjectGroupMapping e : objectGroupMappings) {
                            
                            pstmt.setString(1, e.getObjId());
                            pstmt.setString(2, e.getGroupId());
                            pstmt.setInt(3, e.getIsDeleted());
                            pstmt.setLong(4, e.getUpdatedTime().atZone(ZONE_ID).toEpochSecond() / 1000L);

                            pstmt.addBatch();

                            if( i % 50 == 0 )
                                LOGGER.info("sinkLstObjectGroupMappingToDimTable.executeBatch return size --> [ {} ]", pstmt.executeBatch().length);

                            i++;
                        }

                        if( pstmt != null )
                            pstmt.executeBatch();
                        
                    } catch (Exception ex) {
                        LOGGER.error("ex: ", ex);
                    } finally {
                        if (pstmt != null && !pstmt.isClosed())
                            pstmt.close();
                        if (conn != null && !conn.isClosed())
                            conn.close();
                    }
                }
            });
            return true;
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        } finally {
            if (session != null && session.isOpen()) {
                session.disconnect();
                session.close();
            }
        }
        return false;
    }*/
}
