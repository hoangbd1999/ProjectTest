package com.elcom.metacen.mapping.data.service.impl;

import com.elcom.metacen.mapping.data.constant.Constant;
import com.elcom.metacen.mapping.data.model.AbstractDocument;
import com.elcom.metacen.mapping.data.model.MappingAisMetacen;
import com.elcom.metacen.mapping.data.model.MappingVsatMetacen;
import com.elcom.metacen.mapping.data.model.dto.*;
import com.elcom.metacen.mapping.data.repository.CustomMappingAisMetacenRepository;
import com.elcom.metacen.mapping.data.repository.MappingAisMetacenRepository;
import com.elcom.metacen.mapping.data.service.MappingAisMetacenService;
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

import java.lang.reflect.Type;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class MappingAisMetacenServiceImpl implements MappingAisMetacenService {

    private Logger LOGGER = LoggerFactory.getLogger(MappingAisMetacenServiceImpl.class);

    @Autowired
    private MappingAisMetacenRepository mappingAisMetacenRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private CustomMappingAisMetacenRepository customMappingAisMetacenRepository;

    @Autowired
    private RedisTemplate redisTemplate;

    String key = Constant.REDIS_MAPPING_AIS_LST_KEY;

    @Override
    public MappingAisMetacen save(MappingAisRequestDTO mappingAisRequestDTO, String createBy) {
        try {
            MappingAisMetacen mappingAisMetacen = modelMapper.map(mappingAisRequestDTO, MappingAisMetacen.class);
            mappingAisMetacen.setUuid(UUID.randomUUID().toString());
            mappingAisMetacen.setCreatedDate(DateUtils.convertToLocalDateTime(new Date()));
            mappingAisMetacen.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            mappingAisMetacen.setCreatedBy(createBy);

            MappingAisMetacen response = mappingAisMetacenRepository.save(mappingAisMetacen);
            redisTemplate.opsForHash().put(key, mappingAisMetacen.getUuid(),mappingAisMetacen);
            return response;
        } catch (Exception ex) {
            LOGGER.error("Save MappingAis failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public MappingAisMetacen updateMappingAis(MappingAisMetacen mappingAisMetacen, MappingAisRequestDTO mappingAisRequestDTO, String modifiedBy) {
        try {
            if ((mappingAisRequestDTO.getAisMmsi() != null)) {
                mappingAisMetacen.setAisMmsi(mappingAisRequestDTO.getAisMmsi());
            }
            mappingAisMetacen.setAisShipName(mappingAisRequestDTO.getAisShipName());
            mappingAisMetacen.setObjectType(mappingAisRequestDTO.getObjectType());
            mappingAisMetacen.setObjectId(mappingAisRequestDTO.getObjectId());
            mappingAisMetacen.setObjectUuid(mappingAisRequestDTO.getObjectUuid());
            mappingAisMetacen.setObjectName(mappingAisRequestDTO.getObjectName());

            mappingAisMetacen.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            mappingAisMetacen.setModifiedBy(modifiedBy);

            MappingAisMetacen response = mappingAisMetacenRepository.save(mappingAisMetacen);
            redisTemplate.opsForHash().put(key, mappingAisMetacen.getUuid(),mappingAisMetacen);
            return response;
        } catch (Exception ex) {
            LOGGER.error("Update MappingAis failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public Page<MappingAisResponseDTO> findListMappingAis(MappingAisFilterDTO mappingAisFilterDTO) {
        try {
            if (redisTemplate.hasKey(key)) {
                List<MappingAisMetacen> lst = (List<MappingAisMetacen>) this.redisTemplate.opsForHash().values(key);
                //filter
                if (mappingAisFilterDTO.getTerm() != null && mappingAisFilterDTO.getTerm() != "") {
                    lst = filter(lst, mappingAisFilterDTO.getTerm().toLowerCase());
                } else {
                    lst = filterElement(lst, mappingAisFilterDTO);
                }
                long total = lst.size();
                //sort
                lst = sortList(mappingAisFilterDTO.getSort(), lst);

                Integer page = mappingAisFilterDTO.getPage() > 0 ? mappingAisFilterDTO.getPage() : 0;
                Pageable pageable = PageRequest.of(page, mappingAisFilterDTO.getSize());
                lst = lst.stream().limit(pageable.getPageSize()).collect(Collectors.toList());

                List<MappingAisResponseDTO> result =
                        lst.stream().map(n -> modelMapper.map(n, MappingAisResponseDTO.class)).collect(Collectors.toList());
                return new PageImpl<>(result, pageable, total);
            } else {
                Integer page = mappingAisFilterDTO.getPage() > 0 ? mappingAisFilterDTO.getPage() : 0;
                Pageable pageable = PageRequest.of(page, mappingAisFilterDTO.getSize());
                return customMappingAisMetacenRepository.search(mappingAisFilterDTO, pageable);
            }
        } catch (Exception ex) {
            LOGGER.error("filter failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public MappingAisMetacen findByUuid(String uuid) {
        try {
            if (redisTemplate.hasKey(key)) {
                return (MappingAisMetacen) redisTemplate.opsForHash().get(key, uuid);
            } else {
                return mappingAisMetacenRepository.findByUuid(uuid);
            }
        } catch (Exception ex) {
            LOGGER.error("find MappingAis failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public MappingAisMetacen delete(MappingAisMetacen mappingAisMetacen) {
        try {
            mappingAisMetacenRepository.delete(mappingAisMetacen);
            if (this.redisTemplate.hasKey(key)) {
                redisTemplate.opsForHash().delete(key, mappingAisMetacen.getUuid());
            }
            return null;
        } catch (Exception ex) {
            LOGGER.error("delete MappingAis failed >>> {}", ex.toString());
            return null;
        }
    }
    private List<MappingAisMetacen> filter(List<MappingAisMetacen> list, String term) {
     //   String value = covertToString(term).toLowerCase();
        if (list != null && !list.isEmpty()) {
       //     List<MappingAisMetacen> listFilter = covertToListString(list);
            List<MappingAisMetacen> listFilter = list.stream()
                    .filter(n -> (n.getAisMmsi().toString().trim().contains(term)) ||
                            (n.getAisShipName().toLowerCase().contains(term)) || (n.getObjectType().toLowerCase().contains(term)) ||
                            (n.getObjectName().toLowerCase().contains(term)) || (n.getObjectId().toLowerCase().contains(term))).collect(Collectors.toList());
            return listFilter;
        }
        return list;
    }
//    public static String covertToString(String value) {
//        try {
//            String temp = Normalizer.normalize(value, Normalizer.Form.NFD);
//            Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
//            return pattern.matcher(temp).replaceAll("");
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return null;
//    }
//    public static List<MappingAisMetacen> covertToListString(List<MappingAisMetacen> list) {
//        try {
//            for(int i = 0; i <= list.size(); i++){
//                String temp = Normalizer.normalize(list.get(i).getObjectName(), Normalizer.Form.NFD);
//                Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
//                String a = pattern.matcher(temp).replaceAll("");
//                list.get(i).setObjectName(a);
//            }
//            return list;
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return null;
//    }

    private List<MappingAisMetacen> filterElement(List<MappingAisMetacen> list, MappingAisFilterDTO mappingAisFilterDTO) {
        if (list != null && !list.isEmpty()) {
            Integer aisMmsi = mappingAisFilterDTO.getAisMmsi();
            String objectId = mappingAisFilterDTO.getObjectId().toLowerCase();
            List<String> objectTypes = mappingAisFilterDTO.getObjectTypes();
//            if (aisMmsi == null || aisMmsi.equals("") && objectId == null || objectId == "" && objectTypes == null && objectTypes.isEmpty()) {
//                return list;
//            }
            List<MappingAisMetacen> result = new LinkedList<>();
            for (MappingAisMetacen loop : list) {
                if (aisMmsi != null && !aisMmsi.equals("") && loop.getAisMmsi().toString().contains(String.valueOf(aisMmsi))) {
                    result.add(loop);
                    continue;
                }
                if (objectId != null && objectId != "" && loop.getObjectId().toLowerCase().contains(objectId)) {
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

    private List<MappingAisMetacen> sortList(String sort, List<MappingAisMetacen> list) {
        if (list != null && !list.isEmpty()) {
            if (sort != null && sort != "") {
                String preSort = sort.substring(0, 1);
                String subSort = sort.substring(1);

                Collections.sort(list, (o1, o2) -> {
                    if (subSort.equalsIgnoreCase("aisShipName")) {
                        if (preSort.equalsIgnoreCase("+")) {
                            return o1.getAisShipName().compareTo(o2.getAisShipName());
                        } else {
                            return o2.getAisShipName().compareTo(o1.getAisShipName());
                        }
                    } else if (subSort.equalsIgnoreCase("aisMmsi")) {
                        if (preSort.equalsIgnoreCase("+")) {
                            return o1.getAisMmsi().compareTo(o2.getAisMmsi());
                        } else {
                            return o2.getAisMmsi().compareTo(o1.getAisMmsi());
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

}
