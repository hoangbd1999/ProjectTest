package com.elcom.metacen.contact.validation;

import com.elcom.metacen.contact.model.dto.*;
import com.elcom.metacen.contact.model.dto.EventDTO.EventFilterDTO;
import com.elcom.metacen.contact.model.dto.EventDTO.EventRequestDTO;
import com.elcom.metacen.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarineVesselValidation extends AbstractValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarineVesselValidation.class);

    public String validateMarineVessel(MarineVesselRequestDTO marineVesselRequestDTO) {
        if (StringUtil.isNullOrEmpty(marineVesselRequestDTO.getName())) {
            getMessageDes().add("Tên không được để trống");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateFilterMarineVessel(MarineVesselFilterDTO marineVesselFilterDTO ) {
        return !isValid() ? this.buildValidationMessage() : null;
    }
}
