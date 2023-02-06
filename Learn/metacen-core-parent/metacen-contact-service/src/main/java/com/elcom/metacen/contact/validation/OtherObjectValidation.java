package com.elcom.metacen.contact.validation;

import com.elcom.metacen.contact.model.dto.OtherObjectFilterDTO;
import com.elcom.metacen.contact.model.dto.OtherObjectRequestDTO;
import com.elcom.metacen.contact.model.dto.OtherVehicleFilterDTO;
import com.elcom.metacen.contact.model.dto.OtherVehicleRequestDTO;
import com.elcom.metacen.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OtherObjectValidation extends AbstractValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(OtherObjectValidation.class);
    
    public String validateOtherObject(OtherObjectRequestDTO otherObjectRequestDTO) {
        if (StringUtil.isNullOrEmpty(otherObjectRequestDTO.getName())) {
            getMessageDes().add("Tên không được để trống");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateFilterOtherObject(OtherObjectFilterDTO otherObjectFilterDTO ) {
        return !isValid() ? this.buildValidationMessage() : null;
    }
}
