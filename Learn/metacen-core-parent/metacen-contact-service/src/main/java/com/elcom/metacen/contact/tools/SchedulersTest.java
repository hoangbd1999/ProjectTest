package com.elcom.metacen.contact.tools;

//import com.elcom.metacen.contact.model.VsatDataSource;
import com.elcom.metacen.contact.repository.VsatVesselTypeRepository;
import com.elcom.metacen.dto.redis.VsatDataSource;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import static org.springframework.data.redis.serializer.RedisSerializer.java;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 * @author anhdv
 */
@Service
public class SchedulersTest {
    
    @Autowired
    VsatVesselTypeRepository vsatVesselTypeRepository;
    
    @Autowired
    private RedisTemplate redisTemplate;
    
//    @Scheduled(fixedDelay = Long.MAX_VALUE)
//    public void progress1() {
//        try {
//            
//                List<VsatVesselType> lst = (List<VsatVesselType>) this.vsatVesselTypeRepository.findAllByOrderByTypeCodeAsc();
//                if (lst != null && !lst.isEmpty()) {
//
//                    Long pushValStatus = this.redisTemplate.opsForList().rightPushAll("METACEN_VSAT_VESSEL_TYPE_LST", lst);
////                        if (pushValStatus != null && !pushValStatus.equals(0L)) {
////                            this.redisTemplate.expire(key, 30, TimeUnit.DAYS);
////                        }
//                }
//           
//            
//            int a=-1;
//            
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
    
//    @Scheduled(fixedDelay = Long.MAX_VALUE)
    public void progress2() {
        try {
            
                List<VsatDataSource> lst = (List<VsatDataSource>) this.vsatVesselTypeRepository.findAllByOrderByDataSourceIdDesc();
            
//                List<VsatDataSourceSimple> lst = new ArrayList<>();
//                for( VsatDataSource item : lst0 ) {
//                    lst.add(new VsatDataSourceSimple(item.getDataSourceId(), item.getDataSourceName()));
//                }
                
                if ( !lst.isEmpty() ) {

                    Long pushValStatus = this.redisTemplate.opsForList().rightPushAll("METACEN_VSAT_DATA_SOURCE_LST", lst);
//                        if (pushValStatus != null && !pushValStatus.equals(0L)) {
//                            this.redisTemplate.expire(key, 30, TimeUnit.DAYS);
//                        }
                }
           
            
            int a=-1;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
