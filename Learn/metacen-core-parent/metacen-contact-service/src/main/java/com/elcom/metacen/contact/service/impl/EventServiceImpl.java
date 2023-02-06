package com.elcom.metacen.contact.service.impl;

import com.elcom.metacen.contact.model.Event;
import com.elcom.metacen.contact.model.dto.EventDTO.EventFilterDTO;
import com.elcom.metacen.contact.model.dto.EventDTO.EventRequestDTO;
import com.elcom.metacen.contact.model.dto.EventDTO.EventResponseDTO;
import com.elcom.metacen.contact.model.dto.KeywordDataDTO;
import com.elcom.metacen.contact.model.dto.ObjectRelationshipDTO;
import com.elcom.metacen.contact.repository.CustomEventRepository;
import com.elcom.metacen.contact.repository.EventRepository;
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
public class EventServiceImpl implements EventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventServiceImpl.class);

    @Autowired
    EventRepository eventRepository;

    @Autowired
    CustomEventRepository customEventRepository;

    @Autowired
    ObjectRelationshipService objectRelationshipService;

    @Autowired
    KeywordDataService keywordDataService;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    SequenceGeneratorService sequenceGeneratorService;

    public Event save(EventRequestDTO eventRequestDTO, String createBy) {
        try {
            //insert Event
            Event event = modelMapper.map(eventRequestDTO, Event.class);
            event.setId(StringUtil.convertObject(DataSequenceStatus.E,sequenceGeneratorService.getNextSequenceId(Event.SEQUENCE_NAME)));
            event.setUuid(UUID.randomUUID().toString());
            event.setCreatedDate(DateUtils.convertToLocalDateTime(new Date()));
            event.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            event.setCreatedBy(createBy);
            event.setIsDeleted(DataDeleteStatus.NOT_DELETED.code());
            Event response = eventRepository.save(event);
            // save relationship list
            List<ObjectRelationshipDTO> objectRelationshipDtoList = eventRequestDTO.getRelationshipLst();
            if (objectRelationshipDtoList != null && !objectRelationshipDtoList.isEmpty()) {
                objectRelationshipDtoList.stream().forEach((item) -> {
                    item.setSourceObjectType(ObjectType.EVENT.name());
                    item.setSourceObjectId(response.getUuid());
                });

                objectRelationshipService.save(objectRelationshipDtoList);
            }
            // save keyword list
            List<String> keywordIds = eventRequestDTO.getKeywordLst();
            if (keywordIds != null && !keywordIds.isEmpty()) {
                KeywordDataDTO keywordDataDTO = new KeywordDataDTO();
                keywordDataDTO.setRefType(ObjectType.EVENT.name());
                keywordDataDTO.setRefId(response.getUuid());
                keywordDataDTO.setKeywordIds(keywordIds);

                keywordDataService.save(keywordDataDTO);
            }
            return response;
        } catch (Exception e) {
            LOGGER.error("Save event failed >>> {}", e.toString());
            return null;
        }
    }

    public Event updateEvent(Event event,EventRequestDTO eventRequestDTO,String modifiedBy) {
        try {
            if (!StringUtil.isNullOrEmpty(eventRequestDTO.getName())) {
                event.setName(eventRequestDTO.getName());
            }
            event.setStartTime(eventRequestDTO.getStartTime());
            event.setStopTime(eventRequestDTO.getStopTime());
            event.setDescription(eventRequestDTO.getDescription());
            event.setSideId(eventRequestDTO.getSideId());
            event.setArea(eventRequestDTO.getArea());
            if(eventRequestDTO.getImageLst() != null){
                event.setImageLst(eventRequestDTO.getImageLst());
            }
            if(eventRequestDTO.getFileAttachmentLst() != null){
                event.setFileAttachmentLst(eventRequestDTO.getFileAttachmentLst());
            }
            event.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            event.setModifiedBy(modifiedBy);
            Event response = eventRepository.save(event);

            // save relationship list
            List<ObjectRelationshipDTO> objectRelationshipDtoList = eventRequestDTO.getRelationshipLst();
            if (objectRelationshipDtoList != null && !objectRelationshipDtoList.isEmpty()) {
                objectRelationshipDtoList.stream().forEach((item) -> {
                    item.setSourceObjectType(ObjectType.EVENT.name());
                    item.setSourceObjectId(response.getUuid());
                });
            }
            objectRelationshipService.update(event.getUuid(), objectRelationshipDtoList);

            // save keyword list
            List<String> keywordIds = eventRequestDTO.getKeywordLst();
            KeywordDataDTO keywordDataDTO = new KeywordDataDTO();
            keywordDataDTO.setRefType(ObjectType.EVENT.name());
            keywordDataDTO.setRefId(response.getUuid());
            keywordDataDTO.setKeywordIds(keywordIds);

            keywordDataService.update(keywordDataDTO);
            return response;
        } catch (Exception e) {
            LOGGER.error("Update event failed >>> {}", e.toString());
            return null;
        }
    }

    @Override
    public Page<EventResponseDTO> findListEvent(EventFilterDTO eventFilterDTO) {
        Integer page = eventFilterDTO.getPage() > 0 ? eventFilterDTO.getPage() : 0;
        Pageable pageable = PageRequest.of(page, eventFilterDTO.getSize());

        return customEventRepository.search(eventFilterDTO, pageable);
    }

    @Override
    public EventResponseDTO findEventByUuid(String uuid) {
        EventResponseDTO eventResponseDTO = customEventRepository.findEventByUuid(uuid);
        return eventResponseDTO;
    }

    public Event findById(String uuid) {
        try {
            return eventRepository.findByUuidAndIsDeleted(uuid, DataDeleteStatus.NOT_DELETED.code());
        } catch (Exception e) {
            LOGGER.error("Find by uuid failed >>> {}", e.toString());
            return null;
        }
    }

    public Event deleteEvent(Event event,String modifiedBy) {
        try {
            event.setModifiedBy(modifiedBy);
            event.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            event.setIsDeleted(DataDeleteStatus.DELETED.code());
            return eventRepository.save(event);
        } catch (Exception ex) {
            LOGGER.error("Delete event failed >>> {}", ex.toString());
            return null;
        }
    }
}
