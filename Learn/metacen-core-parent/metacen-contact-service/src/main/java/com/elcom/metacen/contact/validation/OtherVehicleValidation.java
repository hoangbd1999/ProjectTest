package com.elcom.metacen.contact.validation;

import com.elcom.metacen.contact.model.dto.InfrastructureFilterDTO;
import com.elcom.metacen.contact.model.dto.InfrastructureRequestDTO;
import com.elcom.metacen.contact.model.dto.OtherVehicleFilterDTO;
import com.elcom.metacen.contact.model.dto.OtherVehicleRequestDTO;
import com.elcom.metacen.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OtherVehicleValidation extends AbstractValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(OtherVehicleValidation.class);
    
    public String validateOtherVehicle(OtherVehicleRequestDTO otherVehicleRequestDTO) {
        if (StringUtil.isNullOrEmpty(otherVehicleRequestDTO.getName())) {
            getMessageDes().add("Tên không được để trống");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateFilterOtherVehicle(OtherVehicleFilterDTO otherVehicleFilterDTO ) {
        return !isValid() ? this.buildValidationMessage() : null;
    }
}
