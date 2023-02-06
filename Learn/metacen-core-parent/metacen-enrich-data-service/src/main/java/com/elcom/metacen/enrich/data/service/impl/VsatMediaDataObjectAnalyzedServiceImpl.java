/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.enrich.data.service.impl;


import com.elcom.metacen.enrich.data.model.VsatMediaDataObjectAnalyzed;
import com.elcom.metacen.enrich.data.model.dto.VsatMediaDataObjectAnalyzedRequestDTO;
import com.elcom.metacen.enrich.data.repository.VsatMediaDataObjectAnalyzedRepository;
import com.elcom.metacen.enrich.data.service.VsatMediaDataObjectAnalyzedService;
import com.elcom.metacen.enums.DataDeleteStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 *
 * @author Admin
 */
@Service
public class VsatMediaDataObjectAnalyzedServiceImpl implements VsatMediaDataObjectAnalyzedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VsatMediaDataObjectAnalyzedServiceImpl.class);

    @Autowired
    VsatMediaDataObjectAnalyzedRepository vsatMediaDataObjectAnalyzedRepository;

    @Override
    public VsatMediaDataObjectAnalyzed save(VsatMediaDataObjectAnalyzedRequestDTO vsatMediaDataObjectAnalyzedRequestDTO) {
        try {
            return vsatMediaDataObjectAnalyzedRepository.insert(vsatMediaDataObjectAnalyzedRequestDTO);
        } catch (Exception e) {
            LOGGER.error("insert failed >>> {}", e.toString());
            return null;
        }
    }

    @Override
    public VsatMediaDataObjectAnalyzed findByUuid(String uuid) {
        try {
            return vsatMediaDataObjectAnalyzedRepository.findByUuidAndIsDeleted(uuid, DataDeleteStatus.NOT_DELETED.code());
        } catch (Exception e) {
            LOGGER.error("Find by uuid failed >>> {}", e.toString());
            return null;
        }
    }

    @Override
    public VsatMediaDataObjectAnalyzed delete(int isDeleted, String uuid) {
        try {
            return vsatMediaDataObjectAnalyzedRepository.delete(isDeleted, uuid);
        } catch (Exception e) {
            LOGGER.error("update failed >>> {}", e.toString());
            return null;
        }
    }

    @Override
    public List<VsatMediaDataObjectAnalyzed> findAll(String vsatMediaDataAnalyzedUuidKey) {
        try {
            return vsatMediaDataObjectAnalyzedRepository.findAllByIsDeleted(vsatMediaDataAnalyzedUuidKey,DataDeleteStatus.NOT_DELETED.code());
        } catch (Exception e) {
            LOGGER.error("update failed >>> {}", e.toString());
            return null;
        }
    }

    @Override
    public List<VsatMediaDataObjectAnalyzed> findAllByObjectUuid(String ObjectUuid) {
        try {
            return vsatMediaDataObjectAnalyzedRepository.findAllByObjectUuid(ObjectUuid,DataDeleteStatus.NOT_DELETED.code());
        } catch (Exception e) {
            LOGGER.error("update failed >>> {}", e.toString());
            return null;
        }
    }

    @Override
    public void updateNameObjectInternal(String ObjectUuid, String objectName) {
        try {
             vsatMediaDataObjectAnalyzedRepository.updateNameObjectInternal(ObjectUuid, objectName, DataDeleteStatus.NOT_DELETED.code());
        } catch (Exception e) {
            LOGGER.error("update failed >>> {}", e.toString());
        }
    }

    @Override
    public VsatMediaDataObjectAnalyzed checkExist(String vsatMediaDataAnalyzedUuidKey) {
        try {
            return vsatMediaDataObjectAnalyzedRepository.checkExist(vsatMediaDataAnalyzedUuidKey, DataDeleteStatus.NOT_DELETED.code());
        } catch (Exception e) {
            LOGGER.error("update failed >>> {}", e.toString());
            return null;
        }
    }

    @Override
    public Boolean checkExistByObjectUuid(String vsatMediaDataAnalyzedObjectUuid, String vsatMediaDataAnalyzedObjectType) {
        try {
            return vsatMediaDataObjectAnalyzedRepository.findByObjectUuid(vsatMediaDataAnalyzedObjectUuid, vsatMediaDataAnalyzedObjectType, DataDeleteStatus.NOT_DELETED.code());
        } catch (Exception e) {
            LOGGER.error("Find by object uuid fail >>> {}", e.toString());
            return null;
        }
    }
}
