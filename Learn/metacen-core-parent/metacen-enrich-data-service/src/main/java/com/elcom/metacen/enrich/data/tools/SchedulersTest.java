package com.elcom.metacen.enrich.data.tools;

import com.elcom.metacen.dto.redis.VsatVesselType;
import com.elcom.metacen.enrich.data.constant.Constant;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 *
 * @author anhdv
 */
@Service
public class SchedulersTest {

    @Autowired
    private RedisTemplate redisTemplate;

//    @Scheduled(fixedDelay = Long.MAX_VALUE)
    public void progress2() {
        try {

            String key = Constant.REDIS_VESSEL_LST_KEY;
            if (this.redisTemplate.hasKey(key)) {
                List<VsatVesselType> vsatVesselTypesFromCaches = (List<VsatVesselType>) this.redisTemplate.opsForList().range(key, 0, Constant.REDIS_VESSEL_LST_FETCH_MAX);
                if ( vsatVesselTypesFromCaches != null && !vsatVesselTypesFromCaches.isEmpty() ) {
                    int a = -21;
                } else {
                    System.out.println("vsatVesselTypesFromCaches is null");
                }
            }

            int a = -1;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
