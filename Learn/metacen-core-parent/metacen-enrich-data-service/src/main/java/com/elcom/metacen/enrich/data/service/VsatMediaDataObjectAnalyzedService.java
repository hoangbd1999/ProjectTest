/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.enrich.data.service;

import com.elcom.metacen.enrich.data.model.VsatMediaDataObjectAnalyzed;
import com.elcom.metacen.enrich.data.model.dto.VsatMediaDataObjectAnalyzedRequestDTO;

import java.util.List;


/**
 *
 * @author Admin
 */
public interface VsatMediaDataObjectAnalyzedService {

    VsatMediaDataObjectAnalyzed save (VsatMediaDataObjectAnalyzedRequestDTO vsatMediaDataObjectAnalyzedRequestDTO);

    VsatMediaDataObjectAnalyzed findByUuid(String uuid);

    VsatMediaDataObjectAnalyzed delete(int isDeleted, String uuid);

    List<VsatMediaDataObjectAnalyzed> findAll(String vsatMediaDataAnalyzedUuidKey);

    List<VsatMediaDataObjectAnalyzed> findAllByObjectUuid(String ObjectUuid);

    void updateNameObjectInternal(String ObjectUuid, String objectName);

    VsatMediaDataObjectAnalyzed checkExist(String vsatMediaDataAnalyzedUuidKey);

    Boolean checkExistByObjectUuid(String vsatMediaDataAnalyzedObjectUuid, String vsatMediaDataAnalyzedObjectType);
}
