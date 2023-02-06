/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.service.impl;

import com.elcom.metacen.contact.service.*;
import com.elcom.metacen.enums.DataDeleteStatus;
import com.elcom.metacen.contact.model.Organisation;
import com.elcom.metacen.contact.model.dto.CommentDTO;
import com.elcom.metacen.contact.model.dto.KeywordDataDTO;
import com.elcom.metacen.contact.model.dto.ObjectRelationshipDTO;
import com.elcom.metacen.contact.model.dto.OrganisationDTO;
import com.elcom.metacen.contact.model.dto.OrganisationRequestDTO;
import com.elcom.metacen.contact.model.dto.OrganisationFilterDTO;
import com.elcom.metacen.contact.repository.CustomOrganisationRepository;
import com.elcom.metacen.enums.DataSequenceStatus;
import com.elcom.metacen.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.elcom.metacen.contact.repository.OrganisationRepository;
import com.elcom.metacen.enums.ObjectType;
import com.elcom.metacen.utils.DateUtils;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * @author Admin
 */
@Service
public class OrganisationServiceImpl implements OrganisationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrganisationServiceImpl.class);

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    CustomOrganisationRepository customOrganisationRepository;

    @Autowired
    KeywordDataService keywordDataService;

    @Autowired
    ObjectRelationshipService objectRelationshipService;

    @Autowired
    SequenceGeneratorService sequenceGeneratorService;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public Page<OrganisationDTO> findOrganisations(OrganisationFilterDTO organisationFilterDTO) {
        Integer page = organisationFilterDTO.getPage() > 0 ? organisationFilterDTO.getPage() : 0;
        Pageable pageable = PageRequest.of(page, organisationFilterDTO.getSize());

        return customOrganisationRepository.search(organisationFilterDTO, pageable);
    }

    @Override
    public OrganisationDTO findOrganisationByUuid(String uuid) {
        OrganisationDTO organisationDTO = customOrganisationRepository.findByUuid(uuid);
        return organisationDTO;
    }

    @Override
    public Organisation findById(String uuid) {
        Organisation organisation = organisationRepository.findByUuidAndIsDeleted(uuid, DataDeleteStatus.NOT_DELETED.code());
        return organisation;
    }

    @Override
    public Organisation save(OrganisationRequestDTO organisationRequestDTO, String username) {
        try {
            Organisation organisation = modelMapper.map(organisationRequestDTO, Organisation.class);
            organisation.setId(StringUtil.convertObject(DataSequenceStatus.O, sequenceGeneratorService.getNextSequenceId(Organisation.SEQUENCE_NAME)));
            organisation.setUuid(UUID.randomUUID().toString());
            organisation.setIsDeleted(DataDeleteStatus.NOT_DELETED.code());
            organisation.setCreatedBy(username);
            organisation.setCreatedDate(DateUtils.convertToLocalDateTime(new Date()));
            organisation.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));

            Organisation response = organisationRepository.save(organisation);

            // save relationship list
            List<ObjectRelationshipDTO> objectRelationshipDtoList = organisationRequestDTO.getRelationshipLst();
            if (objectRelationshipDtoList != null && !objectRelationshipDtoList.isEmpty()) {
                objectRelationshipDtoList.stream().forEach((item) -> {
                    item.setSourceObjectType(ObjectType.ORGANISATION.name());
                    item.setSourceObjectId(response.getUuid());
                });

                objectRelationshipService.save(objectRelationshipDtoList);
            }

            // save keyword list
            List<String> keywordIds = organisationRequestDTO.getKeywordLst();
            if (keywordIds != null && !keywordIds.isEmpty()) {
                KeywordDataDTO keywordDataDTO = new KeywordDataDTO();
                keywordDataDTO.setRefType(ObjectType.ORGANISATION.name());
                keywordDataDTO.setRefId(response.getUuid());
                keywordDataDTO.setKeywordIds(keywordIds);

                keywordDataService.save(keywordDataDTO);
            }

            return response;
        } catch (Exception ex) {
            LOGGER.error("Save organisation failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public Organisation update(Organisation organisation, OrganisationRequestDTO organisationRequestDTO, String username) {
        try {
            organisation.setName(organisationRequestDTO.getName());
            organisation.setOrganisationType(organisationRequestDTO.getOrganisationType());
            organisation.setCountryId(organisationRequestDTO.getCountryId());
            organisation.setHeadquarters(organisationRequestDTO.getHeadquarters());
            organisation.setSideId(organisationRequestDTO.getSideId());
            organisation.setDescription(organisationRequestDTO.getDescription());
            organisation.setImageLst(organisationRequestDTO.getImageLst());
            organisation.setFileAttachmentLst(organisationRequestDTO.getFileAttachmentLst());
            organisation.setModifiedBy(username);
            organisation.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));

            Organisation response = organisationRepository.save(organisation);

            // save relationship list
            List<ObjectRelationshipDTO> objectRelationshipDtoList = organisationRequestDTO.getRelationshipLst();
            if (objectRelationshipDtoList != null && !objectRelationshipDtoList.isEmpty()) {
                objectRelationshipDtoList.stream().forEach((item) -> {
                    item.setSourceObjectType(ObjectType.ORGANISATION.name());
                    item.setSourceObjectId(response.getUuid());
                });
            }
            objectRelationshipService.update(organisation.getUuid(), objectRelationshipDtoList);

            // save keyword list
            List<String> keywordIds = organisationRequestDTO.getKeywordLst();
            KeywordDataDTO keywordDataDTO = new KeywordDataDTO();
            keywordDataDTO.setRefType(ObjectType.ORGANISATION.name());
            keywordDataDTO.setRefId(response.getUuid());
            keywordDataDTO.setKeywordIds(keywordIds);
            keywordDataService.update(keywordDataDTO);

            return response;
        } catch (Exception ex) {
            LOGGER.error("Update organisation failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public Organisation delete(Organisation organisation, String username) {
        try {
            organisation.setModifiedBy(username);
            organisation.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            organisation.setIsDeleted(DataDeleteStatus.DELETED.code());

            Organisation response = organisationRepository.save(organisation);
            return response;
        } catch (Exception ex) {
            LOGGER.error("Delete organisation failed >>> {}", ex.toString());
            return null;
        }
    }

//    @Override
//    public List<Organisation> findOrganisationsByUuidList(List<String> uuidLst) {
//        return organisationRepository.findByUuidInAndIsDeleted(uuidLst, DataDeleteStatus.NOT_DELETED.code());
//    }

    private CommentDTO buildCommentDTO(String content, String commentBy) {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setUuid(UUID.randomUUID().toString());
        commentDTO.setContent(content);
        commentDTO.setCreatedBy(commentBy);
        commentDTO.setCreatedDate(DateUtils.convertToLocalDateTime(new Date()));
        commentDTO.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));

        return commentDTO;
    }

}
