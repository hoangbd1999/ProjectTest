package com.elcom.metacen.notify.schedule;

import com.elcom.metacen.notify.constant.Constant;
import com.elcom.metacen.notify.messaging.rabbitmq.RabbitMQClient;
import com.elcom.metacen.notify.messaging.rabbitmq.RabbitMQProperties;
import com.elcom.metacen.notify.model.DeviceMap;
import com.elcom.metacen.notify.model.dto.Message;
import com.elcom.metacen.notify.model.dto.MobileNotifyRequestDTO;
import com.elcom.metacen.notify.repository.DeviceMapRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class NotifySchedule {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotifySchedule.class);

    @Autowired
    private DeviceMapRepository deviceMapRepository;

    @Autowired
    private RabbitMQClient rabbitMQClient;

    @Value("${fix-delay-noti}")
    private Long delayTime;

    @Value("${socket_rpc.service.name}")
    private String socketRpcServiceName;


//    @Scheduled(fixedDelay = 1000)
    private void processNoti() throws JsonProcessingException {
       List<DeviceMap> deviceMaps = deviceMapRepository.findAll();
        Instant now = Instant.now();
       if(deviceMaps == null){
           LOGGER.info("Không có thông tin config");
           return;
       }
       for (DeviceMap deviceMap : deviceMaps){
           if(deviceMap.isPatrolViolation()){
               boolean checked = checkSocketOnline(deviceMap.getUserId());
               if(!checked){
                   Instant compare = now.minusSeconds(delayTime);
                   if(compare.isAfter(deviceMap.getLastTimeOnline())){
                       handleSendNoti(deviceMap.getUserId());
                       deviceMap.setPatrolViolation(false);
                       deviceMap.setLastTimeOnline(now);
                   }
               }
           }
       }

       deviceMapRepository.saveAll(deviceMaps);

    }

    private void handleSendNoti(String userId){
        try {
            MobileNotifyRequestDTO mobileNotifyRequestDTO = new MobileNotifyRequestDTO();
            mobileNotifyRequestDTO.setContent("Phát hiện phương tiện vi phạm giao thông");
            mobileNotifyRequestDTO.setTitle("Thông bá");
            mobileNotifyRequestDTO.setUserId(userId);
            mobileNotifyRequestDTO.setData("");

            Message message = new Message();
            message.setType(Constant.NOTIFY_MOBILE);
            message.setData(mobileNotifyRequestDTO.toJsonString());
            boolean result = rabbitMQClient.callWorkerService(RabbitMQProperties.WORKER_QUEUE_MESSAGE, message.toJsonString());
            LOGGER.info("Socket work queue - Push to {} msg: {} => {}", RabbitMQProperties.WORKER_QUEUE_MESSAGE, message.toJsonString(), result);
        }catch (Exception e){
            LOGGER.error("Error push to onesignal",e);
        }
    }

    private Boolean checkSocketOnline(String userId) throws JsonProcessingException {
        Boolean isOnline = null;

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        Map<String, Object> bodyParam = new HashMap<>();
        bodyParam.put("serviceName", socketRpcServiceName);
        bodyParam.put("data", data);

        //Authorize user action with api -> call rbac service

        ObjectMapper mapper = new ObjectMapper();
        String meg = mapper.writeValueAsString(bodyParam);
        try {
            String result = rabbitMQClient.callRpcService(RabbitMQProperties.SOCKET_RPC_EXCHANGE,
                    RabbitMQProperties.SOCKET_RPC_QUEUE, RabbitMQProperties.SOCKET_RPC_KEY, meg);
            if (result != null) {
                List<String> res = mapper.readValue(result, List.class);
                if (!Objects.isNull(res) && !res.isEmpty()) {
                    isOnline = true;
                }else {
                    isOnline = false;
                }
            }
        } catch (Exception e) {
            LOGGER.error("err call socket rpc {}", e);
        }
        return isOnline;
    }
}
