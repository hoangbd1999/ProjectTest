
package com.elcom.metacen.contact.service.impl;


import com.elcom.metacen.contact.model.OtherVehicle;
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
public class OtherVehicleServiceImpl implements OtherVehicleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OtherVehicleServiceImpl.class);

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    OtherVehicleRepository otherVehicleRepository;

    @Autowired
    ObjectRelationshipService objectRelationshipService;

    @Autowired
    KeywordDataService keywordDataService;

    @Autowired
    CustomOtherVehicleRepository customOtherVehicleRepository;

    @Autowired
    SequenceGeneratorService sequenceGeneratorService;

    @Override
    public OtherVehicle save(OtherVehicleRequestDTO otherVehicleRequestDTO, String createBy) {
        try {
            OtherVehicle otherVehicle = modelMapper.map(otherVehicleRequestDTO, OtherVehicle.class);
            otherVehicle.setId(StringUtil.convertObject(DataSequenceStatus.V,sequenceGeneratorService.getNextSequenceId(OtherVehicle.SEQUENCE_NAME)));
            otherVehicle.setUuid(UUID.randomUUID().toString());
            otherVehicle.setCreatedDate(DateUtils.convertToLocalDateTime(new Date()));
            otherVehicle.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            otherVehicle.setCreatedBy(createBy);
            otherVehicle.setIsDeleted(DataDeleteStatus.NOT_DELETED.code());

            OtherVehicle response = otherVehicleRepository.save(otherVehicle);
            // save relationship list
            List<ObjectRelationshipDTO> objectRelationshipDtoList = otherVehicleRequestDTO.getRelationshipLst();
            if (objectRelationshipDtoList != null && !objectRelationshipDtoList.isEmpty()) {
                objectRelationshipDtoList.stream().forEach((item) -> {
                    item.setSourceObjectType(ObjectType.OTHER_VEHICLE.name());
                    item.setSourceObjectId(response.getUuid());
                });

                objectRelationshipService.save(objectRelationshipDtoList);
            }
            // save keyword list
            List<String> keywordIds = otherVehicleRequestDTO.getKeywordLst();
            if (keywordIds != null && !keywordIds.isEmpty()) {
                KeywordDataDTO keywordDataDTO = new KeywordDataDTO();
                keywordDataDTO.setRefType(ObjectType.OTHER_VEHICLE.name());
                keywordDataDTO.setRefId(response.getUuid());
                keywordDataDTO.setKeywordIds(keywordIds);

                keywordDataService.save(keywordDataDTO);
            }
            return response;
        } catch (Exception ex) {
            LOGGER.error("Save other vehicle failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public OtherVehicle findByUuid(String uuid) {
        try {
            return otherVehicleRepository.findByUuidAndIsDeleted(uuid, DataDeleteStatus.NOT_DELETED.code());
        } catch (Exception ex) {
            LOGGER.error("find other vehicle failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public OtherVehicle updateOtherVehicle(OtherVehicle otherVehicle, OtherVehicleRequestDTO otherVehicleRequestDTO, String modifiedBy) {
        try {
            if (!StringUtil.isNullOrEmpty(otherVehicleRequestDTO.getName())) {
                otherVehicle.setName(otherVehicleRequestDTO.getName());
            }
            otherVehicle.setDimLength(otherVehicleRequestDTO.getDimLength());
            otherVehicle.setDimWidth(otherVehicleRequestDTO.getDimWidth());
            otherVehicle.setDimHeight(otherVehicleRequestDTO.getDimHeight());
            otherVehicle.setCountryId(otherVehicleRequestDTO.getCountryId());
            otherVehicle.setDescription(otherVehicleRequestDTO.getDescription());
            otherVehicle.setTonnage(otherVehicleRequestDTO.getTonnage());
            otherVehicle.setPayroll(otherVehicleRequestDTO.getPayroll());
            otherVehicle.setSideId(otherVehicleRequestDTO.getSideId());
            otherVehicle.setEquipment(otherVehicleRequestDTO.getEquipment());
            otherVehicle.setSpeedMax(otherVehicleRequestDTO.getSpeedMax());
            if (otherVehicleRequestDTO.getImageLst() != null) {
                otherVehicle.setImageLst(otherVehicleRequestDTO.getImageLst());
            }
            if (otherVehicleRequestDTO.getFileAttachmentLst() != null) {
                otherVehicle.setFileAttachmentLst(otherVehicleRequestDTO.getFileAttachmentLst());
            }
            otherVehicle.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            otherVehicle.setModifiedBy(modifiedBy);
            OtherVehicle response = otherVehicleRepository.save(otherVehicle);
            // save relationship list
            List<ObjectRelationshipDTO> objectRelationshipDtoList = otherVehicleRequestDTO.getRelationshipLst();
            if (objectRelationshipDtoList != null && !objectRelationshipDtoList.isEmpty()) {
                objectRelationshipDtoList.stream().forEach((item) -> {
                    item.setSourceObjectType(ObjectType.OTHER_VEHICLE.name());
                    item.setSourceObjectId(response.getUuid());
                });
            }
            objectRelationshipService.update(otherVehicle.getUuid(), objectRelationshipDtoList);

            // save keyword list
            List<String> keywordIds = otherVehicleRequestDTO.getKeywordLst();
            KeywordDataDTO keywordDataDTO = new KeywordDataDTO();
            keywordDataDTO.setRefType(ObjectType.OTHER_VEHICLE.name());
            keywordDataDTO.setRefId(response.getUuid());
            keywordDataDTO.setKeywordIds(keywordIds);
            keywordDataService.update(keywordDataDTO);
            return response;
        } catch (Exception ex) {
            LOGGER.error("Update other vehicle failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public Page<OtherVehicleResponseDTO> findListOtherVehicle(OtherVehicleFilterDTO otherVehicleFilterDTO) {
        Integer page = otherVehicleFilterDTO.getPage() > 0 ? otherVehicleFilterDTO.getPage() : 0;
        Pageable pageable = PageRequest.of(page, otherVehicleFilterDTO.getSize());

        return customOtherVehicleRepository.search(otherVehicleFilterDTO, pageable);
    }

    @Override
    public OtherVehicle delete(OtherVehicle otherVehicle, String modifiedBy) {
        try {
            otherVehicle.setModifiedBy(modifiedBy);
            otherVehicle.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            otherVehicle.setIsDeleted(DataDeleteStatus.DELETED.code());
            return otherVehicleRepository.save(otherVehicle);
        } catch (Exception ex) {
            LOGGER.error("Delete other vehicle failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public OtherVehicleResponseDTO findOtherVehicleByUuid(String uuid) {
        OtherVehicleResponseDTO otherVehicleResponseDTO = customOtherVehicleRepository.findOtherVehicleByUuid(uuid);
        return otherVehicleResponseDTO;
    }
}
