package com.elcom.metacen.enrich.data.validation;

import com.elcom.metacen.enrich.data.model.dto.VsatMediaAnalyzedFilterDTO;
import com.elcom.metacen.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VsatMediaAnalyzedValidation extends AbstractValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(VsatMediaAnalyzedValidation.class);

    public String validateFilterVsatMediaAnalyzed(VsatMediaAnalyzedFilterDTO vsatMediaAnalyzedFilterDTO) {
        if (StringUtil.isNullOrEmpty(vsatMediaAnalyzedFilterDTO.getFromTime())) {
            getMessageDes().add("FromTime không được để trống");
        }
        if (StringUtil.isNullOrEmpty(vsatMediaAnalyzedFilterDTO.getToTime())) {
            getMessageDes().add("ToTime không được để trống");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }
}
