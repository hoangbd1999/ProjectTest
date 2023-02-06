
package com.elcom.metacen.contact.service.impl;


import com.elcom.metacen.contact.model.OtherObject;
import com.elcom.metacen.contact.model.dto.*;
import com.elcom.metacen.contact.repository.*;
import com.elcom.metacen.contact.service.*;
import com.elcom.metacen.enums.DataDeleteStatus;
import com.elcom.metacen.enums.DataSequenceStatus;
import com.elcom.metacen.enums.ObjectType;
import com.elcom.metacen.utils.DateUtils;
import com.elcom.metacen.utils.StringUtil;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author hoangbd
 */
@Service
public class OtherObjectServiceImpl implements OtherObjectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OtherObjectServiceImpl.class);

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    OtherObjectRepository otherObjectRepository;

    @Autowired
    ObjectRelationshipService objectRelationshipService;

    @Autowired
    KeywordDataService keywordDataService;

    @Autowired
    CustomOtherObjectRepository customOtherObjectRepository;

    @Autowired
    SequenceGeneratorService sequenceGeneratorService;

    @Override
    public OtherObject save(OtherObjectRequestDTO otherObjectRequestDTO, String createBy) {
        try {
            OtherObject otherObject = modelMapper.map(otherObjectRequestDTO, OtherObject.class);
            otherObject.setId(StringUtil.convertObject(DataSequenceStatus.D,sequenceGeneratorService.getNextSequenceId(OtherObject.SEQUENCE_NAME)));
            otherObject.setUuid(UUID.randomUUID().toString());
            otherObject.setCreatedDate(DateUtils.convertToLocalDateTime(new Date()));
            otherObject.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            otherObject.setCreatedBy(createBy);
            otherObject.setIsDeleted(DataDeleteStatus.NOT_DELETED.code());

            OtherObject response = otherObjectRepository.save(otherObject);
            // save relationship list
            List<ObjectRelationshipDTO> objectRelationshipDtoList = otherObjectRequestDTO.getRelationshipLst();
            if (objectRelationshipDtoList != null && !objectRelationshipDtoList.isEmpty()) {
                objectRelationshipDtoList.stream().forEach((item) -> {
                    item.setSourceObjectType(ObjectType.OTHER_OBJECT.name());
                    item.setSourceObjectId(response.getUuid());
                });

                objectRelationshipService.save(objectRelationshipDtoList);
            }
            // save keyword list
            List<String> keywordIds = otherObjectRequestDTO.getKeywordLst();
            if (keywordIds != null && !keywordIds.isEmpty()) {
                KeywordDataDTO keywordDataDTO = new KeywordDataDTO();
                keywordDataDTO.setRefType(ObjectType.OTHER_OBJECT.name());
                keywordDataDTO.setRefId(response.getUuid());
                keywordDataDTO.setKeywordIds(keywordIds);

                keywordDataService.save(keywordDataDTO);
            }
            return response;
        } catch (Exception ex) {
            LOGGER.error("Save other object failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public OtherObject findByUuid(String uuid) {
        try {
            return otherObjectRepository.findByUuidAndIsDeleted(uuid, DataDeleteStatus.NOT_DELETED.code());
        } catch (Exception ex) {
            LOGGER.error("find other object failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public OtherObject updateOtherObject(OtherObject otherObject, OtherObjectRequestDTO otherObjectRequestDTO, String modifiedBy) {
        try {
            if (!StringUtil.isNullOrEmpty(otherObjectRequestDTO.getName())) {
                otherObject.setName(otherObjectRequestDTO.getName());
            }
            otherObject.setCountryId(otherObjectRequestDTO.getCountryId());
            otherObject.setDescription(otherObjectRequestDTO.getDescription());
            otherObject.setSideId(otherObjectRequestDTO.getSideId());
            if (otherObjectRequestDTO.getImageLst() != null) {
                otherObject.setImageLst(otherObjectRequestDTO.getImageLst());
            }
            if (otherObjectRequestDTO.getFileAttachmentLst() != null) {
                otherObject.setFileAttachmentLst(otherObjectRequestDTO.getFileAttachmentLst());
            }
            otherObject.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            otherObject.setModifiedBy(modifiedBy);
            OtherObject response = otherObjectRepository.save(otherObject);
            // save relationship list
            List<ObjectRelationshipDTO> objectRelationshipDtoList = otherObjectRequestDTO.getRelationshipLst();
            if (objectRelationshipDtoList != null && !objectRelationshipDtoList.isEmpty()) {
                objectRelationshipDtoList.stream().forEach((item) -> {
                    item.setSourceObjectType(ObjectType.OTHER_OBJECT.name());
                    item.setSourceObjectId(response.getUuid());
                });
            }
            objectRelationshipService.update(otherObject.getUuid(), objectRelationshipDtoList);

            // save keyword list
            List<String> keywordIds = otherObjectRequestDTO.getKeywordLst();
            KeywordDataDTO keywordDataDTO = new KeywordDataDTO();
            keywordDataDTO.setRefType(ObjectType.OTHER_OBJECT.name());
            keywordDataDTO.setRefId(response.getUuid());
            keywordDataDTO.setKeywordIds(keywordIds);
            keywordDataService.update(keywordDataDTO);
            return response;
        } catch (Exception ex) {
            LOGGER.error("Update other object failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public Page<OtherObjectResponseDTO> findListOtherObject(OtherObjectFilterDTO otherObjectFilterDTO) {
        Integer page = otherObjectFilterDTO.getPage() > 0 ? otherObjectFilterDTO.getPage() : 0;
        Pageable pageable = PageRequest.of(page, otherObjectFilterDTO.getSize());

        return customOtherObjectRepository.search(otherObjectFilterDTO, pageable);
    }

    @Override
    public OtherObject delete(OtherObject otherObject, String modifiedBy) {
        try {
            otherObject.setModifiedBy(modifiedBy);
            otherObject.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            otherObject.setIsDeleted(DataDeleteStatus.DELETED.code());
            return otherObjectRepository.save(otherObject);
        } catch (Exception ex) {
            LOGGER.error("Delete other object failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public OtherObjectResponseDTO findOtherObjectByUuid(String uuid) {
        OtherObjectResponseDTO otherObjectResponseDTO = customOtherObjectRepository.findOtherObjectByUuid(uuid);
        return otherObjectResponseDTO;
    }
}
