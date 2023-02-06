package com.elcom.metacen.contact.service.impl;

import com.elcom.metacen.contact.model.dto.*;
import com.elcom.metacen.contact.repository.CustomPeopleRepository;
import com.elcom.metacen.contact.service.*;
import com.elcom.metacen.enums.DataDeleteStatus;
import com.elcom.metacen.contact.model.People;
import com.elcom.metacen.contact.repository.PeopleRepository;
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

import java.util.*;

/**
 * @author hoangbd
 */
@Service
public class PeopleServiceImpl implements PeopleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PeopleServiceImpl.class);

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    PeopleRepository peopleRepository;

    @Autowired
    ObjectRelationshipService objectRelationshipService;

    @Autowired
    KeywordDataService keywordDataService;

    @Autowired
    CustomPeopleRepository customPeopleRepository;

    @Autowired
    SequenceGeneratorService sequenceGeneratorService;

    @Override
    public People save(PeopleRequestDTO peopleRequestDTO, String createBy) {
        try {
            People people = modelMapper.map(peopleRequestDTO, People.class);
            people.setId(StringUtil.convertObject(DataSequenceStatus.P, sequenceGeneratorService.getNextSequenceId(People.SEQUENCE_NAME)));
            people.setUuid(UUID.randomUUID().toString());
            people.setCreatedDate(DateUtils.convertToLocalDateTime(new Date()));
            people.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            people.setCreatedBy(createBy);
            people.setIsDeleted(DataDeleteStatus.NOT_DELETED.code());

            People response = peopleRepository.save(people);
            // save relationship list
            List<ObjectRelationshipDTO> objectRelationshipDtoList = peopleRequestDTO.getRelationshipLst();
            if (objectRelationshipDtoList != null && !objectRelationshipDtoList.isEmpty()) {
                objectRelationshipDtoList.stream().forEach((item) -> {
                    item.setSourceObjectType(ObjectType.PEOPLE.name());
                    item.setSourceObjectId(response.getUuid());
                });

                objectRelationshipService.save(objectRelationshipDtoList);
            }
            // save keyword list
            List<String> keywordIds = peopleRequestDTO.getKeywordLst();
            if (keywordIds != null && !keywordIds.isEmpty()) {
                KeywordDataDTO keywordDataDTO = new KeywordDataDTO();
                keywordDataDTO.setRefType(ObjectType.PEOPLE.name());
                keywordDataDTO.setRefId(response.getUuid());
                keywordDataDTO.setKeywordIds(keywordIds);

                keywordDataService.save(keywordDataDTO);
            }
            return response;
        } catch (Exception ex) {
            LOGGER.error("Save People failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public People findByUuid(String uuid) {
        try {
            return peopleRepository.findByUuidAndIsDeleted(uuid, DataDeleteStatus.NOT_DELETED.code());
        } catch (Exception ex) {
            LOGGER.error("find People failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public People updatePeople(People people, PeopleRequestDTO peopleRequestDTO, String modifiedBy) {
        try {
            people.setName(peopleRequestDTO.getName());
            people.setMobileNumber(peopleRequestDTO.getMobileNumber());
            people.setEmail(peopleRequestDTO.getEmail());
            people.setCountryId(peopleRequestDTO.getCountryId());
            people.setDateOfBirth(peopleRequestDTO.getDateOfBirth());
            people.setGender(peopleRequestDTO.getGender());
            people.setAddress(peopleRequestDTO.getAddress());
            people.setLevel(peopleRequestDTO.getLevel());
            people.setDescription(peopleRequestDTO.getDescription());
            people.setSideId(peopleRequestDTO.getSideId());
            people.setImageLst(peopleRequestDTO.getImageLst());
            people.setFileAttachmentLst(peopleRequestDTO.getFileAttachmentLst());
            people.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            people.setModifiedBy(modifiedBy);
            People response = peopleRepository.save(people);

            // save relationship list
            List<ObjectRelationshipDTO> objectRelationshipDtoList = peopleRequestDTO.getRelationshipLst();
            if (objectRelationshipDtoList != null && !objectRelationshipDtoList.isEmpty()) {
                objectRelationshipDtoList.stream().forEach((item) -> {
                    item.setSourceObjectType(ObjectType.PEOPLE.name());
                    item.setSourceObjectId(response.getUuid());
                });
            }
            objectRelationshipService.update(people.getUuid(), objectRelationshipDtoList);

            // save keyword list
            List<String> keywordIds = peopleRequestDTO.getKeywordLst();
            KeywordDataDTO keywordDataDTO = new KeywordDataDTO();
            keywordDataDTO.setRefType(ObjectType.PEOPLE.name());
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
    public Page<PeopleResponseDTO> findListPeople(PeopleFilterDTO peopleFilterDTO) {
        Integer page = peopleFilterDTO.getPage() > 0 ? peopleFilterDTO.getPage() : 0;
        Pageable pageable = PageRequest.of(page, peopleFilterDTO.getSize());

        return customPeopleRepository.search(peopleFilterDTO, pageable);
    }

    @Override
    public People delete(People people, String modifiedBy) {
        try {
            people.setModifiedBy(modifiedBy);
            people.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            people.setIsDeleted(DataDeleteStatus.DELETED.code());
            return peopleRepository.save(people);
        } catch (Exception ex) {
            LOGGER.error("Delete People failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public PeopleResponseDTO findPeopleByUuid(String uuid) {
        PeopleResponseDTO peopleResponseDTO = customPeopleRepository.findPeopleByUuid(uuid);
        return peopleResponseDTO;
    }
}
