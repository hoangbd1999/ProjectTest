/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.mapping.data.service.impl;

import com.elcom.metacen.mapping.data.constant.Constant;
import com.elcom.metacen.mapping.data.model.AbstractDocument;
import com.elcom.metacen.mapping.data.model.MappingVsatMetacen;
import com.elcom.metacen.mapping.data.model.dto.MappingVsatFilterDTO;
import com.elcom.metacen.mapping.data.model.dto.MappingVsatRequestDTO;
import com.elcom.metacen.mapping.data.model.dto.MappingVsatResponseDTO;
import com.elcom.metacen.mapping.data.repository.CustomMappingVsatMetacenRepository;
import com.elcom.metacen.mapping.data.repository.MappingVsatMetacenRepository;
import com.elcom.metacen.mapping.data.service.MappingVsatMetacenService;
import com.elcom.metacen.utils.DateUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Admin
 */
@Service
public class MappingVsatMetacenServiceImpl implements MappingVsatMetacenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MappingVsatMetacenServiceImpl.class);
    final String key = Constant.REDIS_MAPPING_VSAT_LST_KEY;
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
         //   redisTemplate.opsForHash().put(key, mappingVsatMetacen.getUuid(), mappingVsatMetacen);

            return response;
        } catch (Exception ex) {
            LOGGER.error("Save MappingVsat failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public MappingVsatMetacen findByUuid(String uuid) {
        try {
            if (redisTemplate.hasKey(key)) {
                return (MappingVsatMetacen) redisTemplate.opsForHash().get(key, uuid);
            } else {
                return mappingVsatMetacenRepository.findByUuid(uuid);
            }
        } catch (Exception ex) {
            LOGGER.error("find MappingVsat failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public List<MappingVsatMetacen> findByObjectUuid(String objectUuid) {
        try {
            return mappingVsatMetacenRepository.findByObjectUuidIn(objectUuid);
        } catch (Exception ex) {
            LOGGER.error("find MappingVsat failed >>> {}", ex.toString());
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
          //  redisTemplate.opsForHash().put(key, mappingVsatMetacen.getUuid(), mappingVsatMetacen);

            return response;
        } catch (Exception ex) {
            LOGGER.error("Update MappingVsat failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public List<MappingVsatMetacen> updateNameObjectInternal(List<MappingVsatMetacen> mappingVsatMetacen, String objectName) {
        try {
            for (int i = 0; i < mappingVsatMetacen.size(); i++) {
                mappingVsatMetacen.get(i).setObjectName(objectName);
            }
            return mappingVsatMetacenRepository.saveAll(mappingVsatMetacen);
        } catch (Exception ex) {
            LOGGER.error("Save MappingVsat failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public Page<MappingVsatResponseDTO> findListMappingVsat(MappingVsatFilterDTO mappingVsatFilterDTO) {
        try {
            if (redisTemplate.hasKey(key)) {
                List<MappingVsatMetacen> lst = (List<MappingVsatMetacen>) this.redisTemplate.opsForHash().values(key);
                //filter
                if (mappingVsatFilterDTO.getTerm() != null && mappingVsatFilterDTO.getTerm() != "") {
                    lst = filter(lst, mappingVsatFilterDTO.getTerm().toLowerCase());
                } else {
                    lst = filterElement(lst, mappingVsatFilterDTO);
                }
                long total = lst.size();
                //sort
                lst = sortList(mappingVsatFilterDTO.getSort(), lst);

                Integer page = mappingVsatFilterDTO.getPage() > 0 ? mappingVsatFilterDTO.getPage() : 0;
                Pageable pageable = PageRequest.of(page, mappingVsatFilterDTO.getSize());
                lst = lst.stream().limit(pageable.getPageSize()).collect(Collectors.toList());

                List<MappingVsatResponseDTO> result =
                        lst.stream().map(n -> modelMapper.map(n, MappingVsatResponseDTO.class)).collect(Collectors.toList());
                return new PageImpl<>(result, pageable, total);

            } else {
                Integer page = mappingVsatFilterDTO.getPage() > 0 ? mappingVsatFilterDTO.getPage() : 0;
                Pageable pageable = PageRequest.of(page, mappingVsatFilterDTO.getSize());
                return customMappingVsatMetacenRepository.search(mappingVsatFilterDTO, pageable);
            }
        } catch (Exception ex) {
            LOGGER.error("filter failed >>> {}", ex.toString());
            return null;
        }
    }


    private List<MappingVsatMetacen> sortList(String sort, List<MappingVsatMetacen> list) {
        if (list != null && !list.isEmpty()) {
            if (sort != null && sort != "") {
                String preSort = sort.substring(0, 1);
                String subSort = sort.substring(1);

                Collections.sort(list, (o1, o2) -> {
                    if (subSort.equalsIgnoreCase("vsatDataSourceName")) {
                        if (preSort.equalsIgnoreCase("+")) {
                            return o1.getVsatDataSourceName().compareTo(o2.getVsatDataSourceName());
                        } else {
                            return o2.getVsatDataSourceName().compareTo(o1.getVsatDataSourceName());
                        }
                    } else if (subSort.equalsIgnoreCase("vsatIpAddress")) {
                        if (preSort.equalsIgnoreCase("+")) {
                            return o1.getVsatIpAddress().compareTo(o2.getVsatIpAddress());
                        } else {
                            return o2.getVsatIpAddress().compareTo(o1.getVsatIpAddress());
                        }
                    } else if (subSort.equalsIgnoreCase("objectType")) {
                        if (preSort.equalsIgnoreCase("+")) {
                            return o1.getObjectType().compareTo(o2.getObjectType());
                        } else {
                            return o2.getObjectType().compareTo(o1.getObjectType());
                        }
                    } else if (subSort.equalsIgnoreCase("objectId")) {
                        if (preSort.equalsIgnoreCase("+")) {
                            return o1.getObjectId().compareTo(o2.getObjectId());
                        } else {
                            return o2.getObjectId().compareTo(o1.getObjectId());
                        }
                    } else if (subSort.equalsIgnoreCase("objectName")) {
                        if (preSort.equalsIgnoreCase("+")) {
                            return o1.getObjectName().compareTo(o2.getObjectName());
                        } else {
                            return o2.getObjectName().compareTo(o1.getObjectName());
                        }
                    } else {
                        if (preSort.equalsIgnoreCase("+")) {
                            return o1.getCreatedDate().compareTo(o2.getCreatedDate());
                        } else {
                            return o2.getCreatedDate().compareTo(o1.getCreatedDate());
                        }
                    }
                });
            } else {
                Collections.sort(list, Comparator.comparing(AbstractDocument::getCreatedDate));
            }
        }
        return list;
    }


    private List<MappingVsatMetacen> filter(List<MappingVsatMetacen> list, String term) {
        if (list != null && !list.isEmpty()) {
            List<MappingVsatMetacen> listFilter = list.stream()
                    .filter(n -> (n.getVsatDataSourceName().toLowerCase().contains(term)) ||
                            (n.getObjectType().toLowerCase().contains(term)) || (n.getObjectId().toLowerCase().contains(term)) ||
                            (n.getObjectName().toLowerCase().contains(term)) || (n.getVsatIpAddress().toLowerCase().contains(term))).collect(Collectors.toList());
            return listFilter;
        }
        return list;
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
            if (this.redisTemplate.hasKey(key)) {
                redisTemplate.opsForHash().delete(key, mappingVsatMetacen.getUuid());
            }
            return null;
        } catch (Exception ex) {
            LOGGER.error("delete MappingVsat failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public MappingVsatMetacen checkExistMapping(MappingVsatRequestDTO mappingVsatRequestDTO) {
        return mappingVsatMetacenRepository.findFirstByVsatIpAddressAndVsatDataSourceId(
                mappingVsatRequestDTO.getVsatIpAddress(),
                mappingVsatRequestDTO.getVsatDataSourceId());
    }

    @Override
    public MappingVsatMetacen checkExistMappingByObjectUuid(MappingVsatRequestDTO mappingVsatRequestDTO) {
        return mappingVsatMetacenRepository.findFirstByObjectTypeAndObjectUuid(
                mappingVsatRequestDTO.getObjectType(),
                mappingVsatRequestDTO.getObjectUuid());
    }
}
