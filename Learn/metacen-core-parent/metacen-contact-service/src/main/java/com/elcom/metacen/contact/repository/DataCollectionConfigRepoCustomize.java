package com.elcom.metacen.contact.repository;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManagerFactory;
import java.sql.PreparedStatement;

@Repository
public class DataCollectionConfigRepoCustomize extends BaseRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataCollectionConfigRepoCustomize.class);

    protected DataCollectionConfigRepoCustomize(EntityManagerFactory factory) {
        super(factory);
    }

    public boolean updateConfigValue(String collectType, String config_value) {
        Session session = null;
        try {
            session = this.sessionFactory.openSession();
            session.doWork(conn -> {
                PreparedStatement pstmt = null;
                try {
                    String sql = " UPDATE metacen_contact.data_collector_config set config_value = ?::JSON " +
                            "where collect_type = ?";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, config_value);
                    pstmt.setString(2, collectType);
                    int insertStatus = pstmt.executeUpdate();
                    LOGGER.info("SQL: [ {} ], status return: [ {} ]", sql, insertStatus);
                } catch (Exception ex) {
                    LOGGER.error("ex: ", ex);
                } finally {
                    if (pstmt != null && !pstmt.isClosed())
                        pstmt.close();
                    if (conn != null && !conn.isClosed())
                        conn.close();
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
    }

    public boolean updateIsRunning(String collectType, boolean isRunning) {
        Session session = null;
        try {
            session = this.sessionFactory.openSession();
            session.doWork(conn -> {
                PreparedStatement pstmt = null;
                try {
                    String sql = " UPDATE metacen_contact.data_collector_config set is_running_process = ? " +
                            "where collect_type = ?";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setBoolean(1, isRunning);
                    pstmt.setString(2, collectType);
                    int insertStatus = pstmt.executeUpdate();
                    LOGGER.info("SQL: [ {} ], status return: [ {} ]", sql, insertStatus);
                } catch (Exception ex) {
                    LOGGER.error("ex: ", ex);
                } finally {
                    if (pstmt != null && !pstmt.isClosed())
                        pstmt.close();
                    if (conn != null && !conn.isClosed())
                        conn.close();
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
    }
}






