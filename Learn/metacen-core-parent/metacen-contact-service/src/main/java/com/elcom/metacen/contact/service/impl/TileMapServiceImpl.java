package com.elcom.metacen.contact.service.impl;

import com.elcom.metacen.contact.constant.Constant;
import com.elcom.metacen.contact.model.TileMap;
import com.elcom.metacen.contact.model.dto.TileMapDTO.TileMapFilterDTO;
import com.elcom.metacen.contact.model.dto.TileMapDTO.TileMapResponseDTO;
import com.elcom.metacen.contact.repository.EventRepository;
import com.elcom.metacen.contact.repository.rsql.CustomTileMapRepository;
import com.elcom.metacen.contact.repository.rsql.TileMapRepository;
import com.elcom.metacen.contact.service.*;

import com.elcom.metacen.utils.StringUtil;
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

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class TileMapServiceImpl implements TileMapService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TileMapServiceImpl.class);

    @Autowired
    EventRepository eventRepository;

    @Autowired
    CustomTileMapRepository customTileMapRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    TileMapRepository tileMapRepository;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Page<TileMapResponseDTO> findListTileMap(TileMapFilterDTO tileMapFilterDTO) {
        List<TileMap> lst = null;
        String key = Constant.REDIS_TILEMAPS_LST_KEY;
        try {
            if (redisTemplate.hasKey(key)) {
                lst = (List<TileMap>) this.redisTemplate.opsForList()
                        .range(key, 0, Constant.REDIS_TILEMAPS_LST_FETCH_MAX);
                //filter
                if (!StringUtil.isNullOrEmpty(tileMapFilterDTO.getTerm())) {
                    lst = filter(lst, tileMapFilterDTO.getTerm().toLowerCase());
                }
                if (!StringUtil.isNullOrEmpty(tileMapFilterDTO.getName())) {
                    lst = filterName(lst,tileMapFilterDTO.getName().toLowerCase());
                }
                long total = lst.size();

                Integer page = tileMapFilterDTO.getPage() > 0 ? tileMapFilterDTO.getPage() : 0;
                Pageable pageable = PageRequest.of(page, tileMapFilterDTO.getSize());
                lst = lst.stream().skip(pageable.getPageNumber() * pageable.getPageSize()).limit(pageable.getPageSize()).parallel().collect(Collectors.toList());

                List<TileMapResponseDTO> result =
                        lst.stream().map(n -> modelMapper.map(n, TileMapResponseDTO.class)).collect(Collectors.toList());
                return new PageImpl<>(result, pageable, total);
            } else {

                Integer page = tileMapFilterDTO.getPage() > 0 ? tileMapFilterDTO.getPage() : 0;
                Pageable pageable = PageRequest.of(page, tileMapFilterDTO.getSize());
                lst = (List<TileMap>) this.tileMapRepository.findAllByOrderByNameAsc();
                Long pushValStatus = this.redisTemplate.opsForList().rightPushAll(key, lst);
                if (pushValStatus != null && !pushValStatus.equals(0L)) {
                    this.redisTemplate.expire(key, 30, TimeUnit.DAYS);
                }
                return customTileMapRepository.search(tileMapFilterDTO, pageable);
            }
        } catch (Exception ex) {
            LOGGER.error("filter failed >>> {}", ex.toString());
            return null;
        }
    }

    private List<TileMap> filter(List<TileMap> list, String term) {
        if (list != null && !list.isEmpty()) {
            List<TileMap> listFilter = list.stream()
                    .filter(n -> (n.getName().toLowerCase().contains(term)) ||
                            (n.getCoordinates().toLowerCase().contains(term)) ).collect(Collectors.toList());
            return listFilter;
        }
        return list;
    }
    private List<TileMap> filterName(List<TileMap> list, String name) {
        if (list != null && !list.isEmpty()) {
            List<TileMap> listFilter = list.stream()
                    .filter(n -> (n.getName().toLowerCase().contains(name))).collect(Collectors.toList());
            return listFilter;
        }
        return list;
    }

}
