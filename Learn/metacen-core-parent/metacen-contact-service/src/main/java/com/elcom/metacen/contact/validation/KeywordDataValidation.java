package com.elcom.metacen.contact.validation;

import com.elcom.metacen.contact.model.dto.KeywordDataDTO;
import com.elcom.metacen.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeywordDataValidation extends AbstractValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeywordDataValidation.class);

    public String validateObjectKeyword(KeywordDataDTO keywordDataDTO) {
        if (StringUtil.isNullOrEmpty(keywordDataDTO.getRefId())) {
            getMessageDes().add("RefId không được để trống");
        }
        if (StringUtil.isNullOrEmpty(keywordDataDTO.getRefType())) {
            getMessageDes().add("RefType không được để trống");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }
}
