package com.elcom.metacen.report.repository;

import com.elcom.metacen.report.model.DataAnalyzed;
import com.elcom.metacen.report.model.dto.DataAnalyzedFilterDTO;
import com.elcom.metacen.report.model.dto.DataAnalyzedRequestDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import com.elcom.metacen.report.model.dto.DataAnalyzedRequestReportDTO;
import com.elcom.metacen.utils.StringUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.jdbc.Work;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.hibernate.type.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Repository
public class DataAnalyzedRepository extends BaseRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataAnalyzedRepository.class);
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final long GMT_TIME_SUBTRACT = 7L * 60L * 60L * 1000L;
    
    @Autowired
    @Qualifier("clickHouseSession")
    SessionFactory clickHouseSessionFactory;

    @Autowired
    public DataAnalyzedRepository(@Qualifier("clickHouseSession") EntityManagerFactory factory, @Qualifier("chDatasource") DataSource dataSource) {
        super(factory, dataSource);
    }

    public DataAnalyzed insertLoggingProcess(DataAnalyzedRequestReportDTO dataAnalyzedRequestReportDTO) {
        Session session = null;
        String schemaMeta = this.getSchemaMeta();
        try {
            session = this.clickHouseSessionFactory.openSession();
            session.doWork(new Work() {
                @Override
                public void execute(Connection conn) throws SQLException {
                    PreparedStatement pstmt = null;
                    try {
                        String sql = " INSERT INTO " + schemaMeta + ".data_analyzed " +
                                     " ( refUuidKey, processType, eventTime, processTime, processStatus ) " +
                                     " VALUES ( ?, ?, ?, ?, ? ) ";
                        pstmt = conn.prepareStatement(sql);
                        pstmt.setString(1, dataAnalyzedRequestReportDTO.getRefUuidKey());
                        pstmt.setString(2, dataAnalyzedRequestReportDTO.getProcessType());
                        pstmt.setTimestamp(3, new Timestamp( dataAnalyzedRequestReportDTO.getEventTime() != null && !dataAnalyzedRequestReportDTO.getEventTime().equals(0L)
                                           ? dataAnalyzedRequestReportDTO.getEventTime() : new Date().getTime() - GMT_TIME_SUBTRACT ));
                        pstmt.setTimestamp(4, new Timestamp(dataAnalyzedRequestReportDTO.getProcessTime()));
                        pstmt.setInt(5, dataAnalyzedRequestReportDTO.getProcessStatus());
                        pstmt.executeUpdate();
                    } catch (Exception ex) {
                        LOGGER.error("ex: ", ex);
                    } finally {
                        if( pstmt != null && !pstmt.isClosed() )
                            pstmt.close();
                        if( conn != null && !conn.isClosed() )
                            conn.close();
                    }
                }
            });
            return null;
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        } finally {
            if (session != null && session.isOpen()) {
                session.disconnect();
                session.close();
            }
        }
        return null;
    }

    public List<DataAnalyzedRequestDTO> filterReport(DataAnalyzedFilterDTO req) {
        Session session = this.clickHouseSessionFactory.openSession();
        String schemaMeta = this.getSchemaMeta();
        try {
            String fromTime = req.getFromTime().trim();
            String toTime = req.getToTime().trim();

            String condition = "";

            if (req.getProcessTypeLst() != null && !req.getProcessTypeLst().isEmpty()) {
                condition += " AND processType IN :processTypeLst ";
            }
            if (!StringUtil.isNullOrEmpty(req.getFromTime()) && !StringUtil.isNullOrEmpty(req.getToTime())) {
                condition = " (processTime BETWEEN :fromTime AND :toTime) " + condition;
            } else {
                condition = " uuidKey is not null " + condition;
            }

            String sql = " SELECT refUuidKey, processType, MAX(processTime) as timeProcess, MAX(processStatus) as processStatus "
                    + " FROM " + schemaMeta + ".data_analyzed WHERE "
                    + condition + " GROUP BY processType, refUuidKey";

            NativeQuery query = session.createNativeQuery(sql);
            if (!StringUtil.isNullOrEmpty(req.getFromTime()) && !StringUtil.isNullOrEmpty(req.getToTime())) {
                query.setParameter("fromTime", fromTime);
                query.setParameter("toTime", toTime);
            }

            if (req.getProcessTypeLst() != null && !req.getProcessTypeLst().isEmpty()) {
                query.setParameter("processTypeLst", req.getProcessTypeLst());
            }
            query.addScalar("refUuidKey", StringType.INSTANCE)
                    .addScalar("processType", StringType.INSTANCE)
                    .addScalar("timeProcess", TimestampType.INSTANCE)
                    .addScalar("processStatus", IntegerType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(DataAnalyzedRequestDTO.class));

            List<DataAnalyzedRequestDTO> results = query.getResultList();
            return results;
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        } finally {
            if (session != null && session.isOpen()) {
                session.disconnect();
                session.close();
            }
        }

        return null;
    }
}
