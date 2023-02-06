package com.elcom.metacen.comment.repository;

import com.elcom.metacen.comment.model.Comment;
import com.elcom.metacen.comment.model.dto.CommentFilterDTO;
import com.elcom.metacen.comment.model.dto.CommentRequestDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Repository
public class CommentRepository extends BaseRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommentRepository.class);


    @Autowired
    @Qualifier("clickHouseSession")
    SessionFactory clickHouseSessionFactory;

    @Autowired
    public CommentRepository(@Qualifier("clickHouseSession") EntityManagerFactory factory, @Qualifier("chDatasource") DataSource dataSource) {
        super(factory, dataSource);
    }

    public Comment insert(CommentRequestDTO commentRequestDTO) {
        Session session = null;
        String schemaMeta = this.getSchemaMeta();
        try {
            session = this.clickHouseSessionFactory.openSession();

            session.doWork(new Work() {
                @Override
                public void execute(Connection conn) throws SQLException {
                    PreparedStatement pstmt = null;
                    try {
                        String sql = " INSERT into " + schemaMeta + ".comments (uuidKey, type, refId, content, "
                                + "contentUnsigned, createdUser, updatedUser, createdTime, updatedTime, ingestTime, isDeleted) "
                                + "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
                        pstmt = conn.prepareStatement(sql);

                        pstmt.setString(1, commentRequestDTO.getUuidKey());
                        pstmt.setInt(2, commentRequestDTO.getType());
                        pstmt.setString(3, commentRequestDTO.getRefId());
                        pstmt.setString(4, commentRequestDTO.getContent());
                        pstmt.setString(5, commentRequestDTO.getContentUnsigned());
                        pstmt.setString(6, commentRequestDTO.getCreatedUser());
                        pstmt.setString(7, commentRequestDTO.getCreatedUser());
                        pstmt.setString(8, commentRequestDTO.getCreatedTime());
                        pstmt.setString(9, commentRequestDTO.getUpdatedTime());
                        pstmt.setString(10,commentRequestDTO.getIngestTime());
                        pstmt.setInt(11, commentRequestDTO.getIsDeleted());
                        int insertStatus = pstmt.executeUpdate();
                        LOGGER.info("SQL: [ {} ], status return: [ {} ]", sql, insertStatus);
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

    public Comment findByUuidAndIsDeleted(String uuid, int isDeleted) {
        Session session = this.clickHouseSessionFactory.openSession();
        try {
            String sql = "SELECT * FROM " + this.getSchemaMeta() + ".comments WHERE uuidKey = :uuidKey AND isDeleted = :isDeleted";
            NativeQuery query = session.createNativeQuery(sql)
                    .setParameter("uuidKey", uuid)
                    .setParameter("isDeleted", isDeleted);
            query.addScalar("uuidKey", StringType.INSTANCE)
                    .addScalar("type", IntegerType.INSTANCE)
                    .addScalar("refId", StringType.INSTANCE)
                    .addScalar("content", StringType.INSTANCE)
                    .addScalar("contentUnsigned", StringType.INSTANCE)
                    .addScalar("createdUser", StringType.INSTANCE)
                    .addScalar("updatedUser", StringType.INSTANCE)
                    .addScalar("createdTime", DateType.INSTANCE)
                    .addScalar("updatedTime", DateType.INSTANCE)
                    .addScalar("ingestTime", DateType.INSTANCE)
                    .addScalar("isDeleted", IntegerType.INSTANCE)

                    .setResultTransformer(Transformers.aliasToBean(Comment.class));
            return (Comment) query.getSingleResult();
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

    public Comment update(Comment comment, CommentRequestDTO commentRequestDTO) {
        Session session = null;
        String schemaLocal = this.getSchemaMetaLocal();
        // final int[] updateStatus = {0};
        try {
            session = this.clickHouseSessionFactory.openSession();
            session.doWork(conn -> {

                PreparedStatement pstmt = null;
                try {
                    String sql = " ALTER TABLE " + schemaLocal + ".comments ON CLUSTER 'metacen_cluster' UPDATE "
                            + "type = ?,refId = ?,content = ?,contentUnsigned = ?,"
                            + "updatedUser = ?,updatedTime = ? "
                            + "where uuidKey = '" + comment.getUuidKey() + "'";

                    pstmt = conn.prepareStatement(sql);
                    int index = 1;
                    pstmt.setInt(index++, commentRequestDTO.getType());
                    pstmt.setString(index++, commentRequestDTO.getRefId());
                    pstmt.setString(index++, commentRequestDTO.getContent());
                    pstmt.setString(index++, commentRequestDTO.getContentUnsigned());
                    pstmt.setString(index++, commentRequestDTO.getUpdatedUser());
                    pstmt.setString(index++, commentRequestDTO.getUpdatedTime());
                    int updateStatus = pstmt.executeUpdate();
                    LOGGER.info("SQL: [ {} ], status return: [ {} ]", sql, updateStatus);
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
            });
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        } finally {
            if (session != null && session.isOpen()) {
                session.disconnect();
                session.close();
            }
        }
        return comment;
    }
    public Comment delete(int isDeleted, String uuid) {
        Session session = null;
        String schemaLocal = this.getSchemaMetaLocal();
        try {
            session = this.clickHouseSessionFactory.openSession();
            session.doWork(conn -> {

                PreparedStatement pstmt = null;
                try {
                    String sql = " ALTER TABLE " + schemaLocal + ".comments ON CLUSTER 'metacen_cluster' UPDATE "
                            + "isDeleted = ? "
                            + "where uuidKey = '" + uuid + "'";

                    pstmt = conn.prepareStatement(sql);
                    int index = 1;
                    pstmt.setInt(index++, isDeleted);
                    int updateStatus = pstmt.executeUpdate();
                    LOGGER.info("SQL: [ {} ], status return: [ {} ]", sql, updateStatus);
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
            });
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

    public Page<Comment> findByRefIdAndIsDeleted(CommentFilterDTO commentFilterDTO, int isDeleted) {
        Session session = this.clickHouseSessionFactory.openSession();
        String schemaMeta = this.getSchemaMeta();
        try {
            Integer page = commentFilterDTO.getPage() > 0 ? commentFilterDTO.getPage() : 0;
            Pageable pageable = PageRequest.of(page, commentFilterDTO.getSize());

            String sqlTotal = " SELECT COUNT(uuidKey) "
                    + " FROM " + schemaMeta + ".comments sid WHERE refId = :refId AND isDeleted = :isDeleted AND type = :type";

            String sql = "SELECT * FROM " + schemaMeta + ".comments WHERE refId = :refId AND isDeleted = :isDeleted AND type = :type ORDER BY createdTime DESC ";

            sql += " LIMIT :limit OFFSET :offset ";

            NativeQuery query = session.createNativeQuery(sql)
                    .setParameter("refId", commentFilterDTO.getRefId())
                    .setParameter("isDeleted", isDeleted)
                    .setParameter("type", commentFilterDTO.getType())
                    .setParameter("limit", commentFilterDTO.getSize())
                    .setParameter("offset", page * commentFilterDTO.getSize());

            NativeQuery queryTotal = session.createSQLQuery(sqlTotal)
                    .setParameter("refId", commentFilterDTO.getRefId())
                    .setParameter("isDeleted", isDeleted)
                    .setParameter("type", commentFilterDTO.getType());

            query.addScalar("uuidKey", StringType.INSTANCE)
                    .addScalar("type", IntegerType.INSTANCE)
                    .addScalar("refId", StringType.INSTANCE)
                    .addScalar("content", StringType.INSTANCE)
                    .addScalar("contentUnsigned", StringType.INSTANCE)
                    .addScalar("createdUser", StringType.INSTANCE)
                    .addScalar("updatedUser", StringType.INSTANCE)
                    .addScalar("createdTime", TimestampType.INSTANCE)
                    .addScalar("updatedTime", TimestampType.INSTANCE)
                    .addScalar("ingestTime", TimestampType.INSTANCE)
                    .addScalar("isDeleted", IntegerType.INSTANCE)

                    .setResultTransformer(Transformers.aliasToBean(Comment.class));
            List<Comment> results = query.getResultList();
            return new PageImpl<>(results, pageable, ((Number) queryTotal.getSingleResult()).longValue());
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

    public List<Comment> findByRefId(String refId, int isDeleted) {
        Session session = this.clickHouseSessionFactory.openSession();
        try {
            String sql = "SELECT * FROM " + this.getSchemaMeta() + ".comments WHERE refId = :refId AND isDeleted = :isDeleted ";
            NativeQuery query = session.createNativeQuery(sql)
                    .setParameter("refId", refId)
                    .setParameter("isDeleted", isDeleted);
            query.addScalar("uuidKey", StringType.INSTANCE)
                    .addScalar("type", IntegerType.INSTANCE)
                    .addScalar("refId", StringType.INSTANCE)
                    .addScalar("content", StringType.INSTANCE)
                    .addScalar("contentUnsigned", StringType.INSTANCE)
                    .addScalar("createdUser", StringType.INSTANCE)
                    .addScalar("updatedUser", StringType.INSTANCE)
                    .addScalar("createdTime", DateType.INSTANCE)
                    .addScalar("updatedTime", DateType.INSTANCE)
                    .addScalar("ingestTime", DateType.INSTANCE)
                    .addScalar("isDeleted", IntegerType.INSTANCE)

                    .setResultTransformer(Transformers.aliasToBean(Comment.class));
            List<Comment> result = query.getResultList();
            return result;
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
