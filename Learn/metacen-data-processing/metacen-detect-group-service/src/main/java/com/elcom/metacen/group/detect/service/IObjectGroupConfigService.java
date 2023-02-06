package com.elcom.metacen.group.detect.service;

import com.elcom.metacen.group.detect.model.dto.ObjectGroupConfigDTO;

import java.util.List;

public interface IObjectGroupConfigService {
    List<ObjectGroupConfigDTO> getConfigAndFilter();
}
