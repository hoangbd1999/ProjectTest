package com.elcom.metacen.contact.validation;

import com.elcom.metacen.contact.model.dto.EventDTO.EventFilterDTO;
import com.elcom.metacen.contact.model.dto.EventDTO.EventRequestDTO;
import com.elcom.metacen.contact.model.dto.ObjectGroup.ObjectGroupRequestDTO;
import com.elcom.metacen.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectGroupValidation extends AbstractValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectGroupValidation.class);
    
    public String validateObjectGroup(ObjectGroupRequestDTO req) {
        if (StringUtil.isNullOrEmpty(req.getName())) {
            getMessageDes().add("Tên nhóm không được để trống");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }


    public String validateObjectGroupName(String name) {
        if (StringUtil.isNullOrEmpty(name)) {
            getMessageDes().add("Tên nhóm không được để trống");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }

}
