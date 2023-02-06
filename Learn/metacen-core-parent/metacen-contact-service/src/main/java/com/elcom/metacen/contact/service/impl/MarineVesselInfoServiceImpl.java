package com.elcom.metacen.contact.service.impl;

import com.elcom.metacen.contact.model.MarineVesselInfo;
import com.elcom.metacen.contact.model.dto.*;
import com.elcom.metacen.contact.repository.CustomMarineVesselRepository;
import com.elcom.metacen.contact.repository.MarineVesselInfoRepository;
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
public class MarineVesselInfoServiceImpl implements MarineVesselInfoService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarineVesselInfoServiceImpl.class);

    @Autowired
    MarineVesselInfoRepository marineVesselInfoRepository;

    @Autowired
    ObjectRelationshipService objectRelationshipService;

    @Autowired
    KeywordDataService keywordDataService;

    @Autowired
    CustomMarineVesselRepository customMarineVesselRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    SequenceGeneratorService sequenceGeneratorService;

    @Override
    public MarineVesselInfo save(MarineVesselRequestDTO marineVesselRequestDTO, String createBy) {
        try {
            MarineVesselInfo marineVesselInfo = modelMapper.map(marineVesselRequestDTO, MarineVesselInfo.class);
            marineVesselInfo.setId(StringUtil.convertObject(DataSequenceStatus.V,sequenceGeneratorService.getNextSequenceId(MarineVesselInfo.SEQUENCE_NAME)));
            marineVesselInfo.setUuid(UUID.randomUUID().toString());
            marineVesselInfo.setCreatedDate(DateUtils.convertToLocalDateTime(new Date()));
            marineVesselInfo.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            marineVesselInfo.setCreatedBy(createBy);
            marineVesselInfo.setIsDeleted(DataDeleteStatus.NOT_DELETED.code());
            MarineVesselInfo response = marineVesselInfoRepository.save(marineVesselInfo);

            // save relationship list
            List<ObjectRelationshipDTO> objectRelationshipDtoList = marineVesselRequestDTO.getRelationshipLst();
            if (objectRelationshipDtoList != null && !objectRelationshipDtoList.isEmpty()) {
                objectRelationshipDtoList.stream().forEach((item) -> {
                    item.setSourceObjectType(ObjectType.VESSEL.name());
                    item.setSourceObjectId(response.getUuid());
                });

                objectRelationshipService.save(objectRelationshipDtoList);
            }
            // save keyword list
            List<String> keywordIds = marineVesselRequestDTO.getKeywordLst();
            if (keywordIds != null && !keywordIds.isEmpty()) {
                KeywordDataDTO keywordDataDTO = new KeywordDataDTO();
                keywordDataDTO.setRefType(ObjectType.VESSEL.name());
                keywordDataDTO.setRefId(response.getUuid());
                keywordDataDTO.setKeywordIds(keywordIds);

                keywordDataService.save(keywordDataDTO);
            }
            return response;
        } catch (Exception e) {
            LOGGER.error("Save Marine Vessel failed >>> {}", e.toString());
            return null;
        }
    }

    @Override
    public MarineVesselInfo findByMmsi(Long mmsi) {
        try {
            return marineVesselInfoRepository.findByMmsiAndIsDeleted(mmsi, DataDeleteStatus.NOT_DELETED.code());
        } catch (Exception e) {
            LOGGER.error("Find by mmsi failed >>> {}", e.toString());
            return null;
        }
    }

    @Override
    public MarineVesselInfo findById(String uuid) {
        try {
            return marineVesselInfoRepository.findByUuidAndIsDeleted(uuid, DataDeleteStatus.NOT_DELETED.code());
        } catch (Exception e) {
            LOGGER.error("Find by uuid failed >>> {}", e.toString());
            return null;
        }
    }

    @Override
    public MarineVesselResponseDTO findMarineVesselByUuid(String uuid) {
        MarineVesselResponseDTO marineVesselResponseDTO = customMarineVesselRepository.findMarineVesselByUuid(uuid);
        return marineVesselResponseDTO;
    }

    @Override
    public MarineVesselInfo updateMarineVesselInfo(MarineVesselInfo marineVesselInfo,MarineVesselRequestDTO marineVesselRequestDTO,String modifiedBy) {
        try {
            if (!StringUtil.isNullOrEmpty(String.valueOf(marineVesselRequestDTO.getMmsi()))) {
                marineVesselInfo.setMmsi(marineVesselRequestDTO.getMmsi());
            }
            if (!StringUtil.isNullOrEmpty(marineVesselRequestDTO.getName())) {
                marineVesselInfo.setName(marineVesselRequestDTO.getName());
            }
            marineVesselInfo.setImo(marineVesselRequestDTO.getImo());
            marineVesselInfo.setCountryId(marineVesselRequestDTO.getCountryId());
            marineVesselInfo.setTypeId(marineVesselRequestDTO.getTypeId());
            if(marineVesselRequestDTO.getImageLst() != null){
                marineVesselInfo.setImageLst(marineVesselRequestDTO.getImageLst());
            }
            if(marineVesselRequestDTO.getFileAttachmentLst() != null){
                marineVesselInfo.setFileAttachmentLst(marineVesselRequestDTO.getFileAttachmentLst());
            }
            marineVesselInfo.setDimA(marineVesselRequestDTO.getDimA());
            marineVesselInfo.setDimC(marineVesselRequestDTO.getDimC());
            marineVesselInfo.setPayroll(marineVesselRequestDTO.getPayroll());
            marineVesselInfo.setDescription(marineVesselRequestDTO.getDescription());
            marineVesselInfo.setEquipment(marineVesselRequestDTO.getEquipment());
            marineVesselInfo.setDraught(marineVesselRequestDTO.getDraught());
            marineVesselInfo.setGrossTonnage(marineVesselRequestDTO.getGrossTonnage());
            marineVesselInfo.setSpeedMax(marineVesselRequestDTO.getSpeedMax());
            marineVesselInfo.setSideId(marineVesselRequestDTO.getSideId());
            marineVesselInfo.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            marineVesselInfo.setModifiedBy(modifiedBy);
            MarineVesselInfo response = marineVesselInfoRepository.save(marineVesselInfo);

            // save relationship list
            List<ObjectRelationshipDTO> objectRelationshipDtoList = marineVesselRequestDTO.getRelationshipLst();
            if (objectRelationshipDtoList != null && !objectRelationshipDtoList.isEmpty()) {
                objectRelationshipDtoList.stream().forEach((item) -> {
                    item.setSourceObjectType(ObjectType.VESSEL.name());
                    item.setSourceObjectId(response.getUuid());
                });
            }
            objectRelationshipService.update(marineVesselInfo.getUuid(), objectRelationshipDtoList);

            // save keyword list
            List<String> keywordIds = marineVesselRequestDTO.getKeywordLst();
            KeywordDataDTO keywordDataDTO = new KeywordDataDTO();
            keywordDataDTO.setRefType(ObjectType.VESSEL.name());
            keywordDataDTO.setRefId(response.getUuid());
            keywordDataDTO.setKeywordIds(keywordIds);

            keywordDataService.update(keywordDataDTO);
            return response;
        } catch (Exception e) {
            LOGGER.error("ex: ", e);
            return null;
        }
    }

    @Override
    public MarineVesselInfo delete(MarineVesselInfo marineVesselInfo, String modifiedBy) {
        try {
            marineVesselInfo.setModifiedBy(modifiedBy);
            marineVesselInfo.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            marineVesselInfo.setIsDeleted(DataDeleteStatus.DELETED.code());
            return marineVesselInfoRepository.save(marineVesselInfo);
        } catch (Exception ex) {
            LOGGER.error("Delete Marine Vessel failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public Page<MarineVesselResponseDTO> findListMarineVessel(MarineVesselFilterDTO marineVesselFilterDTO) {
        Integer page = marineVesselFilterDTO.getPage() > 0 ? marineVesselFilterDTO.getPage() : 0;
        Pageable pageable = PageRequest.of(page, marineVesselFilterDTO.getSize());

        return customMarineVesselRepository.search(marineVesselFilterDTO, pageable);
    }

    @Override
    public List<MarineVesselInfo> getLstMarineVesselId(List<Integer> mmsiLst) {
        return customMarineVesselRepository.findListMarineVessel(mmsiLst);
    }

}
