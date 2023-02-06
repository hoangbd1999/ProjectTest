/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elcom.com.neo4j.service.impl;
import com.elcom.metacen.utils.DateUtils;
import elcom.com.neo4j.dto.MappingVsatFilterDTO;
import elcom.com.neo4j.dto.MappingVsatRequestDTO;
import elcom.com.neo4j.dto.MappingVsatResponseDTO;
import elcom.com.neo4j.model.MappingVsatMetacen;
import elcom.com.neo4j.repositorymogo.CustomMappingVsatMetacenRepository;
import elcom.com.neo4j.repositorymogo.MappingVsatMetacenRepository;
import elcom.com.neo4j.service.MappingVsatMetacenService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Admin
 */
@Service
public class MappingVsatMetacenServiceImpl implements MappingVsatMetacenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MappingVsatMetacenServiceImpl.class);
    @Autowired
    MappingVsatMetacenRepository mappingVsatMetacenRepository;

    @Autowired
    CustomMappingVsatMetacenRepository customMappingVsatMetacenRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public MappingVsatMetacen save(MappingVsatRequestDTO mappingVsatRequestDTO, String createBy) {
        try {
            MappingVsatMetacen mappingVsatMetacen = modelMapper.map(mappingVsatRequestDTO, MappingVsatMetacen.class);
            mappingVsatMetacen.setUuid(UUID.randomUUID().toString());
            mappingVsatMetacen.setCreatedDate(DateUtils.convertToLocalDateTime(new Date()));
            mappingVsatMetacen.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            mappingVsatMetacen.setCreatedBy(createBy);

            MappingVsatMetacen response = mappingVsatMetacenRepository.save(mappingVsatMetacen);

            return response;
        } catch (Exception ex) {
            LOGGER.error("Save MappingVsat failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public MappingVsatMetacen updateMappingVsat(MappingVsatMetacen mappingVsatMetacen, MappingVsatRequestDTO mappingVsatRequestDTO, String modifiedBy) {
        try {
            if ((mappingVsatRequestDTO.getVsatDataSourceId() != null)) {
                mappingVsatMetacen.setVsatDataSourceId(mappingVsatRequestDTO.getVsatDataSourceId());
            }
            mappingVsatMetacen.setVsatDataSourceName(mappingVsatRequestDTO.getVsatDataSourceName());
            mappingVsatMetacen.setVsatIpAddress(mappingVsatRequestDTO.getVsatIpAddress());
            mappingVsatMetacen.setObjectType(mappingVsatRequestDTO.getObjectType());
            mappingVsatMetacen.setObjectId(mappingVsatRequestDTO.getObjectId());
            mappingVsatMetacen.setObjectUuid(mappingVsatRequestDTO.getObjectUuid());
            mappingVsatMetacen.setObjectName(mappingVsatRequestDTO.getObjectName());

            mappingVsatMetacen.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            mappingVsatMetacen.setModifiedBy(modifiedBy);
            MappingVsatMetacen response = mappingVsatMetacenRepository.save(mappingVsatMetacen);

            return response;
        } catch (Exception ex) {
            LOGGER.error("Update MappingVsat failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public Page<MappingVsatResponseDTO> findListMappingVsat(MappingVsatFilterDTO mappingVsatFilterDTO) {
        try {
                Integer page = mappingVsatFilterDTO.getPage() > 0 ? mappingVsatFilterDTO.getPage() : 0;
                Pageable pageable = PageRequest.of(page, mappingVsatFilterDTO.getSize());
                return customMappingVsatMetacenRepository.search(mappingVsatFilterDTO, pageable);
        } catch (Exception ex) {
            LOGGER.error("filter failed >>> {}", ex.toString());
            return null;
        }
    }


    private List<MappingVsatMetacen> filterElement(List<MappingVsatMetacen> list, MappingVsatFilterDTO mappingVsatFilterDTO) {
        if (list != null && !list.isEmpty()) {
            String vsatIpAddress = mappingVsatFilterDTO.getVsatIpAddress().toLowerCase();
            String objectId = mappingVsatFilterDTO.getObjectId().toLowerCase();
            List<Integer> vsatDataSourceIds = mappingVsatFilterDTO.getVsatDataSourceIds();
            List<String> objectTypes = mappingVsatFilterDTO.getObjectTypes();
//            if (vsatIpAddress == null || vsatIpAddress == "" && objectId == null || objectId == ""
//                    && vsatDataSourceIds == null && vsatDataSourceIds.isEmpty() && objectTypes == null && objectTypes.isEmpty()) {
//                return list;
//            }
            List<MappingVsatMetacen> result = new LinkedList<>();
            for (MappingVsatMetacen loop : list) {
                if (vsatIpAddress != null && vsatIpAddress != "" && loop.getVsatIpAddress().toLowerCase().contains(vsatIpAddress)) {
                    result.add(loop);
                    continue;
                }
                if (objectId != null && objectId != "" && loop.getObjectId().toLowerCase().contains(objectId)) {
                    result.add(loop);
                    continue;
                }
                if (vsatDataSourceIds != null && !vsatDataSourceIds.isEmpty() && vsatDataSourceIds.contains(loop.getVsatDataSourceId())) {
                    result.add(loop);
                    continue;
                }
                if (objectTypes != null && !objectTypes.isEmpty() && objectTypes.contains(loop.getObjectType())) {
                    result.add(loop);
                    continue;
                }
            }
            if(result.isEmpty()){
                return list;
            }
            return result;
        }
        return list;
    }

    private List<MappingVsatMetacen> paging(List<MappingVsatMetacen> list, int page, int size) {
        int listSize = list.size();
        if (listSize <= size) {
            return list;
        } else {
            List<MappingVsatMetacen> listPaging = new LinkedList<>();
            for (int i = page * size; i < size; i++) {
                listPaging.add(list.get(i));
            }
            return listPaging;
        }
    }


    @Override
    public MappingVsatMetacen delete(MappingVsatMetacen mappingVsatMetacen) {
        try {
            mappingVsatMetacenRepository.delete(mappingVsatMetacen);
            return null;
        } catch (Exception ex) {
            LOGGER.error("delete MappingVsat failed >>> {}", ex.toString());
            return null;
        }
    }
}
