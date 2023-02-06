package com.elcom.metacen.group.detect.repository.mongo;

import com.elcom.metacen.group.detect.model.ObjectGroupConfig;

import java.util.List;

/**
 *
 * @author Admin
 */
public interface CustomObjectGroupConfigRepository extends BaseCustomRepository<ObjectGroupConfig> {
    List<ObjectGroupConfig> getActiveConfig();
}
