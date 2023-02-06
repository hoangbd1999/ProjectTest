
package com.elcom.metacen.contact.service.impl;


import com.elcom.metacen.contact.model.Infrastructure;
import com.elcom.metacen.contact.model.dto.*;
import com.elcom.metacen.contact.repository.CustomInfrastructureRepository;
import com.elcom.metacen.contact.repository.InfrastructureRepository;
import com.elcom.metacen.contact.repository.rsql.KeywordDataRepository;
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
public class InfrastructureServiceImpl implements InfrastructureService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfrastructureServiceImpl.class);

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    InfrastructureRepository infrastructureRepository;

    @Autowired
    ObjectRelationshipService objectRelationshipService;

    @Autowired
    CustomInfrastructureRepository customInfrastructureRepository;

    @Autowired
    KeywordDataService keywordDataService;

    @Autowired
    SequenceGeneratorService sequenceGeneratorService;

    @Override
    public Infrastructure save(InfrastructureRequestDTO infrastructureRequestDTO, String createBy) {
        try {
            Infrastructure infrastructure = modelMapper.map(infrastructureRequestDTO, Infrastructure.class);
            infrastructure.setId(StringUtil.convertObject(DataSequenceStatus.I,sequenceGeneratorService.getNextSequenceId(Infrastructure.SEQUENCE_NAME)));
            infrastructure.setUuid(UUID.randomUUID().toString());
            infrastructure.setCreatedDate(DateUtils.convertToLocalDateTime(new Date()));
            infrastructure.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            infrastructure.setCreatedBy(createBy);
            infrastructure.setIsDeleted(DataDeleteStatus.NOT_DELETED.code());

            Infrastructure response = infrastructureRepository.save(infrastructure);
            // save relationship list
            List<ObjectRelationshipDTO> objectRelationshipDtoList = infrastructureRequestDTO.getRelationshipLst();
            if (objectRelationshipDtoList != null && !objectRelationshipDtoList.isEmpty()) {
                objectRelationshipDtoList.stream().forEach((item) -> {
                    item.setSourceObjectType(ObjectType.INFRASTRUCTURE.name());
                    item.setSourceObjectId(response.getUuid());
                });

                objectRelationshipService.save(objectRelationshipDtoList);
            }
            // save keyword list
            List<String> keywordIds = infrastructureRequestDTO.getKeywordLst();
            if (keywordIds != null && !keywordIds.isEmpty()) {
                KeywordDataDTO keywordDataDTO = new KeywordDataDTO();
                keywordDataDTO.setRefType(ObjectType.INFRASTRUCTURE.name());
                keywordDataDTO.setRefId(response.getUuid());
                keywordDataDTO.setKeywordIds(keywordIds);

                keywordDataService.save(keywordDataDTO);
            }
            return response;
        } catch (Exception ex) {
            LOGGER.error("Save Infrastructure failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public Infrastructure findByUuid(String uuid) {
        try {
            return infrastructureRepository.findByUuidAndIsDeleted(uuid, DataDeleteStatus.NOT_DELETED.code());
        } catch (Exception ex) {
            LOGGER.error("find Infrastructure failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public Infrastructure updateInfrastructure(Infrastructure infrastructure, InfrastructureRequestDTO infrastructureRequestDTO, String modifiedBy) {
        try {
            if (!StringUtil.isNullOrEmpty(infrastructureRequestDTO.getName())) {
                infrastructure.setName(infrastructureRequestDTO.getName());
            }
            infrastructure.setLocation(infrastructureRequestDTO.getLocation());
            infrastructure.setCountryId(infrastructureRequestDTO.getCountryId());
            infrastructure.setInfrastructureType(infrastructureRequestDTO.getInfrastructureType());
            infrastructure.setArea(infrastructureRequestDTO.getArea());
            infrastructure.setDescription(infrastructureRequestDTO.getDescription());
            infrastructure.setSideId(infrastructureRequestDTO.getSideId());
            if (infrastructureRequestDTO.getImageLst() != null) {
                infrastructure.setImageLst(infrastructureRequestDTO.getImageLst());
            }
            if (infrastructureRequestDTO.getFileAttachmentLst() != null) {
                infrastructure.setFileAttachmentLst(infrastructureRequestDTO.getFileAttachmentLst());
            }

            infrastructure.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            infrastructure.setModifiedBy(modifiedBy);
            Infrastructure response = infrastructureRepository.save(infrastructure);
            // save relationship list
            List<ObjectRelationshipDTO> objectRelationshipDtoList = infrastructureRequestDTO.getRelationshipLst();
            if (objectRelationshipDtoList != null && !objectRelationshipDtoList.isEmpty()) {
                objectRelationshipDtoList.stream().forEach((item) -> {
                    item.setSourceObjectType(ObjectType.INFRASTRUCTURE.name());
                    item.setSourceObjectId(response.getUuid());
                });
            }
            objectRelationshipService.update(infrastructure.getUuid(), objectRelationshipDtoList);

            // save keyword list
            List<String> keywordIds = infrastructureRequestDTO.getKeywordLst();
            KeywordDataDTO keywordDataDTO = new KeywordDataDTO();
            keywordDataDTO.setRefType(ObjectType.INFRASTRUCTURE.name());
            keywordDataDTO.setRefId(response.getUuid());
            keywordDataDTO.setKeywordIds(keywordIds);

            keywordDataService.update(keywordDataDTO);
            return response;
        } catch (Exception ex) {
            LOGGER.error("Update People failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public Page<InfrastructureResponseDTO> findListInfrastructure(InfrastructureFilterDTO infrastructureFilterDTO) {
        Integer page = infrastructureFilterDTO.getPage() > 0 ? infrastructureFilterDTO.getPage() : 0;
        Pageable pageable = PageRequest.of(page, infrastructureFilterDTO.getSize());

        return customInfrastructureRepository.search(infrastructureFilterDTO, pageable);
    }

    @Override
    public Infrastructure delete(Infrastructure infrastructure, String modifiedBy) {
        try {
            infrastructure.setModifiedBy(modifiedBy);
            infrastructure.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            infrastructure.setIsDeleted(DataDeleteStatus.DELETED.code());
            return infrastructureRepository.save(infrastructure);
        } catch (Exception ex) {
            LOGGER.error("Delete Infrastructure failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public InfrastructureResponseDTO findInfrastructureByUuid(String uuid) {
        InfrastructureResponseDTO infrastructureResponseDTO = customInfrastructureRepository.findInfrastructureByUuid(uuid);
        return infrastructureResponseDTO;
    }
}
