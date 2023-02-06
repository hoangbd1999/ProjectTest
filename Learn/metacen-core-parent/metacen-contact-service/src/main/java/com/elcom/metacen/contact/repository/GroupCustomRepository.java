package com.elcom.metacen.contact.repository;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManagerFactory;

@Repository
public class GroupCustomRepository extends BaseRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupCustomRepository.class);

    protected GroupCustomRepository(EntityManagerFactory factory) {
        super(factory);
    }

    @Autowired
    protected ModelMapper modelMapper;

//    public GroupResponseListDTO findAll(GroupFilterDTO groupFilterDTO) {
//        GroupResponseListDTO groupResponseListDTO = new GroupResponseListDTO();
//        Session session = openSession();
//        try {
//            String prefix = "select id, name, note," +
//                    " side_id sideId ";
//            String sql
//                    = " from metacen_contact.group WHERE is_deleted = :is_deleted ";
//            if (!StringUtil.isNullOrEmpty(groupFilterDTO.getName())) {
//                sql += " and name LIKE :name ";
//            }
//            if (!StringUtil.isNullOrEmpty(groupFilterDTO.getNote())) {
//                sql += " and note LIKE :note ";
//            }
//            if (groupFilterDTO.getSideId() != null) {
//                sql += " and side_id LIKE :side_id ";
//            }
//            long offSet = groupFilterDTO.getPage() * groupFilterDTO.getSize();
//            long limit = groupFilterDTO.getSize();
//            String sqlFinal =  " order by created_time desc offset "+offSet +" limit "+limit;
//            String sqlNormal = prefix + sql + sqlFinal;
//            NativeQuery query = session.createNativeQuery(sqlNormal)
//                    .setParameter("is_deleted", DataDeleteStatus.NOT_DELETED.code());
//            if (!StringUtil.isNullOrEmpty(groupFilterDTO.getName())) {
//                String keyword = StringUtil.replaceSpecialSQLCharacter(groupFilterDTO.getName());
//                query.setParameter("name", "%" + keyword + "%");
//            }
//            if (!StringUtil.isNullOrEmpty(groupFilterDTO.getNote())) {
//                String keyword = StringUtil.replaceSpecialSQLCharacter(groupFilterDTO.getNote());
//                query.setParameter("note", "%" + keyword + "%");
//            }
//            if (groupFilterDTO.getSideId()!=null) {
//                String keyword = StringUtil.replaceSpecialSQLCharacter(groupFilterDTO.getSideId().toString());
//                query.setParameter("side_id", "%" + keyword + "%");
//            }
//            query.addScalar("id", PostgresUUIDType.INSTANCE)
//                    .addScalar("name", StringType.INSTANCE)
//                    .addScalar("note", StringType.INSTANCE)
//                    .addScalar("sideId", PostgresUUIDType.INSTANCE)
//                    .setResultTransformer(Transformers.aliasToBean(GroupDTO.class));
//            List<GroupDTO> result = query.getResultList();
//            groupResponseListDTO.setList(result);
//            groupResponseListDTO.setTotal((long)result.size());
//            return groupResponseListDTO;
//        } catch (Exception ex) {
//            LOGGER.error("ex: ", ex);
//        } finally {
//            closeSession(session);
//        }
//        return null;
//    }


//    public List<GroupDTO> findAll(GroupFilterDTO groupFilterDTO) {
//        Session session = openSession();
//        try {
//            String prefix = "select id, name, note," +
//                    " side_id sideId ";
//            String sql
//                    = " from metacen_contact.group WHERE is_deleted = :is_deleted ";
//            if (!StringUtil.isNullOrEmpty(groupFilterDTO.getName())) {
//                sql += " and name LIKE :name ";
//            }
//            if (!StringUtil.isNullOrEmpty(groupFilterDTO.getNote())) {
//                sql += " and note LIKE :note ";
//            }
//            if (groupFilterDTO.getSideId() != null) {
//                sql += " and side_id LIKE :side_id ";
//            }
//            long offSet = groupFilterDTO.getPage() * groupFilterDTO.getSize();
//            long limit = groupFilterDTO.getSize();
//            String sqlFinal =  " order by created_time desc offset "+offSet +" limit "+limit;
//            String sqlNormal = prefix + sql + sqlFinal;
//            NativeQuery query = session.createNativeQuery(sqlNormal)
//                    .setParameter("is_deleted", DataDeleteStatus.NOT_DELETED.code());
//            if (!StringUtil.isNullOrEmpty(groupFilterDTO.getName())) {
//                String keyword = StringUtil.replaceSpecialSQLCharacter(groupFilterDTO.getName());
//                query.setParameter("name", "%" + keyword + "%");
//            }
//            if (!StringUtil.isNullOrEmpty(groupFilterDTO.getNote())) {
//                String keyword = StringUtil.replaceSpecialSQLCharacter(groupFilterDTO.getNote());
//                query.setParameter("note", "%" + keyword + "%");
//            }
//            if (groupFilterDTO.getSideId()!=null) {
//                String keyword = StringUtil.replaceSpecialSQLCharacter(groupFilterDTO.getSideId().toString());
//                query.setParameter("side_id", "%" + keyword + "%");
//            }
//            query.addScalar("id", PostgresUUIDType.INSTANCE)
//                    .addScalar("name", StringType.INSTANCE)
//                    .addScalar("note", StringType.INSTANCE)
//                    .addScalar("sideId", PostgresUUIDType.INSTANCE)
//                    .setResultTransformer(Transformers.aliasToBean(GroupDTO.class));
//            List<GroupDTO> result = query.getResultList();
//            return result;
//        } catch (Exception ex) {
//            LOGGER.error("ex: ", ex);
//        } finally {
//            closeSession(session);
//        }
//        return null;
//    }
}

