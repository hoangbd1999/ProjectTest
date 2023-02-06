package com.elcom.metacen.id.service;

import com.elcom.metacen.id.model.Unit;
import com.elcom.metacen.id.model.dto.UnitDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface UnitService {

    List<Unit> findAll();

    Page<Unit> findAll(Integer page, Integer size, Sort sort);

    Page<Unit> findAll(Integer page, Integer size, String search, Sort sort);

    Unit findByUuid(String uuid);

    void save(Unit unit);

    Unit deleteByUuid(String uuid);

    Unit findByEmail(String email);

    Unit findByPhone(String phone);

    Unit findByName(String name);

    Unit findByCode(String code);

    List<Unit> findByIds(List<String> ids);

    List<Unit> deleteByListId(List<String> ids);

    List<Unit> findUnitByStageIdList(List<String> stageIdList);

    void deleteStageData(List<String> stageIds);

    List<UnitDTO> transformWithPermission(List<Unit> unitList, String jobType);
}
