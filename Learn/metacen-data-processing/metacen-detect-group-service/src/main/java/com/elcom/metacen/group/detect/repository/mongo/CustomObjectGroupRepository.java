package com.elcom.metacen.group.detect.repository.mongo;

import com.elcom.metacen.group.detect.model.ObjectGroup;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomObjectGroupRepository extends BaseCustomRepository<ObjectGroup> {
    List<ObjectGroup> findGroupOverPeriod(String id);

    Integer insert(List<ObjectGroup> objectGroups);

    Integer updateGroups(List<ObjectGroup> objectGroups);
}
