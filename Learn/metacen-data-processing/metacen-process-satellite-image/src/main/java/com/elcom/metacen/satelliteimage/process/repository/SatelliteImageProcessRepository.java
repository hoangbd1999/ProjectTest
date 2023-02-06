package com.elcom.metacen.satelliteimage.process.repository;

import com.elcom.metacen.enums.MetacenProcessStatus;
import com.elcom.metacen.satelliteimage.process.model.kafka.consumer.SatelliteImageRawMessageFull;
import com.elcom.metacen.satelliteimage.process.model.kafka.producer.SatelliteImageRawMessageSimplify;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.jdbc.Work;
import org.hibernate.transform.Transformers;
import org.hibernate.type.DoubleType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class SatelliteImageProcessRepository extends BaseRepository {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SatelliteImageProcessRepository.class);

    @Autowired
    @Qualifier("clickHouseSession")
    SessionFactory clickHouseSessionFactory;
    
    @Value("${process.satellite.image.retry.for.timeout.within}")
    private int processSatelliteImageRetryForTimeoutWithin;
    
    public static void main(String[] args) {
        
        int aa = tryMmsi0("93942221211").intValue();
        
        System.out.println("s: " + aa);
        
    }
    
    public static BigInteger tryMmsi0(String mmsi){
        try {
            return new BigInteger(mmsi);
        }catch (Exception e){
            return new BigInteger("0");
        }
    }
    
    public static long tryMmsi(String mmsi){
        try {
            return Long.parseLong(mmsi);
        }catch (Exception e){
            return 0;
        }
    }
    
    @Autowired
    public SatelliteImageProcessRepository(@Qualifier("vsatEntityManagerFactory") EntityManagerFactory factory, @Qualifier("vsatChDataSource") DataSource clickHouseDataSource) {
        super(factory, clickHouseDataSource);
    }
    
    public List<SatelliteImageRawMessageSimplify> getLstSatelliteImageTimeoutProcess() {
        Session session = null;
        try {
            session = this.clickHouseSessionFactory.openSession();
            String sql = " SELECT uuidKey AS uuidKey, rootDataFolderPath AS rootDataFolderPath, originLongitude AS originLongitude, originLatitude AS originLatitude, cornerLongitude AS cornerLongitude, cornerLatitude AS cornerLatitude, retryTimes AS retryNum " +
                         " FROM " + getSchemaViewName() + ".satellite_image_data_analyzed " +
                         " WHERE processStatus = :processStatus AND ingestTime < ( now() - :oldTimeToGet) "; 
            return session.createNativeQuery(sql)
                    .setParameter("processStatus", MetacenProcessStatus.PROCESSING.code())
                    .setParameter("oldTimeToGet", this.processSatelliteImageRetryForTimeoutWithin)
                    .addScalar("uuidKey", StringType.INSTANCE)
                    .addScalar("rootDataFolderPath", StringType.INSTANCE)
                    .addScalar("originLongitude", DoubleType.INSTANCE)
                    .addScalar("originLatitude", DoubleType.INSTANCE)
                    .addScalar("cornerLongitude", DoubleType.INSTANCE)
                    .addScalar("cornerLatitude", DoubleType.INSTANCE)
                    .addScalar("retryNum", IntegerType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(SatelliteImageRawMessageSimplify.class))
                    .getResultList();
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        } finally {
            if ( session != null && session.isOpen() ) {
                session.disconnect();
                session.close();
            }
        }
        return null;
    }
    
    public boolean updateSatelliteImageRawProcessStatus(int processStatus, String satelliteImageRawUuidKey) {
        Session session = null;
        try {
            session = this.clickHouseSessionFactory.openSession();
//            Transaction tx = session.beginTransaction();
            session.doWork(new Work() {
                @Override
                public void execute(Connection conn) throws SQLException {
                    PreparedStatement pstmt = null;
                    try {
                        String sql = " ALTER TABLE " + getSchemaLocalName() + ".satellite_image_data_analyzed " +
                                     // " ON CLUSTER '" + getClusterName() + "' " +
                                     " UPDATE processStatus = ?, processTime = now() " +
                                     " WHERE uuidKey = ? ";
                        
                        pstmt = conn.prepareStatement(sql);
                        pstmt.setInt(1, processStatus);
                        pstmt.setString(2, satelliteImageRawUuidKey);
                        
                        LOGGER.info("SatelliteImageProcessRepository.updateSatelliteImageRawProcessStatus --> {}", pstmt.executeUpdate() > 0);
                        
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
    }
    
    public boolean updateSatelliteImageCompareProcessStatus(int processStatus, String satelliteImageCompareUuidKey) {
        Session session = null;
        try {
            session = this.clickHouseSessionFactory.openSession();
//            Transaction tx = session.beginTransaction();
            session.doWork(new Work() {
                @Override
                public void execute(Connection conn) throws SQLException {
                    PreparedStatement pstmt = null;
                    try {
                        String sql = " ALTER TABLE " + getSchemaLocalName() + ".satellite_image_changes " +
                                     // " ON CLUSTER '" + getClusterName() + "' " +
                                     " UPDATE processStatus = ?, timeReceiveResult = now() " +
                                     " WHERE uuidKey = ? ";
                        
                        pstmt = conn.prepareStatement(sql);
                        pstmt.setInt(1, processStatus);
                        pstmt.setString(2, satelliteImageCompareUuidKey);
                        
                        LOGGER.info("SatelliteImageProcessRepository.updateSatelliteImageCompareProcessStatus --> {}", pstmt.executeUpdate() > 0);
                        
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
    }
    
    public boolean increaseRetryTimesForSatelliteImage(String satelliteImageUuidKey) {
        Session session = null;
        try {
            session = this.clickHouseSessionFactory.openSession();
//            Transaction tx = session.beginTransaction();
            session.doWork(new Work() {
                @Override
                public void execute(Connection conn) throws SQLException {
                    PreparedStatement pstmt = null;
                    try {
                        String sql = " ALTER TABLE " + getSchemaLocalName() + ".satellite_image_data_analyzed " +
                                     // " ON CLUSTER '" + getClusterName() + "' " +
                                     " UPDATE retryTimes = retryTimes + 1, processStatus = ?, processTime = now() " +
                                     " WHERE uuidKey = ? ";
                        pstmt = conn.prepareStatement(sql);
                        pstmt.setInt(1, MetacenProcessStatus.ERROR.code());
                        pstmt.setString(2, satelliteImageUuidKey);
                        
                        LOGGER.info("SatelliteImageProcessRepository.increaseRetryTimesForSatelliteImage --> {}", pstmt.executeUpdate() > 0);
                        
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
    }
    
    public boolean insertSatelliteImageDataProcess(SatelliteImageRawMessageFull satelliteImage) {
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
                                     " , geoWmsUrl, geoWorkSpace, geoLayerName, captureTime, secondTime, processTime, processStatus, retryTimes, dataVendor ) " +
                                     " VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), ?, ?, ? ) ";
                        pstmt = conn.prepareStatement(sql);
                        pstmt.setString(1, satelliteImage.getUuidKey());
                        pstmt.setString(2, satelliteImage.getSatelliteName());
                        pstmt.setString(3, satelliteImage.getMissionId());
                        pstmt.setString(4, satelliteImage.getProductLevel());
                        pstmt.setString(5, satelliteImage.getBaseLineNumber());
                        pstmt.setString(6, satelliteImage.getRelativeOrbitNumber());
                        pstmt.setString(7, satelliteImage.getTileNumber());
                        pstmt.setDouble(8, satelliteImage.getOriginLongitude());
                        pstmt.setDouble(9, satelliteImage.getOriginLatitude() );
                        pstmt.setDouble(10, satelliteImage.getCornerLongitude());
                        pstmt.setDouble(11, satelliteImage.getCornerLatitude());
                        pstmt.setString(12, satelliteImage.getRootDataFolderPath());
                        pstmt.setString(13, satelliteImage.getGeoWmsUrl());
                        pstmt.setString(14, satelliteImage.getGeoWorkSpace());
                        pstmt.setString(15,satelliteImage.getGeoLayerName() );
                        pstmt.setLong(16, satelliteImage.getCaptureTime() / 1000L);
                        pstmt.setLong(17, satelliteImage.getSecondTime() != null ? satelliteImage.getSecondTime() / 1000L : 0L);
                        pstmt.setInt(18, satelliteImage.getProcessStatus());
                        pstmt.setInt(19, satelliteImage.getRetryNum());
                        pstmt.setString(20, satelliteImage.getDataVendor());
                        
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
    }
}
