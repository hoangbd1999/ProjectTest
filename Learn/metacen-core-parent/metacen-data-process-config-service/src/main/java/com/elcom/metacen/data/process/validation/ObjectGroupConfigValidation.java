package com.elcom.metacen.data.process.validation;

import com.elcom.metacen.data.process.model.dto.ObjectGroupConfigDTO.ObjectGroupConfigFilterDTO;
import com.elcom.metacen.data.process.model.dto.ObjectGroupConfigDTO.ObjectGroupConfigRequestDTO;
import com.elcom.metacen.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectGroupConfigValidation extends AbstractValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectGroupConfigValidation.class);

    public String validateObjectGroupConfig(ObjectGroupConfigRequestDTO objectGroupConfigRequestDTO) {
        if (StringUtil.isNullOrEmpty(objectGroupConfigRequestDTO.getName())) {
            getMessageDes().add("Tên cấu hình không được để trống");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateFilterObjectGroupConfig(ObjectGroupConfigFilterDTO objectGroupConfigFilterDTO) {
        return !isValid() ? this.buildValidationMessage() : null;
    }
}
