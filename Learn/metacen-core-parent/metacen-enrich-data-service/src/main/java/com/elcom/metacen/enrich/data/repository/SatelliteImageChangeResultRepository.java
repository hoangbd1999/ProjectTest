package com.elcom.metacen.enrich.data.repository;

import com.elcom.metacen.enrich.data.model.SatelliteImageChanges;
import com.elcom.metacen.enrich.data.model.SatelliteImageChangesResult;
import com.elcom.metacen.enrich.data.model.dto.SatelliteImageChangeFilterDTO;
import com.elcom.metacen.enrich.data.model.dto.SatelliteImageChangeRequestDTO;
import com.elcom.metacen.enrich.data.model.dto.SatelliteImageChangeResponseDTO;
import com.elcom.metacen.enrich.data.model.dto.SatelliteImageChangeResultDTO;
import com.elcom.metacen.utils.DateUtils;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
public class SatelliteImageChangeResultRepository extends BaseRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(SatelliteImageChangeResultRepository.class);


    @Autowired
    @Qualifier("clickHouseSession")
    SessionFactory clickHouseSessionFactory;

    @Autowired
    public SatelliteImageChangeResultRepository(@Qualifier("clickHouseSession") EntityManagerFactory factory, @Qualifier("chDatasource") DataSource dataSource) {
        super(factory, dataSource);
    }

    public List<SatelliteImageChangeResultDTO> findByUuid(String uuid) {
        Session session = this.clickHouseSessionFactory.openSession();
        String schemaMeta = this.getSchemaMeta();
        try {
            String sql = "SELECT * FROM " + schemaMeta + ".satellite_image_changes_result sic WHERE satelliteImageChangesUuidKey = :satelliteImageChangesUuidKey";
            NativeQuery query = session.createNativeQuery(sql)
                    .setParameter("satelliteImageChangesUuidKey", uuid);
            query.addScalar("uuidKey", StringType.INSTANCE)
                    .addScalar("satelliteImageChangesUuidKey", StringType.INSTANCE)
                    .addScalar("originLatitude", BigDecimalType.INSTANCE)
                    .addScalar("originLongitude", BigDecimalType.INSTANCE)
                    .addScalar("cornerLatitude", BigDecimalType.INSTANCE)
                    .addScalar("cornerLongitude", BigDecimalType.INSTANCE)
                    .addScalar("width", LongType.INSTANCE)
                    .addScalar("height", LongType.INSTANCE)
                    .addScalar("imageFilePathOrigin", StringType.INSTANCE)
                    .addScalar("imageFilePathCompare", StringType.INSTANCE)
                    .addScalar("ingestTime", TimestampType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(SatelliteImageChangeResultDTO.class));
            List<SatelliteImageChangeResultDTO> results = query.getResultList();
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
