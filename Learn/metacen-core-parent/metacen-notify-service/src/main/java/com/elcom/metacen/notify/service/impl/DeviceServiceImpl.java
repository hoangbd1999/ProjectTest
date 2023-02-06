package com.elcom.metacen.notify.service.impl;

import com.elcom.metacen.notify.repository.DeviceMapRepository;
import com.elcom.metacen.notify.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeviceServiceImpl implements DeviceService {
    @Autowired
    private DeviceMapRepository deviceMapRepository;

//    @Override
//    public Device saveDevice(String token, String userId, String siteId){
//        Date now = new Date();
//        Device existDevice = deviceRepository.findByDeviceTokenAndUserId(token, userId).orElse(null);
//        if(Objects.isNull(existDevice)){
//            Device device = new Device(UUID.randomUUID().toString(), token,userId, siteId,userId,now,userId,now);
//            return deviceRepository.save(device);
//        }
//        existDevice.setSiteId(siteId);
//        existDevice.setModifiedBy(siteId);
//        existDevice.setModifiedDate(now);
//        return deviceRepository.save(existDevice);
//    }

}
