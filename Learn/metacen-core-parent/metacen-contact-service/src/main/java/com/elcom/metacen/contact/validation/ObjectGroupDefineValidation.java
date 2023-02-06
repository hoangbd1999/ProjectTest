package com.elcom.metacen.contact.validation;

import com.elcom.metacen.contact.model.dto.ObjectGroupDefine.ObjectGroupDefineRequestDTO;
import com.elcom.metacen.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectGroupDefineValidation extends AbstractValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectGroupDefineValidation.class);
    
    public String validateObjectGroupDefine(ObjectGroupDefineRequestDTO req) {
        if (StringUtil.isNullOrEmpty(req.getName())) {
            getMessageDes().add("Tên nhóm không được để trống");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }

}
