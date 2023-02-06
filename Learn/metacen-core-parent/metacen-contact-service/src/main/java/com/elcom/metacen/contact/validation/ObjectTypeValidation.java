package com.elcom.metacen.contact.validation;

import com.elcom.metacen.contact.model.dto.ObjectTypesRequestDTO;
import com.elcom.metacen.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectTypeValidation extends AbstractValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectTypeValidation.class);

    public String validateObjectType(ObjectTypesRequestDTO item) {

        if (StringUtil.isNullOrEmpty(item.getTypeName())) {
            getMessageDes().add("typeName không được để trống");
        }

        if (StringUtil.isNullOrEmpty(item.getTypeCode())) {
            getMessageDes().add("typeCode không được để trống");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }

}
