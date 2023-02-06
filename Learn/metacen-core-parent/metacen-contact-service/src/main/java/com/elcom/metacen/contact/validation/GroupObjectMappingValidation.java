package com.elcom.metacen.contact.validation;

import com.elcom.metacen.contact.model.dto.GroupDTO.GroupObjectMappingDTO;
import com.elcom.metacen.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GroupObjectMappingValidation extends AbstractValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupObjectMappingValidation.class);
    
    public String validateGroupObjectMapping(GroupObjectMappingDTO item) {

        if ( StringUtil.isNullOrEmpty(item.getObjectId()) )
            getMessageDes().add("ObjectId không được để trống");

        return !isValid() ? this.buildValidationMessage() : null;
    }
}
