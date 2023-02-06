package com.elcom.metacen.contact.validation;

import com.elcom.metacen.contact.model.dto.EventDTO.EventFilterDTO;
import com.elcom.metacen.contact.model.dto.EventDTO.EventRequestDTO;
import com.elcom.metacen.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventValidation extends AbstractValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventValidation.class);
    
    public String validateEvent(EventRequestDTO eventRequestDTO) {
        if (StringUtil.isNullOrEmpty(eventRequestDTO.getName())) {
            getMessageDes().add("Tên không được để trống");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateFilterEvent(EventFilterDTO eventFilterDTO ) {
        return !isValid() ? this.buildValidationMessage() : null;
    }
}
