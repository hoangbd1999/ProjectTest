package com.elcom.metacen.raw.data.validation;

import com.elcom.metacen.raw.data.model.dto.ConvertAndFetchVideoRequestDTO;
import com.elcom.metacen.raw.data.model.dto.DetailMediaRelationRequestDTO;
import com.elcom.metacen.raw.data.model.dto.VsatMediaFilterDTO;
import com.elcom.metacen.raw.data.model.dto.VsatMediaOverallFilterDTO;
import com.elcom.metacen.raw.data.model.dto.VsatMediaOverallStatisticFilterDTO;
import com.elcom.metacen.raw.data.model.dto.VsatMediaRelationFilterDTO;
import com.elcom.metacen.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VsatMediaValidation extends AbstractValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(VsatMediaValidation.class);

    public String validateFilterVsatMediaRawData(VsatMediaFilterDTO vsatMediaFilterDTO) {
        if (StringUtil.isNullOrEmpty(vsatMediaFilterDTO.getFromTime())) {
            getMessageDes().add("FromTime không được để trống");
        }
        if (StringUtil.isNullOrEmpty(vsatMediaFilterDTO.getToTime())) {
            getMessageDes().add("ToTime không được để trống");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateFilterVsatMediaOverall(VsatMediaOverallFilterDTO vsatMediaFilterDTO) {
        if (StringUtil.isNullOrEmpty(vsatMediaFilterDTO.getFromTime())) {
            getMessageDes().add("FromTime không được để trống");
        }
        if (StringUtil.isNullOrEmpty(vsatMediaFilterDTO.getToTime())) {
            getMessageDes().add("ToTime không được để trống");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateVsatMediaOverallStatistc(VsatMediaOverallStatisticFilterDTO vsatMediaOverallStatisticFilterDTO) {
        if (StringUtil.isNullOrEmpty(vsatMediaOverallStatisticFilterDTO.getFromTime())) {
            getMessageDes().add("FromTime không được để trống");
        }
        if (StringUtil.isNullOrEmpty(vsatMediaOverallStatisticFilterDTO.getToTime())) {
            getMessageDes().add("ToTime không được để trống");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateFilterVsatMediaRelationRawData(VsatMediaRelationFilterDTO vsatMediaRelationFilterDTO) {
        if (StringUtil.isNullOrEmpty(vsatMediaRelationFilterDTO.getFromTime())) {
            getMessageDes().add("FromTime không được để trống");
        }
        if (StringUtil.isNullOrEmpty(vsatMediaRelationFilterDTO.getToTime())) {
            getMessageDes().add("ToTime không được để trống");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateDetailMediaRelationRequestDTO(DetailMediaRelationRequestDTO detailMediaRelationRequestDTO) {
        if (StringUtil.isNullOrEmpty(detailMediaRelationRequestDTO.getUuidKeyFrom())) {
            getMessageDes().add("UuidKeyFrom không được để trống");
        }
        if (StringUtil.isNullOrEmpty(detailMediaRelationRequestDTO.getUuidKeyTo())) {
            getMessageDes().add("UuidKeyTo không được để trống");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateConvertAndFetchVideo(ConvertAndFetchVideoRequestDTO convertAndFetchVideoRequestDTO) {
        if (StringUtil.isNullOrEmpty(convertAndFetchVideoRequestDTO.getFilePath())) {
            getMessageDes().add("FilePath không được để trống");
        }
        if (StringUtil.isNullOrEmpty(convertAndFetchVideoRequestDTO.getTargetExtension())) {
            getMessageDes().add("TargetExtension không được để trống");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }
}
