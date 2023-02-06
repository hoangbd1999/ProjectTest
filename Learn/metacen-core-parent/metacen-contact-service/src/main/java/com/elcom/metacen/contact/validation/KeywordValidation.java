package com.elcom.metacen.contact.validation;

import com.elcom.metacen.contact.model.dto.*;
import com.elcom.metacen.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeywordValidation extends AbstractValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeywordValidation.class);

    public String validateFilterKeyword(KeywordFilterDTO keywordFilterDTO) {
        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateKeywordRequest(KeywordRequestDTO keywordRequestDTO) {
        if (StringUtil.isNullOrEmpty(keywordRequestDTO.getName())) {
            getMessageDes().add("Name không được để trống");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }
    public String validateKeywordGrantRequest(KeywordGrantRequestDTO keywordGrantRequestDTO) {
        if (StringUtil.isNullOrEmpty(keywordGrantRequestDTO.getName())) {
            getMessageDes().add("Name không được để trống");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }
    public String validateKeywordDataRequest(KeywordDataRequestDTO keywordDataRequestDTO) {
        if (StringUtil.isNullOrEmpty(keywordDataRequestDTO.getRefId())) {
            getMessageDes().add("RefId không được để trống");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateKeywordDataObject(KeyworDataObject keyworDataObject) {
        return !isValid() ? this.buildValidationMessage() : null;
    }
}
