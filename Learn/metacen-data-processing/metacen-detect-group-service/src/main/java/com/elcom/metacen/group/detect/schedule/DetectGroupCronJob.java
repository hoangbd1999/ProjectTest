package com.elcom.metacen.group.detect.schedule;

import com.elcom.metacen.group.detect.model.ObjectGroupGeneralConfig;
import com.elcom.metacen.group.detect.repository.mongo.ObjectGroupGeneralConfigRepository;
import com.elcom.metacen.group.detect.service.IDetectGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

@Component
@Configuration
public class DetectGroupCronJob {

    @Autowired private IDetectGroupService detectGroupService;
    @Autowired private ObjectGroupGeneralConfigRepository objectGroupGeneralConfigRepository;

    @Scheduled(cron = "0 */5 * ? * *")
//    @PostConstruct
    public void detectRealtime() {
        ObjectGroupGeneralConfig objectGroupGeneralConfig = objectGroupGeneralConfigRepository.findAll().get(0);
        detectGroupService.detect(objectGroupGeneralConfig.getTogetherTime(), objectGroupGeneralConfig.getDistanceLevel());
    }

//    @PostConstruct
    public void detectBatch() {
        detectGroupService.detectBatch(48, 7);
    }
}
