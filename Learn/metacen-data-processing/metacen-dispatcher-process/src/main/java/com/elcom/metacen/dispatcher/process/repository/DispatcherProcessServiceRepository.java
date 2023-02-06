package com.elcom.metacen.dispatcher.process.repository;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class DispatcherProcessServiceRepository extends BaseRepository {
    
//    private static final Logger LOGGER = LoggerFactory.getLogger(DispatcherProcessServiceRepository.class);

    @Autowired
    @Qualifier("clickHouseSession")
    SessionFactory clickHouseSessionFactory;
    
    @Autowired
    public DispatcherProcessServiceRepository(@Qualifier("vsatEntityManagerFactory") EntityManagerFactory factory, @Qualifier("vsatChDataSource") DataSource clickHouseDataSource) {
        super(factory, clickHouseDataSource);
    }
    
    /*public boolean updateProcessStatus(int processStatus, String satelliteImageUuidKey, String tableName) {
        Session session = null;
        try {
            session = this.clickHouseSessionFactory.openSession();
//            Transaction tx = session.beginTransaction();
            session.doWork(new Work() {
                @Override
                public void execute(Connection conn) throws SQLException {
                    PreparedStatement pstmt = null;
                    try {
                        String sql = " ALTER TABLE " + getSchemaLocalName() + "." + tableName +
                                     // " ON CLUSTER '" + getClusterName() + "' " +
                                     " UPDATE processStatus = ?, processTime = now() " +
                                     " WHERE uuidKey = ? ";
                        pstmt = conn.prepareStatement(sql);
                        pstmt.setInt(1, processStatus);
                        pstmt.setString(2, satelliteImageUuidKey);
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
//            tx.commit();
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
    
    /*public boolean insertSatelliteImageDataProcess(SatelliteImageMessageFull satelliteImage) {
        Session session = null;
        try {
            session = this.clickHouseSessionFactory.openSession();
//            Transaction tx = session.beginTransaction();
            session.doWork(new Work() {
                @Override
                public void execute(Connection conn) throws SQLException {
                    PreparedStatement pstmt = null;
                    try {
                        String sql = " INSERT INTO " + getSchemaViewName() + ".satellite_image_data_analyzed " +
                                     " ( uuidKey, satelliteName, missionId, productLevel, baseLineNumber, relativeOrbitNumber, tileNumber " +
                                     " , originLongitude, originLatitude, cornerLongitude, cornerLatitude, rootDataFolderPath " +
                                     " , geoWmsUrl, geoWorkSpace, geoLayerName, captureTime, secondTime, processTime, processStatus, dataVendor ) " +
                                     " VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), ?, ? ) ";
                        pstmt = conn.prepareStatement(sql);
                        pstmt.setString(1, satelliteImage.getUuidKey());
                        pstmt.setString(2, satelliteImage.getSatelliteName());
                        pstmt.setString(3, satelliteImage.getMissionId());
                        pstmt.setString(4, satelliteImage.getProductLevel());
                        pstmt.setString(5, satelliteImage.getBaseLineNumber());
                        pstmt.setString(6, satelliteImage.getRelativeOrbitNumber());
                        pstmt.setString(7, satelliteImage.getTileNumber());
                        pstmt.setFloat(8, satelliteImage.getOriginLongitude());
                        pstmt.setFloat(9, satelliteImage.getOriginLatitude() );
                        pstmt.setFloat(10, satelliteImage.getCornerLongitude());
                        pstmt.setFloat(11, satelliteImage.getCornerLatitude());
                        pstmt.setString(12, satelliteImage.getRootDataFolderPath());
                        pstmt.setString(13, satelliteImage.getGeoWmsUrl());
                        pstmt.setString(14, satelliteImage.getGeoWorkSpace());
                        pstmt.setString(15,satelliteImage.getGeoLayerName() );
                        pstmt.setLong(16, satelliteImage.getCaptureTime() / 1000L);
                        pstmt.setLong(17, satelliteImage.getSecondTime() != null ? satelliteImage.getSecondTime() / 1000L : 0L);
                        pstmt.setInt(18, MetacenProcessStatus.NOT_PROCESS.code());
                        pstmt.setString(19, satelliteImage.getDataVendor());
                        
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
//            tx.commit();
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
    
    /*public boolean insertVsatMediaDataProcess(VsatMediaMessageFull vsatMediaMessageFull) {
        Session session = null;
        try {
            session = this.clickHouseSessionFactory.openSession();
//            Transaction tx = session.beginTransaction();
            session.doWork(new Work() {
                @Override
                public void execute(Connection conn) throws SQLException {
                    PreparedStatement pstmt = null;
                    try {
                        
                        String sql = " INSERT INTO " + getSchemaViewName() + ".vsat_media_analyzed " +
                                     " ( vsatMediaUuidKey, mediaTypeId, mediaTypeName, sourceId, sourceName, sourceIp, sourcePort " +
                                     " , destId, destName, destIp, destPort, filePath, fileType, fileSize " +
                                     " , dataSourceId, dataSourceName, dataVendor, eventTime, processTime, processStatus ) " +
                                     " VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), ? ) ";
                        pstmt = conn.prepareStatement(sql);
                        pstmt.setString(1, vsatMediaMessageFull.getUuidKey());
                        pstmt.setInt(2, vsatMediaMessageFull.getMediaTypeId());
                        pstmt.setString(3, vsatMediaMessageFull.getMediaTypeName());
                        pstmt.setLong(4, vsatMediaMessageFull.getSourceId());
                        pstmt.setString(5, vsatMediaMessageFull.getSourceName());
                        pstmt.setString(6, vsatMediaMessageFull.getSourceIp());
                        pstmt.setInt(7, vsatMediaMessageFull.getSourcePort());
                        pstmt.setLong(8, vsatMediaMessageFull.getDestId());
                        pstmt.setString(9, vsatMediaMessageFull.getDestName());
                        pstmt.setString(10, vsatMediaMessageFull.getDestIp());
                        pstmt.setInt(11, vsatMediaMessageFull.getDestPort());
                        pstmt.setString(12, vsatMediaMessageFull.getFilePath());
                        pstmt.setString(13, vsatMediaMessageFull.getFileType());
                        pstmt.setString(14, vsatMediaMessageFull.getFileSize());
                        pstmt.setLong(15,vsatMediaMessageFull.getDataSourceId());
                        pstmt.setString(16, vsatMediaMessageFull.getDataSourceName());
                        pstmt.setString(17, vsatMediaMessageFull.getDataVendor());
                        pstmt.setLong(18, vsatMediaMessageFull.getEventTime() / 1000L);
                        pstmt.setInt(19, MetacenProcessStatus.NOT_PROCESS.code());
                        
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
//            tx.commit();
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
    
    /*public boolean insertLoggingProcess(String refUuidKey, String processType, int processStatus) {
        Session session = null;
        try {
            session = this.clickHouseSessionFactory.openSession();
//            Transaction tx = session.beginTransaction();
            session.doWork(new Work() {
                @Override
                public void execute(Connection conn) throws SQLException {
                    PreparedStatement pstmt = null;
                    try {
                        String sql = " INSERT INTO " + getSchemaViewName() + ".data_analyzed " +
                                     " ( refUuidKey, processType, processStatus ) " +
                                     " VALUES ( ?, ?, ? ) ";
                        pstmt = conn.prepareStatement(sql);
                        pstmt.setString(1, refUuidKey);
                        pstmt.setString(2, processType);
                        pstmt.setInt(3, processStatus);
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
//            tx.commit();
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
