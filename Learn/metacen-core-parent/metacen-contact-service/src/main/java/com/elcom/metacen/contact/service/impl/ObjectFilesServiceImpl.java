package com.elcom.metacen.contact.service.impl;


import com.elcom.metacen.contact.model.ObjectFiles;
import com.elcom.metacen.contact.model.dto.ObjectFilesDTO;
import com.elcom.metacen.contact.repository.ObjectFilesRepository;
import com.elcom.metacen.contact.service.ObjectFilesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @author hoangbd
 */
@Service
public class ObjectFilesServiceImpl implements ObjectFilesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectFilesServiceImpl.class);

    @Autowired
    ObjectFilesRepository objectFilesRepository;

    @Override
    public ObjectFiles insertObjectFiles(ObjectFilesDTO item) {
        ObjectFiles model = new ObjectFiles(item.getObjectId(),item.getFileType(), item.getObjectType(), item.getImagePath(), item.getIsDelete());
        try {
            this.objectFilesRepository.save(model);
            return model.getObjectId() != null && !model.getObjectId().equals(0L) ? model : null;
        } catch (Exception e) {
            LOGGER.error("ex: ", e);
        }
        return null;
    }

    @Override
    public int delete(String objectId) {
        return objectFilesRepository.delete(objectId);
    }

}
