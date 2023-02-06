package com.elcom.metacen.notify.repository;

import com.elcom.metacen.notify.model.DeviceMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceMapRepository extends JpaRepository<DeviceMap, String> {
    DeviceMap findByUserId(String userId);
}
