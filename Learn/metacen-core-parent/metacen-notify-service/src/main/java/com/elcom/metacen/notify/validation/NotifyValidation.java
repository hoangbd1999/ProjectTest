package com.elcom.metacen.notify.validation;

import com.elcom.metacen.notify.exception.ValidationException;
import com.elcom.metacen.notify.model.dto.NotifyRequestDTO;
import com.elcom.metacen.utils.StringUtil;
import java.util.stream.IntStream;

public class NotifyValidation extends AbstractValidation {

    public String validate(NotifyRequestDTO notifyDto) throws ValidationException {

        if (StringUtil.isNullOrEmpty(notifyDto.getTitle())) {
            getMessageDes().add("title is required");
        }
        if (StringUtil.isNullOrEmpty(notifyDto.getContent())) {
            getMessageDes().add("content is required");
        }
        if (StringUtil.isNullOrEmpty(notifyDto.getObjectType())) {
            getMessageDes().add("objectType is required");
        }

        // check type
        int[] arrType = {1, 2, 3, 4, 5};
        boolean checkType = IntStream.of(arrType).anyMatch(x -> x == notifyDto.getType());
        if (!checkType) {
            getMessageDes().add("type is invalid");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }
}
