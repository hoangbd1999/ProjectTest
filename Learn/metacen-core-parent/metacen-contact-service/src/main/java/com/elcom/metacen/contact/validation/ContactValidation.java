package com.elcom.metacen.contact.validation;

import com.elcom.metacen.contact.model.dto.*;
import com.elcom.metacen.utils.StringUtil;

/**
 *
 * @author hoangbd
 */
public class ContactValidation extends AbstractValidation{

    public String validateSearch(Integer currentPage, Integer rowsPerPage) {
        if (currentPage == null) {
            getMessageDes().add("currentPage không được để trống");
        }

        if (rowsPerPage == null || rowsPerPage == 0) {
            getMessageDes().add("rowsPerPage không được để trống");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateInsertMarineVesselInfo(MarineVesselRequestDTO item) {

        if (StringUtil.isNullOrEmpty(String.valueOf(item.getMmsi())))
            getMessageDes().add("mmsi không được để trống");

        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateComment(CommentsDTO item) {

        if ( StringUtil.isNullOrEmpty(item.getRefId()) )
            getMessageDes().add("RefId không được để trống");

        if ( StringUtil.isNullOrEmpty(item.getContent()) )
            getMessageDes().add("Content không được để trống");

        if ( StringUtil.isNullOrEmpty(item.getContentUnsigned()) )
            getMessageDes().add("ContentUnsigned không được để trống");

        return !isValid() ? this.buildValidationMessage() : null;
    }

}
