package com.elcom.metacen.raw.data.validation;

import com.elcom.metacen.raw.data.model.dto.AisDataFilterDTO;
import com.elcom.metacen.raw.data.model.dto.PositionOverallRequest;
import com.elcom.metacen.raw.data.model.dto.VsatAisFilterDTO;
import com.elcom.metacen.utils.DateUtil;
import com.elcom.metacen.utils.StringUtil;
import java.util.Arrays;

public class PositionValidation extends AbstractValidation {

    public String validateSearchVsatAis(VsatAisFilterDTO req) {
        
        if( req == null ) {
            getMessageDes().add("Request không hợp lệ!");
            return !isValid() ? this.buildValidationMessage() : null;
        }
        
        String timeFormat = "yyyy-MM-dd HH:mm:ss";

        if (StringUtil.isNullOrEmpty(req.getFromTime())) {
            getMessageDes().add("Từ ngày không được để trống");
        } else if (!DateUtil.isValidTimeByFormat(req.getFromTime(), timeFormat)) {
            getMessageDes().add("Từ ngày không đúng định dạng [" + timeFormat + "]");
        }

        if (StringUtil.isNullOrEmpty(req.getToTime())) {
            getMessageDes().add("Đến ngày không được để trống");
        } else if (!DateUtil.isValidTimeByFormat(req.getToTime(), timeFormat)) {
            getMessageDes().add("Đến ngày không đúng định dạng [" + timeFormat + "]");
        }

        if (req.getDataSourceId() != null && req.getDataSourceId().equals(0)) {
            getMessageDes().add("dataSource phải lớn hơn 0");
        }

        if (req.getCountryId() != null && req.getCountryId().equals(0)) {
            getMessageDes().add("countryId phải lớn hơn 0");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }
    
    public String validateSearchAisData(AisDataFilterDTO req) {
        
        if( req == null ) {
            getMessageDes().add("Request không hợp lệ!");
            return !isValid() ? this.buildValidationMessage() : null;
        }
        
        String timeFormat = "yyyy-MM-dd HH:mm:ss";

        if (StringUtil.isNullOrEmpty(req.getFromTime())) {
            getMessageDes().add("Từ ngày không được để trống");
        } else if (!DateUtil.isValidTimeByFormat(req.getFromTime(), timeFormat)) {
            getMessageDes().add("Từ ngày không đúng định dạng [" + timeFormat + "]");
        }

        if (StringUtil.isNullOrEmpty(req.getToTime())) {
            getMessageDes().add("Đến ngày không được để trống");
        } else if (!DateUtil.isValidTimeByFormat(req.getToTime(), timeFormat)) {
            getMessageDes().add("Đến ngày không đúng định dạng [" + timeFormat + "]");
        }
        return !isValid() ? this.buildValidationMessage() : null;
    }
    
    public String validateSearchPositionGeneral(PositionOverallRequest req) {
        
        if( req == null ) {
            getMessageDes().add("Request không hợp lệ!");
            return !isValid() ? this.buildValidationMessage() : null;
        }
        
        String timeFormat = "yyyy-MM-dd HH:mm:ss";

        if (StringUtil.isNullOrEmpty(req.getFromTime()))
            getMessageDes().add("Từ ngày không được để trống");
        else if (!DateUtil.isValidTimeByFormat(req.getFromTime(), timeFormat))
            getMessageDes().add("Từ ngày không đúng định dạng [" + timeFormat + "]");

        if (StringUtil.isNullOrEmpty(req.getToTime()))
            getMessageDes().add("Đến ngày không được để trống");
        else if (!DateUtil.isValidTimeByFormat(req.getToTime(), timeFormat))
            getMessageDes().add("Đến ngày không đúng định dạng [" + timeFormat + "]");

        if ( !StringUtil.isNullOrEmpty(req.getSourceType()) ) {
            String[] validValues = { "AIS", "VSAT", "VSAT,AIS", "AIS,VSAT" };
            if ( !Arrays.asList(validValues).contains(req.getSourceType().trim().toUpperCase()) )
                getMessageDes().add("sourceType không hợp lệ: ["+req.getSourceType()+"]");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }
}
