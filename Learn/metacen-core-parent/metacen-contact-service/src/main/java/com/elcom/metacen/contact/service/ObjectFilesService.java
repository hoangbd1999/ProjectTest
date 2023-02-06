package com.elcom.metacen.contact.service;

import com.elcom.metacen.contact.model.ObjectFiles;
import com.elcom.metacen.contact.model.dto.ObjectFilesDTO;

/**
 * @author hoangbd
 */
public interface ObjectFilesService {

    ObjectFiles insertObjectFiles(ObjectFilesDTO item);

    int delete(String objectId);

}
