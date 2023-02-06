package com.elcom.metacen.contact.service.impl;

import com.elcom.metacen.contact.model.Areas;
import com.elcom.metacen.contact.model.dto.AreasDTO.AreasFilterDTO;
import com.elcom.metacen.contact.model.dto.AreasDTO.AreasResponseDTO;
import com.elcom.metacen.contact.model.dto.AreasDTO.AreasRequestDTO;
import com.elcom.metacen.contact.model.dto.KeywordDataDTO;
import com.elcom.metacen.contact.model.dto.ObjectRelationshipDTO;
import com.elcom.metacen.contact.repository.CustomAreasRepository;
import com.elcom.metacen.contact.repository.AreasRepository;
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
public class AreasServiceImpl implements AreasService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AreasServiceImpl.class);

    @Autowired
    AreasRepository areasRepository;

    @Autowired
    CustomAreasRepository customAreasRepository;

    @Autowired
    ObjectRelationshipService objectRelationshipService;

    @Autowired
    KeywordDataService keywordDataService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    SequenceGeneratorService sequenceGeneratorService;

    public Areas save(AreasRequestDTO areasRequestDTO, String createBy) {
        try {
            Areas areas = modelMapper.map(areasRequestDTO, Areas.class);
            areas.setId(StringUtil.convertObject(DataSequenceStatus.A,sequenceGeneratorService.getNextSequenceId(Areas.SEQUENCE_NAME)));
            areas.setUuid(UUID.randomUUID().toString());
            areas.setCreatedDate(DateUtils.convertToLocalDateTime(new Date()));
            areas.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            areas.setCreatedBy(createBy);
            areas.setIsDeleted(DataDeleteStatus.NOT_DELETED.code());
            Areas response = areasRepository.save(areas);
            // save relationship list
            List<ObjectRelationshipDTO> objectRelationshipDtoList = areasRequestDTO.getRelationshipLst();
            if (objectRelationshipDtoList != null && !objectRelationshipDtoList.isEmpty()) {
                objectRelationshipDtoList.stream().forEach((item) -> {
                    item.setSourceObjectType(ObjectType.AREA.name());
                    item.setSourceObjectId(response.getUuid());
                });

                objectRelationshipService.save(objectRelationshipDtoList);
            }
            // save keyword list
            List<String> keywordIds = areasRequestDTO.getKeywordLst();
            if (keywordIds != null && !keywordIds.isEmpty()) {
                KeywordDataDTO keywordDataDTO = new KeywordDataDTO();
                keywordDataDTO.setRefType(ObjectType.AREA.name());
                keywordDataDTO.setRefId(response.getUuid());
                keywordDataDTO.setKeywordIds(keywordIds);

                keywordDataService.save(keywordDataDTO);
            }
            return response;
        } catch (Exception ex) {
            LOGGER.error("Save areas failed >>> {}", ex.toString());
            return null;
        }
    }

    public Areas findById(String uuid) {
        try {
            return areasRepository.findByUuidAndIsDeleted(uuid, DataDeleteStatus.NOT_DELETED.code());
        } catch (Exception e) {
            LOGGER.error("Find by uuid failed >>> {}", e.toString());
            return null;
        }
    }

    public Areas updateAreas(Areas areas, AreasRequestDTO areasRequestDTO, String modifiedBy) {
        try {
            if (!StringUtil.isNullOrEmpty(areasRequestDTO.getName())) {
                areas.setName(areasRequestDTO.getName());
            }
            areas.setValue(areasRequestDTO.getValue());
            areas.setDescription(areasRequestDTO.getDescription());
            areas.setSideId(areasRequestDTO.getSideId());
            if (areasRequestDTO.getImageLst() != null) {
                areas.setImageLst(areasRequestDTO.getImageLst());
            }
            if (areasRequestDTO.getFileAttachmentLst() != null) {
                areas.setFileAttachmentLst(areasRequestDTO.getFileAttachmentLst());
            }
            areas.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            areas.setModifiedBy(modifiedBy);
            Areas response = areasRepository.save(areas);

            // save relationship list
            List<ObjectRelationshipDTO> objectRelationshipDtoList = areasRequestDTO.getRelationshipLst();
            if (objectRelationshipDtoList != null && !objectRelationshipDtoList.isEmpty()) {
                objectRelationshipDtoList.stream().forEach((item) -> {
                    item.setSourceObjectType(ObjectType.AREA.name());
                    item.setSourceObjectId(response.getUuid());
                });
            }
            objectRelationshipService.update(areas.getUuid(), objectRelationshipDtoList);

            // save keyword list
            List<String> keywordIds = areasRequestDTO.getKeywordLst();
            KeywordDataDTO keywordDataDTO = new KeywordDataDTO();
            keywordDataDTO.setRefType(ObjectType.AREA.name());
            keywordDataDTO.setRefId(response.getUuid());
            keywordDataDTO.setKeywordIds(keywordIds);

            keywordDataService.update(keywordDataDTO);
            return response;
        } catch (Exception ex) {
            LOGGER.error("Update areas failed >>> {}", ex.toString());
            return null;
        }
    }

    public Areas deleteAreas(Areas areas, String modifiedBy) {
        try {
            areas.setModifiedBy(modifiedBy);
            areas.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            areas.setIsDeleted(DataDeleteStatus.DELETED.code());
            return areasRepository.save(areas);
        } catch (Exception ex) {
            LOGGER.error("Delete areas failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public AreasResponseDTO findAreasByUuid(String uuid) {
        AreasResponseDTO areasResponseDTO = customAreasRepository.findAreasByUuid(uuid);
        return areasResponseDTO;
    }

    @Override
    public Page<AreasResponseDTO> findListAreas(AreasFilterDTO areasFilterDTO) {
        Integer page = areasFilterDTO.getPage() > 0 ? areasFilterDTO.getPage() : 0;
        Pageable pageable = PageRequest.of(page, areasFilterDTO.getSize());

        return customAreasRepository.search(areasFilterDTO, pageable);
    }

}
