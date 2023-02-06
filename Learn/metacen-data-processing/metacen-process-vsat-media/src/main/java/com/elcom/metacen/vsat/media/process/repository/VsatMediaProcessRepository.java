package com.elcom.metacen.vsat.media.process.repository;

import com.elcom.metacen.enums.MetacenProcessStatus;
import com.elcom.metacen.enums.ProcessType;
import com.elcom.metacen.vsat.media.process.model.kafka.VsatMediaMessageFull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.jdbc.Work;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class VsatMediaProcessRepository extends BaseRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(VsatMediaProcessRepository.class);

    @Autowired
    @Qualifier("clickHouseSession")
    SessionFactory clickHouseSessionFactory;

//    @Value("${process.vsat.media.retry.for.timeout.within}")
//    private int processVsatMediaRetryForTimeoutWithin;

//    public static final Integer MAX_LENGTH_CONTENT = 5000;

    @Autowired
    public VsatMediaProcessRepository(@Qualifier("vsatEntityManagerFactory") EntityManagerFactory factory, @Qualifier("vsatChDataSource") DataSource clickHouseDataSource) {
        super(factory, clickHouseDataSource);
    }
    
    public boolean insertVsatMediaDataLstProcess(List<VsatMediaMessageFull> vsatMediaMessages, Long processTime) {
        Session session = null;
        try {
            session = this.clickHouseSessionFactory.openSession();
            session.doWork(new Work() {
                @Override
                public void execute(Connection conn) throws SQLException {
                    PreparedStatement pstmt = null;
                    try {
                        String sql = " INSERT INTO " + getSchemaViewName() + ".vsat_media_data_analyzed "
                                + " ( uuidKey, vsatMediaUuidKey, mediaTypeId, mediaTypeName, sourceId, sourceName, sourceIp, sourcePort "
                                + " , destId, destName, destIp, destPort, filePath, fileType, fileSize "
                                + " , dataSourceId, dataSourceName, direction, dataVendor, eventTime, processTime, processStatus, processType ) "
                                + " VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) ";
                        pstmt = conn.prepareStatement(sql);
                        int i = 0;
                        for (VsatMediaMessageFull vsatMediaMessage : vsatMediaMessages) {
                            pstmt.setString(1, vsatMediaMessage.getUuidKey());
                            pstmt.setString(2, vsatMediaMessage.getMediaUuidKey());
                            pstmt.setInt(3, vsatMediaMessage.getMediaTypeId());
                            pstmt.setString(4, vsatMediaMessage.getMediaTypeName());
                            pstmt.setLong(5, vsatMediaMessage.getSourceId());
                            pstmt.setString(6, vsatMediaMessage.getSourceName());
                            pstmt.setString(7, vsatMediaMessage.getSourceIp());
                            pstmt.setInt(8, vsatMediaMessage.getSourcePort());
                            pstmt.setLong(9, vsatMediaMessage.getDestId());
                            pstmt.setString(10, vsatMediaMessage.getDestName());
                            pstmt.setString(11, vsatMediaMessage.getDestIp());
                            pstmt.setInt(12, vsatMediaMessage.getDestPort());
                            pstmt.setString(13, vsatMediaMessage.getFilePath());
                            pstmt.setString(14, vsatMediaMessage.getFileType());
                            pstmt.setInt(15, Integer.parseInt(vsatMediaMessage.getFileSize()));
                            pstmt.setLong(16, vsatMediaMessage.getDataSourceId());
                            pstmt.setString(17, vsatMediaMessage.getDataSourceName());
                            pstmt.setInt(18, vsatMediaMessage.getDirection());
                            pstmt.setString(19, vsatMediaMessage.getDataVendor());
                            pstmt.setLong(20, vsatMediaMessage.getEventTime() != null ? vsatMediaMessage.getEventTime() / 1000L : 0L);
                            pstmt.setTimestamp(21, new Timestamp(processTime));
                            pstmt.setInt(22, MetacenProcessStatus.PROCESSING.code());
                            pstmt.setString(23, ProcessType.VSAT_MEDIA_ANALYTICS.type());

                            pstmt.addBatch();

                            if( i % 50 == 0 )
                                LOGGER.info("VsatMediaProcessRepository.insertVsatMediaDataLstProcess.executeBatch return size --> [ {} ]", pstmt.executeBatch().length);

                            i++;
                        }

                        if( pstmt != null )
                            pstmt.executeBatch();
                        
                    } catch (Exception ex) {
                        LOGGER.error("ex: ", ex);
                    } finally {
                        if (pstmt != null && !pstmt.isClosed()) {
                            pstmt.close();
                        }
                        if (conn != null && !conn.isClosed()) {
                            conn.close();
                        }
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
    }

    //TODO: cần update lại SQL hàm này để lấy ra danh sách bản tin media bị xử lý timeOut
    /*public List<VsatMediaMessageFull> getLstVsatMediaTimeoutProcess() {
        Session session = null;
        try {
            session = this.clickHouseSessionFactory.openSession();
            String sql = " SELECT uuidKey AS id, vsatMediaUuidKey, mediaTypeId, mediaTypeName, sourceId, sourceName, sourceIp, sourcePort, "
                    + " destId, destName, destIp, destPort, filePath, fileType, fileSize, "
                    + " dataSourceId, dataSourceName, dataVendor, analyzedEngine, "
                    + " processType, eventTime, retryTimes AS retryNum "
                    + " FROM " + getSchemaViewName() + ".vsat_media_data_analyzed "
                    + " WHERE processStatus = :processStatus AND ingestTime < ( now() - :oldTimeToGet ) ";

            List<VsatMediaDTO> vsatMediaDTOLst = session.createNativeQuery(sql)
                    .setParameter("processStatus", MetacenProcessStatus.PROCESSING.code())
                    .setParameter("oldTimeToGet", this.processVsatMediaRetryForTimeoutWithin)
                    .addScalar("id", StringType.INSTANCE)
                    .addScalar("vsatMediaUuidKey", StringType.INSTANCE)
                    .addScalar("mediaTypeId", LongType.INSTANCE)
                    .addScalar("mediaTypeName", StringType.INSTANCE)
                    .addScalar("sourceId", LongType.INSTANCE)
                    .addScalar("sourceName", StringType.INSTANCE)
                    .addScalar("sourceIp", StringType.INSTANCE)
                    .addScalar("sourcePort", LongType.INSTANCE)
                    .addScalar("destId", LongType.INSTANCE)
                    .addScalar("destName", StringType.INSTANCE)
                    .addScalar("destIp", StringType.INSTANCE)
                    .addScalar("destPort", LongType.INSTANCE)
                    .addScalar("filePath", StringType.INSTANCE)
                    .addScalar("fileType", StringType.INSTANCE)
                    .addScalar("fileSize", LongType.INSTANCE)
                    .addScalar("dataSourceId", LongType.INSTANCE)
                    .addScalar("dataSourceName", StringType.INSTANCE)
                    .addScalar("dataVendor", StringType.INSTANCE)
                    .addScalar("analyzedEngine", StringType.INSTANCE)
                    .addScalar("processType", StringType.INSTANCE)
                    .addScalar("eventTime", TimestampType.INSTANCE)
                    .addScalar("retryNum", IntegerType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(VsatMediaDTO.class))
                    .getResultList();

            List<VsatMediaMessageFull> results = vsatMediaDTOLst.parallelStream()
                    .map(entity -> entityToVsatMediaMessage(entity))
                    .collect(Collectors.toList());

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
    }*/

    /*public boolean increaseRetryTimesForVsatMedia(String vsatMediaUuidKey) {
        Session session = null;
        try {
            session = this.clickHouseSessionFactory.openSession();
//            Transaction tx = session.beginTransaction();
            session.doWork(new Work() {
                @Override
                public void execute(Connection conn) throws SQLException {
                    PreparedStatement pstmt = null;
                    try {
                        String sql = " ALTER TABLE " + getSchemaLocalName() + ".vsat_media_data_analyzed "
                                + // + " ON CLUSTER '" + getClusterName() + "' "
                                " UPDATE retryTimes = retryTimes + 1, processStatus = ?, processTime = now() "
                                + " WHERE vsatMediaUuidKey = ? ";
                        pstmt = conn.prepareStatement(sql);
                        pstmt.setInt(1, MetacenProcessStatus.ERROR.code());
                        pstmt.setString(2, vsatMediaUuidKey);

                        LOGGER.info("VsatMediaProcessRepository.increaseRetryTimesForVsatMedia --> {}", pstmt.executeUpdate() > 0);

                    } catch (Exception ex) {
                        LOGGER.error("ex: ", ex);
                    } finally {
                        if (pstmt != null && !pstmt.isClosed()) {
                            pstmt.close();
                        }
                        if (conn != null && !conn.isClosed()) {
                            conn.close();
                        }
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

    /*public boolean updateProcessStatusForVsatMedia(int processStatus, Long processTime, String uuidKey) {
        Session session = null;
        try {
            session = this.clickHouseSessionFactory.openSession();
            session.doWork(new Work() {
                @Override
                public void execute(Connection conn) throws SQLException {
                    PreparedStatement pstmt = null;
                    try {
                        String sql = " ALTER TABLE " + getSchemaLocalName() + ".vsat_media_data_analyzed "
                                // + " ON CLUSTER '" + getClusterName() + "' "
                                + " UPDATE processStatus = ?, processTime = ? "
                                + " WHERE uuidKey = ? ";
                        pstmt = conn.prepareStatement(sql);
                        pstmt.setInt(1, processStatus);
                        pstmt.setTimestamp(2, new Timestamp(processTime));
                        pstmt.setString(3, uuidKey);
                        LOGGER.info("updateProcessStatusForVsatMedia --> {}", pstmt.executeUpdate() > 0);
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
    
    /*public boolean insertDataProcessRawStatusOld(String uuidKey, int processStatus, long eventTimeInMs) {
        Session session = null;
        try {
            session = this.clickHouseSessionFactory.openSession();
            session.doWork(new Work() {
                @Override
                public void execute(Connection conn) throws SQLException {
                    PreparedStatement pstmt = null;
                    try {
                        String sql = " INSERT INTO " + getSchemaViewName() + ".data_process_status ( recordId, processStatus, eventTime ) VALUES ( ?, ?, ? ) ";
                        pstmt = conn.prepareStatement(sql);
                        pstmt.setString(1, uuidKey);
                        pstmt.setInt(2, processStatus);
                        pstmt.setLong(3, eventTimeInMs / 1000L);

                        LOGGER.info("insertDataProcessRawStatus --> {}", pstmt.executeUpdate() > 0);

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

    /*public boolean insertDataProcessRawStatus(List<DataProcessStatusDTO> input) {
        Session session = null;
        try {
            session = this.clickHouseSessionFactory.openSession();
            session.doWork(new Work() {
                @Override
                public void execute(Connection conn) throws SQLException {
                    PreparedStatement pstmt = null;
                    try {
                        String sql = " INSERT INTO " + getSchemaViewName() + ".data_process_status ( recordId, processStatus, eventTime ) VALUES ( ?, ?, ? ) ";
                        pstmt = conn.prepareStatement(sql);
                        int i = 0;
                        for (DataProcessStatusDTO e : input) {
                            
                            pstmt.setString(1, e.getRecordId());
                            pstmt.setInt(2, e.getProcessStatus());
                            pstmt.setLong(3, e.getEventTimeInMs() / 1000L);

                            pstmt.addBatch();

                            if( i % 50 == 0 )
                                LOGGER.info("insertDataProcessRawStatus.executeBatch return size --> [ {} ]", pstmt.executeBatch().length);

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
    
    /*public boolean updateVsatMediaProcessSuccess(VsatMediaDTO vsatMediaDto) {
        Session session = null;
        try {
            session = this.clickHouseSessionFactory.openSession();
//            Transaction tx = session.beginTransaction();
            session.doWork(new Work() {
                @Override
                public void execute(Connection conn) throws SQLException {
                    PreparedStatement pstmt = null;
                    try {

                        String contentUTF8 = vsatMediaDto.getFileContentUtf8();
                        String contentGB18030 = vsatMediaDto.getFileContentGB18030();
                        String mailAttachments = vsatMediaDto.getMailAttachments();
                        String mailContents = vsatMediaDto.getMailContents();
                        String mailRaw = vsatMediaDto.getMailRaw();

                        if (!StringUtil.isNullOrEmpty(contentUTF8) && contentUTF8.length() > MAX_LENGTH_CONTENT) {
                            contentUTF8 = contentUTF8.substring(0, MAX_LENGTH_CONTENT);
                        }
                        if (!StringUtil.isNullOrEmpty(contentGB18030) && contentGB18030.length() > MAX_LENGTH_CONTENT) {
                            contentGB18030 = contentGB18030.substring(0, MAX_LENGTH_CONTENT);
                        }
                        if (!StringUtil.isNullOrEmpty(mailAttachments) && mailAttachments.length() > MAX_LENGTH_CONTENT) {
                            mailAttachments = mailAttachments.substring(0, MAX_LENGTH_CONTENT);
                        }
                        if (!StringUtil.isNullOrEmpty(mailContents) && mailContents.length() > MAX_LENGTH_CONTENT) {
                            mailContents = mailContents.substring(0, MAX_LENGTH_CONTENT);
                        }
                        if (!StringUtil.isNullOrEmpty(mailRaw) && mailRaw.length() > MAX_LENGTH_CONTENT) {
                            mailRaw = mailRaw.substring(0, MAX_LENGTH_CONTENT);
                        }

                        String sql = " ALTER TABLE " + getSchemaLocalName() + ".vsat_media_data_analyzed "
                                // + " ON CLUSTER '" + getClusterName() + "' "
                                + " UPDATE processStatus = ?, processTime = now(), "
                                + " fileContentUtf8 = ?, fileContentGB18030 = ?, "
                                + " mailFrom = ?, mailReplyTo = ?, mailTo = ?, mailAttachments = ?, mailContents = ?, mailSubject = ?, "
                                + " mailScanVirus = ?, mailScanResult = ?, mailUserAgent = ?, mailContentLanguage = ?, mailXMail = ?, mailRaw = ? "
                                + " WHERE uuidKey = ? ";
                        pstmt = conn.prepareStatement(sql);
                        pstmt.setInt(1, MetacenProcessStatus.SUCCESS.code());
                        pstmt.setString(2, contentUTF8);
                        pstmt.setString(3, contentGB18030);
                        pstmt.setString(4, vsatMediaDto.getMailFrom());
                        pstmt.setString(5, vsatMediaDto.getMailReplyTo());
                        pstmt.setString(6, vsatMediaDto.getMailTo());
                        pstmt.setString(7, mailAttachments);
                        pstmt.setString(8, mailContents);
                        pstmt.setString(9, vsatMediaDto.getMailSubject());
                        pstmt.setString(10, vsatMediaDto.getMailScanVirus());
                        pstmt.setString(11, vsatMediaDto.getMailScanResult());
                        pstmt.setString(12, vsatMediaDto.getMailUserAgent());
                        pstmt.setString(13, vsatMediaDto.getMailContentLanguage());
                        pstmt.setString(14, vsatMediaDto.getMailXMail());
                        pstmt.setString(15, mailRaw);
                        pstmt.setString(16, vsatMediaDto.getId());

                        LOGGER.info("VsatMediaProcessRepository.updateVsatMediaProcessSuccess --> {}", pstmt.executeUpdate() > 0);

                    } catch (Exception ex) {
                        LOGGER.error("ex: ", ex);
                    } finally {
                        if (pstmt != null && !pstmt.isClosed()) {
                            pstmt.close();
                        }
                        if (conn != null && !conn.isClosed()) {
                            conn.close();
                        }
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

    /*public boolean insertVsatMediaProcessedDetail(VsatMediaDTO vsatMediaDto) {
        Session session = null;
        try {
            session = this.clickHouseSessionFactory.openSession();
            session.doWork(new Work() {
                @Override
                public void execute(Connection conn) throws SQLException {
                    PreparedStatement pstmt = null;
                    try {

                        String contentUTF8 = vsatMediaDto.getFileContentUtf8();
                        String contentGB18030 = vsatMediaDto.getFileContentGB18030();
                        String mailAttachments = vsatMediaDto.getMailAttachments();
                        String mailContents = vsatMediaDto.getMailContents();
                        String mailRaw = vsatMediaDto.getMailRaw();

                        if (!StringUtil.isNullOrEmpty(contentUTF8) && contentUTF8.length() > MAX_LENGTH_CONTENT) {
                            contentUTF8 = contentUTF8.substring(0, MAX_LENGTH_CONTENT);
                        }
                        if (!StringUtil.isNullOrEmpty(contentGB18030) && contentGB18030.length() > MAX_LENGTH_CONTENT) {
                            contentGB18030 = contentGB18030.substring(0, MAX_LENGTH_CONTENT);
                        }
                        if (!StringUtil.isNullOrEmpty(mailAttachments) && mailAttachments.length() > MAX_LENGTH_CONTENT) {
                            mailAttachments = mailAttachments.substring(0, MAX_LENGTH_CONTENT);
                        }
                        if (!StringUtil.isNullOrEmpty(mailContents) && mailContents.length() > MAX_LENGTH_CONTENT) {
                            mailContents = mailContents.substring(0, MAX_LENGTH_CONTENT);
                        }
                        if (!StringUtil.isNullOrEmpty(mailRaw) && mailRaw.length() > MAX_LENGTH_CONTENT) {
                            mailRaw = mailRaw.substring(0, MAX_LENGTH_CONTENT);
                        }

                        String sql = " INSERT INTO " + getSchemaViewName() + ".vsat_media_data_analyzed_detail "
                                // + " ON CLUSTER '" + getClusterName() + "' "
                                + " ( refId, fileContentUtf8, fileContentGB18030, mailFrom, mailReplyTo, mailTo, mailAttachments, mailContents, mailSubject "
                                + " , mailScanVirus, mailScanResult, mailUserAgent, mailContentLanguage, mailXMail, mailRaw, eventTime, processStatus ) "
                                + " VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) ";

                        pstmt = conn.prepareStatement(sql);
                        pstmt.setString(1, vsatMediaDto.getVsatMediaUuidKey());
                        pstmt.setString(2, contentUTF8);
                        pstmt.setString(3, contentGB18030);
                        pstmt.setString(4, vsatMediaDto.getMailFrom());
                        pstmt.setString(5, vsatMediaDto.getMailReplyTo());
                        pstmt.setString(6, vsatMediaDto.getMailTo());
                        pstmt.setString(7, mailAttachments);
                        pstmt.setString(8, mailContents);
                        pstmt.setString(9, vsatMediaDto.getMailSubject());
                        pstmt.setString(10, vsatMediaDto.getMailScanVirus());
                        pstmt.setString(11, vsatMediaDto.getMailScanResult());
                        pstmt.setString(12, vsatMediaDto.getMailUserAgent());
                        pstmt.setString(13, vsatMediaDto.getMailContentLanguage());
                        pstmt.setString(14, vsatMediaDto.getMailXMail());
                        pstmt.setString(15, mailRaw);
                        pstmt.setTimestamp(16, new Timestamp(vsatMediaDto.getEventTime().getTime()));
                        pstmt.setInt(17, MetacenProcessStatus.SUCCESS.code());

                        LOGGER.info("VsatMediaProcessRepository.insertVsatMediaProcessedDetail --> {}", pstmt.executeUpdate() > 0);

                    } catch (Exception ex) {
                        LOGGER.error("ex: ", ex);
                    } finally {
                        if (pstmt != null && !pstmt.isClosed()) {
                            pstmt.close();
                        }
                        if (conn != null && !conn.isClosed()) {
                            conn.close();
                        }
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

    /*public boolean insertVsatMediaDataProcess(VsatMediaMessageFull vsatMediaMessage) {
        Session session = null;
        try {
            session = this.clickHouseSessionFactory.openSession();
//            Transaction tx = session.beginTransaction();
            session.doWork(new Work() {
                @Override
                public void execute(Connection conn) throws SQLException {
                    PreparedStatement pstmt = null;
                    try {
                        String sql = " INSERT INTO " + getSchemaViewName() + ".vsat_media_data_analyzed "
                                + " ( uuidKey, vsatMediaUuidKey, mediaTypeId, mediaTypeName, sourceId, sourceName, sourceIp, sourcePort "
                                + " , destId, destName, destIp, destPort, filePath, fileType, fileSize "
                                + " , dataSourceId, dataSourceName, dataVendor, eventTime, processTime, processStatus, processType ) "
                                + " VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), ?, ? ) ";
                        pstmt = conn.prepareStatement(sql);
                        pstmt.setString(1, vsatMediaMessage.getUuidKey());
                        pstmt.setString(2, vsatMediaMessage.getMediaUuidKey());
                        pstmt.setInt(3, vsatMediaMessage.getMediaTypeId());
                        pstmt.setString(4, vsatMediaMessage.getMediaTypeName());
                        pstmt.setLong(5, vsatMediaMessage.getSourceId());
                        pstmt.setString(6, vsatMediaMessage.getSourceName());
                        pstmt.setString(7, vsatMediaMessage.getSourceIp());
                        pstmt.setInt(8, vsatMediaMessage.getSourcePort());
                        pstmt.setLong(9, vsatMediaMessage.getDestId());
                        pstmt.setString(10, vsatMediaMessage.getDestName());
                        pstmt.setString(11, vsatMediaMessage.getDestIp());
                        pstmt.setInt(12, vsatMediaMessage.getDestPort());
                        pstmt.setString(13, vsatMediaMessage.getFilePath());
                        pstmt.setString(14, vsatMediaMessage.getFileType());
                        pstmt.setInt(15, Integer.parseInt(vsatMediaMessage.getFileSize()));
                        pstmt.setLong(16, vsatMediaMessage.getDataSourceId());
                        pstmt.setString(17, vsatMediaMessage.getDataSourceName());
                        pstmt.setString(18, vsatMediaMessage.getDataVendor());
                        pstmt.setLong(19, vsatMediaMessage.getEventTime() != null ? vsatMediaMessage.getEventTime() / 1000L : 0L);
                        pstmt.setInt(20, vsatMediaMessage.getProcessStatus());
                        pstmt.setString(21, ProcessType.VSAT_MEDIA_ANALYTICS.type());

                        pstmt.executeUpdate();

                    } catch (Exception ex) {
                        LOGGER.error("ex: ", ex);
                    } finally {
                        if (pstmt != null && !pstmt.isClosed()) {
                            pstmt.close();
                        }
                        if (conn != null && !conn.isClosed()) {
                            conn.close();
                        }
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

    /*private VsatMediaMessageFull entityToVsatMediaMessage(VsatMediaDTO vsatMediaDTO) {
        VsatMediaMessageFull vsatMediaMessageFull = VsatMediaMessageFull.builder()
                .uuidKey(vsatMediaDTO.getId())
                .mediaUuidKey(vsatMediaDTO.getVsatMediaUuidKey())
                .mediaTypeId(vsatMediaDTO.getMediaTypeId().intValue())
                .mediaTypeName(vsatMediaDTO.getMediaTypeName())
                .sourceId(vsatMediaDTO.getSourceId())
                .sourceName(vsatMediaDTO.getSourceName())
                .sourceIp(vsatMediaDTO.getSourceIp())
                .sourcePort(vsatMediaDTO.getSourcePort().intValue())
                .destId(vsatMediaDTO.getDestId())
                .destName(vsatMediaDTO.getDestName())
                .destIp(vsatMediaDTO.getDestIp())
                .destPort(vsatMediaDTO.getDestPort().intValue())
                .filePath(vsatMediaDTO.getFilePath())
                .fileType(vsatMediaDTO.getFileType())
                .fileSize(String.valueOf(vsatMediaDTO.getFileSize()))
                .dataSourceId(vsatMediaDTO.getDataSourceId().intValue())
                .dataSourceName(vsatMediaDTO.getDataSourceName())
                .dataVendor(vsatMediaDTO.getDataVendor())
                .analyzedEngine(vsatMediaDTO.getAnalyzedEngine())
                .processType(vsatMediaDTO.getProcessType())
                .eventTime(DateUtil.dateToLong("yyyy-MM-dd HH:mm:ss", vsatMediaDTO.getEventTime().toString()))
                .retryNum(vsatMediaDTO.getRetryNum())
                .build();

        return vsatMediaMessageFull;
    }*/
}
