package com.elcom.metacen.id.service.impl;

import com.elcom.metacen.id.model.Unit;
import com.elcom.metacen.id.model.dto.UnitDTO;
import com.elcom.metacen.id.repository.UnitCustomizeRepository;
import com.elcom.metacen.id.repository.UnitRepository;
import com.elcom.metacen.id.service.UnitService;
import io.netty.util.internal.StringUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UnitServiceImpl implements UnitService {

    //private static final String URI = ApplicationConfig.ITS_ROOT_URL + "/v1.0/its/management/stage/siteId/";
    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private UnitCustomizeRepository unitCustomizeRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<Unit> findAll() {
        return (List<Unit>) unitRepository.findAll();
    }

    @Override
    public Page<Unit> findAll(Integer page, Integer size, Sort sort) {
        if (page > 0) {
            page--;
        }
        return unitRepository.findAll(PageRequest.of(page, size, sort));
    }

    @Override
    public Page<Unit> findAll(Integer page, Integer size, String search, Sort sort) {
        if (page > 0) {
            page--;
        }
        Pageable paging = PageRequest.of(page, size,sort);
        Page<Unit> result = null;
        if (StringUtil.isNullOrEmpty(search)) {
            result = unitRepository.findAll(paging);
        } else {
            result = unitRepository.findByCodeContainingIgnoreCaseOrNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrPhoneContainingIgnoreCaseOrEmailContainingIgnoreCase(search, search, search, search, search, paging);
        }
        return result;
    }

    @Override
    public Unit findByUuid(String uuid) {
        return unitRepository.findByUuid(uuid);
    }

    @Override
    public void save(Unit unit) {
        unitRepository.save(unit);
    }

    @Override
    public Unit deleteByUuid(String uuid) {
        Unit unit = unitRepository.findById(uuid).orElse(null);
        if (unit != null) {
            unitRepository.delete(unit);
            return unit;
        } else {
            return null;
        }
    }

    @Override
    public Unit findByEmail(String email) {
        return unitRepository.findByEmail(email);
    }

    @Override
    public Unit findByPhone(String phone) {
        return unitRepository.findByPhone(phone);
    }

    @Override
    public Unit findByName(String name) {
        return unitRepository.findByName(name);
    }

    @Override
    public Unit findByCode(String code) {
        return unitRepository.findByCode(code);
    }

    @Override
    public List<Unit> findByIds(List<String> ids) {
        return unitRepository.findByUuidIn(ids);
    }

    @Override
    public List<Unit> deleteByListId(List<String> ids) {
        List<Unit> unitList = unitRepository.findByUuidIn(ids);
        if (unitList.size() > 0) {
            unitRepository.deleteAll(unitList);
        }
        return unitList;
    }

    @Override
    public List<Unit> findUnitByStageIdList(List<String> stageIdList) {
        return unitCustomizeRepository.findUnitByStageIdList(stageIdList);
    }

    @Override
    public void deleteStageData(List<String> stageIds) {
        for (String stageId : stageIds) {
            List<Unit> listUnit = unitRepository.findByLisOfStageContainingIgnoreCase(stageId);
            for (Unit unit : listUnit) {
                String listOfStage = unit.getLisOfStage();
                listOfStage = listOfStage.replaceAll(stageId + ",", "").replaceAll(stageId, "");
                unit.setLisOfStage(listOfStage);
            }
            if (listUnit != null && !listUnit.isEmpty()) {
                unitRepository.saveAll(listUnit);
            }
        }
    }

    @Override
    public List<UnitDTO> transformWithPermission(List<Unit> unitList, String jobType) {
        List<UnitDTO> unitDTOList = new ArrayList<>();
        for (Unit unit : unitList) {
            UnitDTO unitDTO = modelMapper.map(unit, UnitDTO.class);
            if (StringUtil.isNullOrEmpty(jobType) || unit.getListOfJob() != null && unit.getListOfJob().contains(jobType)) {
                unitDTO.setProcessStatus(true);
                unitDTOList.add(0, unitDTO);
            } else {
                unitDTO.setProcessStatus(false);
                unitDTOList.add(unitDTO);
            }
        }
        return unitDTOList;
    }

}
