package elcom.com.neo4j.repositorymogo;

import elcom.com.neo4j.model.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ConfigObjectRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigObjectRepository.class);

    private final SessionFactory sessionFactory;

    @Autowired
    public ConfigObjectRepository(EntityManagerFactory factory) {
        if (factory.unwrap(SessionFactory.class) == null) {
            throw new NullPointerException("factory is not a hibernate factory");
        }
        this.sessionFactory = factory.unwrap(SessionFactory.class);
    }

    private Session openSession() {
        Session session = this.sessionFactory.openSession();
        return session;
    }

    private void closeSession(Session session) {
        if (session.isOpen()) {
            session.disconnect();
            session.close();
        }
    }



    public List<AisInfo> getAisInfo() {
        Session session = openSession();
        try {
            StringBuilder stringBuilder = new StringBuilder("");
            //List
            stringBuilder.append("select mmsi,name,source_ip"
                    + " FROM ais_info  WHERE is_deleted = 0");
            List<AisInfo> result = session.createNativeQuery(stringBuilder.toString(),AisInfo.class).getResultList();
//            List<AisInfo> tmp= result.stream().map(item ->  new AisInfo(Long.valueOf(item[0].toString()),item[1].toString())).collect(Collectors.toList());
            return result;
        } catch (Exception ex) {
            LOGGER.error("findAll().ex: " + ex.toString());
            ex.printStackTrace();
        } finally {
            closeSession(session);
        }
        return null;
    }

    public List<AisInfo> getAisInfo(Integer mmsi) {
        Session session = openSession();
        try {
            StringBuilder stringBuilder = new StringBuilder("");
            //List
            stringBuilder.append("select mmsi,name,source_ip"
                    + " FROM ais_info  WHERE is_deleted = 0 and mmsi=").append(mmsi);
            List<AisInfo> result = session.createNativeQuery(stringBuilder.toString(),AisInfo.class).getResultList();
//            List<AisInfo> tmp= result.stream().map(item ->  new AisInfo(Long.valueOf(item[0].toString()),item[1].toString())).collect(Collectors.toList());
            return result;
        } catch (Exception ex) {
            LOGGER.error("findAll().ex: " + ex.toString());
            ex.printStackTrace();
        } finally {
            closeSession(session);
        }
        return null;
    }

    public List<MmsiIp> getMmsiIp() {
        Session session = openSession();
        try {
            StringBuilder stringBuilder = new StringBuilder("");
            //List
            stringBuilder.append("select mmsi,type,ip_address,data_source"
                    + " FROM mmsi_ip  WHERE is_deleted = 0 ");
            List<Object[]> result = session.createNativeQuery(stringBuilder.toString()).getResultList();
            List<MmsiIp> tmp = result.stream().map(item -> {
                MmsiIp mmsiIp = new MmsiIp();
                mmsiIp.setMmsi(Long.valueOf(item[0].toString()));
                mmsiIp.setDataSource(Long.valueOf(item[3].toString()));
                mmsiIp.setType(Integer.valueOf(item[1].toString()));
                mmsiIp.setIpAddress(item[2].toString());
                return mmsiIp;
            }).collect(Collectors.toList());
            return tmp;
        } catch (Exception ex) {
            LOGGER.error("findAll().ex: " + ex.toString());
            ex.printStackTrace();
        } finally {
            closeSession(session);
        }
        return null;
    }

    public List<MmsiIp> getMmsiIp(Integer mmsi) {
        Session session = openSession();
        try {
            StringBuilder stringBuilder = new StringBuilder("");
            //List
            stringBuilder.append("select mmsi,type,ip_address,data_source"
                    + " FROM mmsi_ip  WHERE is_deleted = 0 and mmsi= ").append(mmsi);
            List<Object[]> result = session.createNativeQuery(stringBuilder.toString()).getResultList();
            List<MmsiIp> tmp = result.stream().map(item -> {
                MmsiIp mmsiIp = new MmsiIp();
                mmsiIp.setMmsi(Long.valueOf(item[0].toString()));
                mmsiIp.setDataSource(Long.valueOf(item[3].toString()));
                mmsiIp.setType(Integer.valueOf(item[1].toString()));
                mmsiIp.setIpAddress(item[2].toString());
                return mmsiIp;
            }).collect(Collectors.toList());
            return tmp;
        } catch (Exception ex) {
            LOGGER.error("findAll().ex: " + ex.toString());
            ex.printStackTrace();
        } finally {
            closeSession(session);
        }
        return null;
    }



    public List<Headquarters> getHeadquarters() {
        Session session = openSession();
        try {
            StringBuilder stringBuilder = new StringBuilder("");
            //List
            stringBuilder.append("select id,name,longitude,latitude "
                    + " FROM headquarters  WHERE is_deleted = 0 ");
            List<Headquarters> result = session.createNativeQuery(stringBuilder.toString(),Headquarters.class).getResultList();
//            List<Headquarters> tmp = result.parallelStream().map(item -> {
//                return new Headquarters(Long.valueOf(item[0].toString()),item[1].toString(),Double.valueOf(item[0].toString()),Double.valueOf(item[3].toString()));
//            }).collect(Collectors.toList());
            return result;
        } catch (Exception ex) {
            LOGGER.error("findAll().ex: " + ex.toString());
            ex.printStackTrace();
        } finally {
            closeSession(session);
        }
        return null;
    }

    public List<Headquarters> getHeadquarters(Integer mmsi) {
        Session session = openSession();
        try {
            StringBuilder stringBuilder = new StringBuilder("");
            //List
            stringBuilder.append("select id,name,longitude,latitude "
                    + " FROM headquarters  WHERE is_deleted = 0 and id=").append(mmsi);
            List<Headquarters> result = session.createNativeQuery(stringBuilder.toString(),Headquarters.class).getResultList();
//            List<Headquarters> tmp = result.parallelStream().map(item -> {
//                return new Headquarters(Long.valueOf(item[0].toString()),item[1].toString(),Double.valueOf(item[0].toString()),Double.valueOf(item[3].toString()));
//            }).collect(Collectors.toList());
            return result;
        } catch (Exception ex) {
            LOGGER.error("findAll().ex: " + ex.toString());
            ex.printStackTrace();
        } finally {
            closeSession(session);
        }
        return null;
    }

    public List<ObjectUndefined> getObjectUndefined() {
        Session session = openSession();
        try {
            StringBuilder stringBuilder = new StringBuilder("");
            //List
            stringBuilder.append("select uuid,name,source_ip FROM object_undefined_info  WHERE is_deleted = 0");
            List<ObjectUndefined> result = session.createNativeQuery(stringBuilder.toString(),ObjectUndefined.class).getResultList();
            return result;
        } catch (Exception ex) {
            LOGGER.error("findAll().ex: " + ex.toString());
            ex.printStackTrace();
        } finally {
            closeSession(session);
        }
        return null;
    }

    public List<ObjectUndefined> getObjectUndefined(String uuid) {
        Session session = openSession();
        try {
            StringBuilder stringBuilder = new StringBuilder("");
            //List
            stringBuilder.append("select uuid,name,source_ip FROM object_undefined_info  WHERE is_deleted = 0 and uuid='").append(uuid).append("'");
            List<ObjectUndefined> result = session.createNativeQuery(stringBuilder.toString(),ObjectUndefined.class).getResultList();
            return result;
        } catch (Exception ex) {
            LOGGER.error("findAll().ex: " + ex.toString());
            ex.printStackTrace();
        } finally {
            closeSession(session);
        }
        return null;
    }

    public List<ObjectUndefinedIp> getObjectUndefinedIp(String uuid) {
        Session session = openSession();
        try {
            StringBuilder stringBuilder = new StringBuilder("");
            //List
            stringBuilder.append("select id, ufo_id,data_source,ip_address "
                    + " FROM ufo_ip  WHERE is_deleted = 0 and ufo_id='").append(uuid).append("'");
            List<ObjectUndefinedIp> result = session.createNativeQuery(stringBuilder.toString(),ObjectUndefinedIp.class).getResultList();
//            List<ObjectUndefinedIp> tmp = result.parallelStream().map(item -> {
//                return new ObjectUndefinedIp(item[0].toString(),Long.valueOf(item[1].toString()),item[2].toString());
//            }).collect(Collectors.toList());
            return result;
        } catch (Exception ex) {
            LOGGER.error("findAll().ex: " + ex.toString());
            ex.printStackTrace();
        } finally {
            closeSession(session);
        }
        return null;
    }
    public List<ObjectUndefinedIp> getObjectUndefinedIp() {
        Session session = openSession();
        try {
            StringBuilder stringBuilder = new StringBuilder("");
            //List
            stringBuilder.append("select id,ufo_id,data_source,ip_address "
                    + " FROM ufo_ip  WHERE is_deleted = 0 ");
            List<ObjectUndefinedIp> result = session.createNativeQuery(stringBuilder.toString(),ObjectUndefinedIp.class).getResultList();
//            List<ObjectUndefinedIp> tmp = result.parallelStream().map(item -> {
//                return new ObjectUndefinedIp(item[0].toString(),Long.valueOf(item[1].toString()),item[2].toString());
//            }).collect(Collectors.toList());
            return result;
        } catch (Exception ex) {
            LOGGER.error("findAll().ex: " + ex.toString());
            ex.printStackTrace();
        } finally {
            closeSession(session);
        }
        return null;
    }


}
