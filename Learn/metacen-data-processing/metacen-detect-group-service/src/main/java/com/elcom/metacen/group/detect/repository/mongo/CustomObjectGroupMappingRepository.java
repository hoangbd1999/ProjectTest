package com.elcom.metacen.group.detect.repository.mongo;

import com.elcom.metacen.group.detect.model.ObjectGroupMapping;

import java.util.List;

public interface CustomObjectGroupMappingRepository extends BaseCustomRepository<ObjectGroupMapping> {
    List<ObjectGroupMapping> getObjectGroupMappingByObjectGroupUuid(List<String> uuids);

    Integer insert(List<ObjectGroupMapping> objectGroupMappings);
}
