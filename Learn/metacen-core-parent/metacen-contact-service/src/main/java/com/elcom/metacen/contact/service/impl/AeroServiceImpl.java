package com.elcom.metacen.contact.service.impl;

import com.elcom.metacen.contact.model.AeroAirplaneInfo;
import com.elcom.metacen.contact.model.dto.AeroDTO.AeroFilterDTO;
import com.elcom.metacen.contact.model.dto.AeroDTO.AeroInsertRequestDTO;
import com.elcom.metacen.contact.model.dto.AeroDTO.AeroResponseDTO;
import com.elcom.metacen.contact.model.dto.KeywordDataDTO;
import com.elcom.metacen.contact.model.dto.ObjectRelationshipDTO;
import com.elcom.metacen.contact.repository.AeroAirplaneInfoRepository;
import com.elcom.metacen.contact.repository.CustomAeroRepository;
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

@Service
public class AeroServiceImpl implements AeroService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AeroServiceImpl.class);

    @Autowired
    AeroAirplaneInfoRepository aeroAirplaneInfoRepository;

    @Autowired
    CustomAeroRepository customAeroRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    ObjectRelationshipService objectRelationshipService;

    @Autowired
    KeywordDataService keywordDataService;

    @Autowired
    SequenceGeneratorService sequenceGeneratorService;

    @Override
    public AeroAirplaneInfo insert(AeroInsertRequestDTO aeroInsertRequestDTO, String createBy) {
        try {
            AeroAirplaneInfo aeroAirplaneInfo = modelMapper.map(aeroInsertRequestDTO, AeroAirplaneInfo.class);
            aeroAirplaneInfo.setId(StringUtil.convertObject(DataSequenceStatus.V, sequenceGeneratorService.getNextSequenceId(AeroAirplaneInfo.SEQUENCE_NAME)));
            aeroAirplaneInfo.setUuid(UUID.randomUUID().toString());
            aeroAirplaneInfo.setIsDeleted(DataDeleteStatus.NOT_DELETED.code());
            aeroAirplaneInfo.setCreatedBy(createBy);
            aeroAirplaneInfo.setCreatedDate(DateUtils.convertToLocalDateTime(new Date()));
            aeroAirplaneInfo.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));

            AeroAirplaneInfo response = aeroAirplaneInfoRepository.save(aeroAirplaneInfo);

            // save relationship list
            List<ObjectRelationshipDTO> objectRelationshipDtoList = aeroInsertRequestDTO.getRelationshipLst();
            if (objectRelationshipDtoList != null && !objectRelationshipDtoList.isEmpty()) {
                objectRelationshipDtoList.stream().forEach((item) -> {
                    item.setSourceObjectType(ObjectType.AIRPLANE.name());
                    item.setSourceObjectId(response.getUuid());
                });

                objectRelationshipService.save(objectRelationshipDtoList);
            }

            // save keyword list
            List<String> keywordIds = aeroInsertRequestDTO.getKeywordLst();
            if (keywordIds != null && !keywordIds.isEmpty()) {
                KeywordDataDTO keywordDataDTO = new KeywordDataDTO();
                keywordDataDTO.setRefType(ObjectType.AIRPLANE.name());
                keywordDataDTO.setRefId(response.getUuid());
                keywordDataDTO.setKeywordIds(keywordIds);
                keywordDataService.save(keywordDataDTO);
            }

            return response;
        } catch (Exception ex) {
            LOGGER.error("Save aero airplane failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public AeroAirplaneInfo update(AeroAirplaneInfo aeroAirplaneInfo, AeroInsertRequestDTO aeroInsertRequestDTO, String modifiedBy) {
        try {

            aeroAirplaneInfo.setName(aeroInsertRequestDTO.getName());
            aeroAirplaneInfo.setModel(aeroInsertRequestDTO.getModel());
            aeroAirplaneInfo.setCountryId(aeroInsertRequestDTO.getCountryId());
            aeroAirplaneInfo.setDimLength(aeroInsertRequestDTO.getDimLength());
            aeroAirplaneInfo.setDimWidth(aeroInsertRequestDTO.getDimWidth());
            aeroAirplaneInfo.setDimHeight(aeroInsertRequestDTO.getDimHeight());
            aeroAirplaneInfo.setSpeedMax(aeroInsertRequestDTO.getSpeedMax());
            aeroAirplaneInfo.setGrossTonnage(aeroInsertRequestDTO.getGrossTonnage());
            aeroAirplaneInfo.setPayrollTime(aeroInsertRequestDTO.getPayrollTime());
            aeroAirplaneInfo.setEquipment(aeroInsertRequestDTO.getEquipment());
            aeroAirplaneInfo.setPermanentBase(aeroInsertRequestDTO.getPermanentBase());
            aeroAirplaneInfo.setDescription(aeroInsertRequestDTO.getDescription());
            aeroAirplaneInfo.setSideId(aeroInsertRequestDTO.getSideId());
            aeroAirplaneInfo.setTypeId(aeroInsertRequestDTO.getTypeId());
            aeroAirplaneInfo.setImageLst(aeroInsertRequestDTO.getImageLst());
            aeroAirplaneInfo.setFileAttachmentLst(aeroInsertRequestDTO.getFileAttachmentLst());
            aeroAirplaneInfo.setModifiedBy(modifiedBy);
            aeroAirplaneInfo.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));

            AeroAirplaneInfo response = aeroAirplaneInfoRepository.save(aeroAirplaneInfo);

            // save relationship list
            List<ObjectRelationshipDTO> objectRelationshipDtoList = aeroInsertRequestDTO.getRelationshipLst();
            if (objectRelationshipDtoList != null && !objectRelationshipDtoList.isEmpty()) {
                objectRelationshipDtoList.stream().forEach((item) -> {
                    item.setSourceObjectType(ObjectType.AIRPLANE.name());
                    item.setSourceObjectId(response.getUuid());
                });
            }
            objectRelationshipService.update(aeroAirplaneInfo.getUuid(), objectRelationshipDtoList);

            // save keyword list
            List<String> keywordIds = aeroInsertRequestDTO.getKeywordLst();
            KeywordDataDTO keywordDataDTO = new KeywordDataDTO();
            keywordDataDTO.setRefType(ObjectType.AIRPLANE.name());
            keywordDataDTO.setRefId(response.getUuid());
            keywordDataDTO.setKeywordIds(keywordIds);
            keywordDataService.update(keywordDataDTO);

            return response;
        } catch (Exception ex) {
            LOGGER.error("Update aero airplane failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public AeroAirplaneInfo findByUuid(String uuid) {
        return aeroAirplaneInfoRepository.findByUuidAndIsDeleted(uuid, DataDeleteStatus.NOT_DELETED.code());
    }

    @Override
    public AeroAirplaneInfo delete(AeroAirplaneInfo aeroAirplaneInfo, String modifiedBy) {
        try {
            aeroAirplaneInfo.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            aeroAirplaneInfo.setModifiedBy(modifiedBy);
            aeroAirplaneInfo.setIsDeleted(DataDeleteStatus.DELETED.code());
            AeroAirplaneInfo response = aeroAirplaneInfoRepository.save(aeroAirplaneInfo);
            return response;
        } catch (Exception ex) {
            LOGGER.error("Delete aero airplane failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public Page<AeroResponseDTO> findListAero(AeroFilterDTO aeroFilterDTO) {
        Integer page = aeroFilterDTO.getPage() > 0 ? aeroFilterDTO.getPage() : 0;
        Pageable pageable = PageRequest.of(page, aeroFilterDTO.getSize());

        return customAeroRepository.search(aeroFilterDTO, pageable);
    }

    @Override
    public AeroResponseDTO findAeroByUuid(String uuid) {
        AeroResponseDTO aeroResponseDTO = customAeroRepository.findAeroByUuid(uuid);
        return aeroResponseDTO;
    }
}
