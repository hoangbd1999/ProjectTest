package com.elcom.metacen.group.detect.service.impl;

import com.elcom.metacen.group.detect.model.MappingPairInfo;
import com.elcom.metacen.group.detect.model.ObjectGroup;
import com.elcom.metacen.group.detect.model.ObjectGroupMapping;
import com.elcom.metacen.group.detect.model.dto.*;
import com.elcom.metacen.group.detect.repository.mongo.CustomObjectGroupMappingRepository;
import com.elcom.metacen.group.detect.repository.mongo.CustomObjectGroupRepository;
import com.elcom.metacen.group.detect.service.IDetectGroupAsyncService;
import com.uber.h3core.H3Core;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

import static com.elcom.metacen.group.detect.constant.DetectGroupConstant.*;
import static java.util.stream.Collectors.*;

@Service
public class DetectGroupAsyncServiceImpl implements IDetectGroupAsyncService {

    private static final Logger log = LoggerFactory.getLogger(DetectGroupAsyncServiceImpl.class);

    private final ModelMapper modelMapper;
    private final CustomObjectGroupRepository customObjectGroupRepository;
    private final CustomObjectGroupMappingRepository customObjectGroupMappingRepository;

    public DetectGroupAsyncServiceImpl(ModelMapper modelMapper, CustomObjectGroupRepository customObjectGroupRepository, CustomObjectGroupMappingRepository customObjectGroupMappingRepository) {
        this.modelMapper = modelMapper;
        this.customObjectGroupRepository = customObjectGroupRepository;
        this.customObjectGroupMappingRepository = customObjectGroupMappingRepository;
    }

    @Override
    @SneakyThrows
    @Async
    public void detectGroup(ObjectGroupConfigDTO objectGroupConfigDTO, List<VsatAisDTO> filterCoordinates, Integer hour, Integer level, LocalDateTime endTime) {

        // TH1: Thời gian để xác định xem có lấy các nhóm mới nhất theo từng tàu hay ko, nếu nhóm mới nhất có thời gian gặp nhau cuối quá xa thì loại bỏ.
        // TH2: Áp dụng để tách các tàu khỏi nhóm, nếu tàu có thời gian cuối cách quá xa thì loại khỏi nhóm, taạo nhóm mới.
        final Integer DELTA_HOUR_GET_GROUP_FROM_DATABASE = hour / 3;

        Map<BigInteger, List<VsatAisDTO>> mmsiMap = filterCoordinates.stream()
                .collect(groupingBy(VsatAisDTO::getMmsi));
        // group theo các đối tượng.
        Map<BigInteger, List<CellDTO>> mmsiCellDTOMap = mmsiMapGroupByMmsi(filterCoordinates);
        Map<Long, List<MmsiInCellDTO>> cellIdMap = getCellIdsWithListShipMmsi(filterCoordinates);

        // tìm kiếm các cặp đi chung với nhau
        H3Core h3 = H3Core.newInstance();
        Map<BigInteger, Map<BigInteger, PairGroupDTO>> pairGroupMap = detectPairGoTogether(mmsiCellDTOMap, cellIdMap, h3);

        //Lọc các cặp có count thỏa mãn
        Map<String, PairGroupDTO> pairGroupFiltered = filterPair(pairGroupMap, hour);

        // nhóm các cặp lại thành một nhóm
        List<ObjectGroupDTO> listDetailGroup = detectPotentialGroupFromListPairShip(mmsiMap, pairGroupFiltered, DELTA_HOUR_GET_GROUP_FROM_DATABASE);

        // Lấy các nhóm trong db
        List<ObjectGroupDTO> groupsInDatabase = getGroupByConfig(objectGroupConfigDTO.getUuid());

        //ObjectGroup và ObjectGroupMapping để insert và update
        List<ObjectGroup> insertGroups = new ArrayList<>();
        List<ObjectGroupMapping> insertGroupMappings = new ArrayList<>();
        List<ObjectGroup> updateGroups = new ArrayList<>();

        //Lấy các nhóm mới nhất theo các tàu (trong db)
        Map<String, Pair<LocalDateTime, ObjectGroupDTO>> shipWithLastSeparateTimeInDb =
                modifiedGroupsInDb(objectGroupConfigDTO, hour, level, groupsInDatabase, insertGroups,
                        insertGroupMappings, DELTA_HOUR_GET_GROUP_FROM_DATABASE, endTime);

        // So sánh các nhóm đã tính ra với các nhóm trong db
        calculateExactGroup(objectGroupConfigDTO, hour, level, listDetailGroup, shipWithLastSeparateTimeInDb,
                insertGroups, insertGroupMappings, updateGroups, DELTA_HOUR_GET_GROUP_FROM_DATABASE);

        if (insertGroups.size() != 0) customObjectGroupRepository.insert(insertGroups);
        if (updateGroups.size() != 0) customObjectGroupRepository.updateGroups(updateGroups);
        if (insertGroupMappings.size() != 0) customObjectGroupMappingRepository.insert(insertGroupMappings);

        log.info("Insert groups: {}, update groups: {}, insert mappings: {}", insertGroups.size(), updateGroups.size(), insertGroupMappings.size());
    }

    private void calculateExactGroup(ObjectGroupConfigDTO objectGroupConfigDTO,
                                     Integer hour,
                                     Integer level,
                                     List<ObjectGroupDTO> listGroupCalculated,
                                     Map<String, Pair<LocalDateTime, ObjectGroupDTO>> shipWithLastSeparateTimeInDb,
                                     List<ObjectGroup> insertGroups,
                                     List<ObjectGroupMapping> insertGroupMappings,
                                     List<ObjectGroup> updateGroups,
                                     Integer DELTA_HOUR_GET_GROUP_FROM_DATABASE) {
        for (ObjectGroupDTO objectGroupCalculated : listGroupCalculated) {
            ObjectGroupDTO groupInDb = null;

            //Tìm các nhóm mới nhất trong list các nhóm
            groupInDb = objectGroupCalculated.getShips().stream()
                    .map(ship -> shipWithLastSeparateTimeInDb.getOrDefault(ship.getObjId(), null))
                    .filter(Objects::nonNull)
                    .max(Comparator.comparing(Pair::getLeft))
                    .orElse(Pair.of(null, null))
                    .getRight();

            if (groupInDb == null) {
                //tao moi
                createNewGroup(objectGroupConfigDTO, hour, level, insertGroups, insertGroupMappings, objectGroupCalculated);
            } else {
                // TODO: kiểm tra nếu thời gian của các tàu lệch quá nhiều so với thời gian đi chung cuối cùng của cả nhóm

                boolean isSeparate = false;

                List<String> shipIdsInCalculatedGroup = objectGroupCalculated.getShips().stream()
                        .map(ObjectGroupMapping::getObjId)
                        .collect(toList());

                isSeparate = groupInDb.getShips().stream()
                        .filter(ship -> !shipIdsInCalculatedGroup.contains(ship.getObjId()))
                        .map(ship -> shipWithLastSeparateTimeInDb.getOrDefault(ship.getObjId(), null))
                        .filter(Objects::nonNull)
                        .map(Pair::getLeft)
                        .anyMatch(lastSeparateTime -> lastSeparateTime.isBefore(objectGroupCalculated.getLastTogetherTime().minusHours(DELTA_HOUR_GET_GROUP_FROM_DATABASE)));

//                for (Map.Entry<String, MappingPairInfo> mappingPair : groupInDb.getMappingPairInfos().entrySet()) {
//                    if (mappingPair.getValue().getSeparateTime().isBefore(objectGroupCalculated.getLastTogetherTime().minusHours(DELTA_HOUR_GET_GROUP_FROM_DATABASE))) {
//                        isSeparate = true;
//                        break;
//                    }
//                }

                if (isSeparate) {
                    // Tạo nhóm mới.
                    createNewGroup(objectGroupConfigDTO, hour, level, insertGroups, insertGroupMappings, objectGroupCalculated);
                } else {
                    // update
                    ObjectGroup updateGroup = modelMapper.map(groupInDb, ObjectGroup.class);
                    updateGroup.setUpdatedBy("system")
                            .setConfigDistanceLevel(level)
                            .setConfigTogetherTime(hour)
                            .setFirstTogetherTime(objectGroupCalculated.getFirstTogetherTime())
                            .setLastTogetherTime(objectGroupCalculated.getLastTogetherTime())
                            .setUpdatedAt(LocalDateTime.now());

                    // update mappingPairInfo
                    objectGroupCalculated.getMappingPairInfos().forEach((key, value) -> {
                        MappingPairInfo toModifiedMappingPair = updateGroup.getMappingPairInfos().getOrDefault(key, null);
                        if (toModifiedMappingPair == null) {
                            updateGroup.getMappingPairInfos().put(key, value);
                        } else {
                            //Thêm thời gian chưa có vào trong list
                            LocalDateTime lastTimeInDb = toModifiedMappingPair.getSeparateTime();
                            value.getEventTimes().stream().forEach(calculatedTime -> {
                                if (calculatedTime.isAfter(lastTimeInDb)) {
                                    toModifiedMappingPair.getEventTimes().add(calculatedTime);
                                }
                            });
                        }
                    });

                    List<String> mappingObjIds = groupInDb.getShips().stream().map(ObjectGroupMapping::getObjId).collect(toList());

                    List<ObjectGroupMapping> listShipAdd = objectGroupCalculated.getShips().stream()
                            .filter(ship -> !mappingObjIds.contains(ship.getObjId()))
                            .peek(mapping -> mapping.setGroupId(updateGroup.getUuid()))
                            .collect(toList());
                    updateGroups.add(updateGroup);
                    insertGroupMappings.addAll(listShipAdd);
                }
            }
        }
    }

    private void createNewGroup(ObjectGroupConfigDTO objectGroupConfigDTO, Integer hour, Integer level, List<ObjectGroup> insertGroups, List<ObjectGroupMapping> insertGroupMappings, ObjectGroupDTO objectGroupCalculated) {
        objectGroupCalculated.setUuid(UUID.randomUUID().toString())
                .setConfigName(objectGroupConfigDTO.getName())
                .setConfigUuid(objectGroupConfigDTO.getUuid())
                .setIsConfirmed(0)
                .setIsDeleted(0)
                .setConfigDistanceLevel(level)
                .setConfigTogetherTime(hour)
                .setCreatedBy("system");
        objectGroupCalculated.setCreatedDate(LocalDateTime.now());
        objectGroupCalculated.getShips().forEach(shipMapping -> shipMapping.setGroupId(objectGroupCalculated.getUuid()));
        insertGroups.add(modelMapper.map(objectGroupCalculated, ObjectGroup.class));
        insertGroupMappings.addAll(objectGroupCalculated.getShips());
    }

    private Map<String, Pair<LocalDateTime, ObjectGroupDTO>> modifiedGroupsInDb(ObjectGroupConfigDTO objectGroupConfigDTO,
                                                                                Integer hour,
                                                                                Integer level,
                                                                                List<ObjectGroupDTO> groupsInDatabase,
                                                                                List<ObjectGroup> insertGroups,
                                                                                List<ObjectGroupMapping> insertGroupMappings,
                                                                                Integer DELTA_HOUR_GET_GROUP_FROM_DATABASE,
                                                                                LocalDateTime endTime) {
        //dùng 1 trong 2: 1 cái là 4 giờ, 1 cái là 1 nửa của config time
        //final LocalDateTime MIN_TIME_ACCEPT_FILTERED_DB = endTime.minusHours(DELTA_HOUR_SEPARATE);
        final LocalDateTime MIN_TIME_ACCEPT_FILTERED_DB = endTime.minusHours(DELTA_HOUR_GET_GROUP_FROM_DATABASE);
        Map<String, Pair<LocalDateTime, ObjectGroupDTO>> groupsInDbFiltered = groupsInDatabase.stream()
                .flatMap(group -> group.getMappingPairInfos().entrySet().stream()
                        .flatMap(shipPair -> Stream.of(
                                Map.entry(shipPair.getKey().split(":")[0], Pair.of(shipPair.getValue().getSeparateTime(), group)),
                                Map.entry(shipPair.getKey().split(":")[1], Pair.of(shipPair.getValue().getSeparateTime(), group)))))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (separateTime1, separateTime2) -> {
                    if (separateTime1.getLeft().isAfter(separateTime2.getLeft())) return separateTime1;
                    else return separateTime2;
                }))
                .entrySet().stream()
                .filter(entry -> entry.getValue().getLeft().isAfter(MIN_TIME_ACCEPT_FILTERED_DB))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        return groupsInDbFiltered;
    }

    private static List<ObjectGroupDTO> detectPotentialGroupFromListPairShip(Map<BigInteger, List<VsatAisDTO>> mmsiMap,
                                                                             Map<String, PairGroupDTO> pairGroupFiltered,
                                                                             Integer DELTA_HOUR_GET_GROUP_FROM_DATABASE) {
        List<Pair<String, Integer>> keyPairs = pairGroupFiltered.keySet()
                .stream().map(key -> Pair.of(key, 0))
                .collect(toList());
        List<ObjectGroupDTO> listDetailGroup = new ArrayList<>();
        for (int i = 0; i < keyPairs.size(); i++) {
            if (keyPairs.get(i).getRight() != 0) continue;

            PairGroupDTO value = pairGroupFiltered.get(keyPairs.get(i).getLeft());
            keyPairs.set(i, Pair.of(keyPairs.get(i).getLeft(), 1));

            List<String> shipIdsInGroup = new ArrayList<>(List.of(keyPairs.get(i).getLeft().split(":")));

            VsatAisDTO firstObjectMappingInfo = mmsiMap.get(BigInteger.valueOf(Long.parseLong(keyPairs.get(i).getLeft().split(":")[0]))).get(0);
            VsatAisDTO secondObjectMappingInfo = mmsiMap.get(BigInteger.valueOf(Long.parseLong(keyPairs.get(i).getLeft().split(":")[1]))).get(0);

            ObjectGroupDTO objectGroupDTO = new ObjectGroupDTO();
            objectGroupDTO.setFirstTogetherTime(value.getEventTimes().get(0));
            objectGroupDTO.setLastTogetherTime(value.getEventTimes().get(value.getEventTimes().size() - 1));
            objectGroupDTO.addMappingPairInfo(
                    keyPairs.get(i).getLeft(),
                    new MappingPairInfo()
                            .setEventTimes(value.getEventTimes()));

            ObjectGroupMapping firstMappingShip = new ObjectGroupMapping()
                    .setCreatedTime(LocalDateTime.now())
                    .setObjName(firstObjectMappingInfo.getName())
                    .setObjTypeId(firstObjectMappingInfo.getTypeId())
                    .setIsDeleted(0)
                    .setTakedToSync(0)
                    .setObjId(keyPairs.get(i).getLeft().split(":")[0]);
            ObjectGroupMapping secondMappingShip = new ObjectGroupMapping()
                    .setCreatedTime(LocalDateTime.now())
                    .setObjName(secondObjectMappingInfo.getName())
                    .setObjTypeId(secondObjectMappingInfo.getTypeId())
                    .setIsDeleted(0)
                    .setTakedToSync(0)
                    .setObjId(keyPairs.get(i).getLeft().split(":")[1]);
            List<ObjectGroupMapping> shipsInGroup = new ArrayList<>(List.of(firstMappingShip, secondMappingShip));
            for (int j = 0; j < keyPairs.size(); j++) {
                if (keyPairs.get(j).getRight() != 0) continue;
                PairGroupDTO temp = pairGroupFiltered.get(keyPairs.get(j).getLeft());
                String firstShip = keyPairs.get(j).getLeft().split(":")[0];
                String secondShip = keyPairs.get(j).getLeft().split(":")[1];
                if (shipIdsInGroup.contains(firstShip) && !shipIdsInGroup.contains(secondShip)) {
                    //Lọc tàu nếu lệch thời gian gặp nhau cuối của tàu đó và nhóm quá thời gian cho phép
                    if (!temp.getEventTimes().get(temp.getEventTimes().size() - 1).isBefore(objectGroupDTO.getLastTogetherTime().minusHours(DELTA_HOUR_GET_GROUP_FROM_DATABASE))) {
                        //reset j để tính lại các nhóm từ đầu.
                        j = generateNewGroupMappingAndUpdateCalculatedGroup(mmsiMap, keyPairs, shipIdsInGroup, objectGroupDTO, shipsInGroup, j, temp, secondShip);
                    }
                } else if (!shipIdsInGroup.contains(firstShip) && shipIdsInGroup.contains(secondShip)) {
                    if (!temp.getEventTimes().get(temp.getEventTimes().size() - 1).isBefore(objectGroupDTO.getLastTogetherTime().minusHours(DELTA_HOUR_GET_GROUP_FROM_DATABASE))) {
                        j = generateNewGroupMappingAndUpdateCalculatedGroup(mmsiMap, keyPairs, shipIdsInGroup, objectGroupDTO, shipsInGroup, j, temp, firstShip);
                    }
                } else if (shipIdsInGroup.contains(firstShip) && shipIdsInGroup.contains(secondShip)) {
                    keyPairs.set(j, Pair.of(keyPairs.get(j).getLeft(), 1));
                    objectGroupDTO.addMappingPairInfo(keyPairs.get(j).getLeft(),
                            new MappingPairInfo()
                                    .setEventTimes(temp.getEventTimes()));
                    if (temp.getEventTimes().get(temp.getEventTimes().size() - 1).isAfter(objectGroupDTO.getLastTogetherTime())) {
                        objectGroupDTO.setLastTogetherTime(temp.getEventTimes().get(temp.getEventTimes().size() - 1));
                    }
                }
            }
            objectGroupDTO.setShips(shipsInGroup);
            listDetailGroup.add(objectGroupDTO);
        }
        return listDetailGroup;
    }

    private static int generateNewGroupMappingAndUpdateCalculatedGroup(Map<BigInteger, List<VsatAisDTO>> mmsiMap,
                                                                       List<Pair<String, Integer>> keyPairs,
                                                                       List<String> shipIdsInGroup, ObjectGroupDTO objectGroupDTO,
                                                                       List<ObjectGroupMapping> shipsInGroup,
                                                                       int j, PairGroupDTO temp, String secondShip) {
        shipIdsInGroup.add(secondShip);
        VsatAisDTO tempObjectMappingInfo = mmsiMap.get(BigInteger.valueOf(Long.parseLong(secondShip))).get(0);
        ObjectGroupMapping tempShipMapping = new ObjectGroupMapping()
                .setCreatedTime(LocalDateTime.now())
                .setObjName(tempObjectMappingInfo.getName())
                .setObjTypeId(tempObjectMappingInfo.getTypeId())
                .setIsDeleted(0)
                .setTakedToSync(0)
                .setObjId(secondShip);
        shipsInGroup.add(tempShipMapping);
        keyPairs.set(j, Pair.of(keyPairs.get(j).getLeft(), 1));
        if (objectGroupDTO.getFirstTogetherTime().isAfter(temp.getEventTimes().get(0))) {
            objectGroupDTO.setFirstTogetherTime(temp.getEventTimes().get(0));
        }
        objectGroupDTO.addMappingPairInfo(keyPairs.get(j).getLeft(),
                new MappingPairInfo()
                        .setEventTimes(temp.getEventTimes()));
        if (temp.getEventTimes().get(temp.getEventTimes().size() - 1).isAfter(objectGroupDTO.getLastTogetherTime())) {
            objectGroupDTO.setLastTogetherTime(temp.getEventTimes().get(temp.getEventTimes().size() - 1));
        }
        j = 0;
        return j;
    }

    private Map<String, PairGroupDTO> filterPair(Map<BigInteger, Map<BigInteger, PairGroupDTO>> pairGroupMap, Integer hour) {
        return pairGroupMap
                .entrySet()
                .stream()
                .flatMap(listShipOfFirstShip -> listShipOfFirstShip.getValue().entrySet().stream()
                        .map(secondShip -> Map.entry(generateKey(listShipOfFirstShip.getKey(), secondShip.getKey()), secondShip.getValue())))
                .filter(entry -> entry.getValue() != null)
                .filter(entry -> {
                    if (entry.getValue().getCount() == 0) return false;
                    LocalDateTime firstEventTime = entry.getValue().getEventTimes().get(0);
                    LocalDateTime lastEventTime = entry.getValue().getEventTimes().get(entry.getValue().getEventTimes().size() - 1);

                    long hourDiff = ChronoUnit.HOURS.between(firstEventTime, lastEventTime);
                    return entry.getValue().getCount() >= MIN_NUM_COUNT && hourDiff >= hour;
                })
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Map<BigInteger, Map<BigInteger, PairGroupDTO>> detectPairGoTogether(Map<BigInteger, List<CellDTO>> mmsiCellDTOMap, Map<Long, List<MmsiInCellDTO>> cellIdMap, H3Core h3) {
        Map<BigInteger, Map<BigInteger, PairGroupDTO>> pairGroupMap = new HashMap<>();
        mmsiCellDTOMap.forEach((mmsiFirstShip, listCellDTO) -> {
            Map<BigInteger, PairGroupDTO> shipOfFirstShip = new HashMap<>();
            listCellDTO.forEach(cellOfFirstShipOfCompare -> {
                LocalDateTime timeFirstShip = cellOfFirstShipOfCompare.getInTime();

                List<Long> neighborsAndMiddleH3 = h3.gridDisk(cellOfFirstShipOfCompare.getCellId(), 1);
                List<BigInteger> checkedShipsInNeighbors = new ArrayList<>();

                for (Long neighbor : neighborsAndMiddleH3) {
                    List<MmsiInCellDTO> shipsInCell = cellIdMap.getOrDefault(neighbor, null);
                    if (shipsInCell != null) {
                        for (MmsiInCellDTO secondShip : shipsInCell) {
                            if (secondShip.getMmsi().equals(mmsiFirstShip)) continue;
                            if (checkedShipsInNeighbors.contains(secondShip.getMmsi())) continue;
                            BigInteger mmsiSecondShip = secondShip.getMmsi();
                            List<LocalDateTime> timesSecondShip = secondShip.getEventTimes();
                            for (LocalDateTime timeToCheckOverlap : timesSecondShip) {
                                if ((timeToCheckOverlap.isAfter(timeFirstShip.minusMinutes(DELTA_TIME))
                                        && timeToCheckOverlap.isBefore(timeFirstShip.plusMinutes(DELTA_TIME)))
                                        || timeToCheckOverlap.isEqual(timeFirstShip.minusMinutes(DELTA_TIME))
                                        || timeToCheckOverlap.isEqual(timeFirstShip.plusMinutes(DELTA_TIME))) {
                                    PairGroupDTO pairInGroup = shipOfFirstShip.getOrDefault(mmsiSecondShip, null);
                                    if (pairInGroup == null) {
                                        PairGroupDTO pairOf2Ship = new PairGroupDTO();
                                        pairOf2Ship.addEventTime(timeFirstShip);
                                        pairOf2Ship.setCount(1);
                                        shipOfFirstShip.put(mmsiSecondShip, pairOf2Ship);
                                    } else {
                                        pairInGroup.setCount(pairInGroup.getCount() + 1);
                                        pairInGroup.addEventTime(timeFirstShip);
                                        pairInGroup.setCheckCount(0);
                                    }
                                    checkedShipsInNeighbors.add(mmsiSecondShip);
                                    break;
                                }
                            }
                        }
                    }
                }

                //Check nếu ko bị lệch (mất tín hiệu) quá số lần cho phép thì loại
                for (Iterator<Map.Entry<BigInteger, PairGroupDTO>> it = shipOfFirstShip.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<BigInteger, PairGroupDTO> entry = it.next();
                    if (!checkedShipsInNeighbors.contains(entry.getKey())) {
                        if (entry.getValue() != null && entry.getValue().getCount() >= 2) {
                            entry.getValue().setCheckCount(entry.getValue().getCheckCount() + 1);
                        }
                        if (entry.getValue() == null || entry.getValue().getCheckCount() >= 3 || entry.getValue().getCount() < 2) {
                            it.remove();
                        }
                    }
                }
            });

            pairGroupMap.put(mmsiFirstShip, shipOfFirstShip);
        });
        return pairGroupMap;
    }

    private static Map<Long, List<MmsiInCellDTO>> getCellIdsWithListShipMmsi(List<VsatAisDTO> filterCoordinates) {
        return filterCoordinates.stream()
                .collect(groupingBy(VsatAisDTO::getCellId))
                .entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), entry.getValue()
                        .stream().collect(groupingBy(VsatAisDTO::getMmsi, mapping(VsatAisDTO::getEventTime, toList())))
                        .entrySet().stream()
                        .map(mmsiEventTimePair -> {
                            MmsiInCellDTO mmsiInCellDTO = new MmsiInCellDTO().setMmsi(mmsiEventTimePair.getKey());
                            if (mmsiEventTimePair.getValue().size() <= 1) {
                                mmsiInCellDTO.addEventTime(mmsiEventTimePair.getValue().get(0));
                            } else {
                                mmsiInCellDTO.addEventTimes(List.of(mmsiEventTimePair.getValue().get(0), mmsiEventTimePair.getValue().get(mmsiEventTimePair.getValue().size() - 1)));
                            }
                            return mmsiInCellDTO;
                        })
                        .collect(toList())))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Map<BigInteger, List<CellDTO>> mmsiMapGroupByMmsi(List<VsatAisDTO> filterCoordinates) {
        return filterCoordinates.stream()
                .collect(groupingBy(VsatAisDTO::getMmsi))
                .entrySet().stream()
                .map(entry -> {
                    List<CellDTO> temp = new ArrayList<>();
                    temp.add(new CellDTO()
                            .setCellId(entry.getValue().get(0).getCellId())
                            .setInTime(entry.getValue().get(0).getEventTime()));
                    for (int i = 0; i < entry.getValue().size(); i++) {
                        if (!entry.getValue().get(i).getCellId().equals(temp.get(temp.size() - 1).getCellId())) {
                            temp.add(new CellDTO()
                                    .setCellId(entry.getValue().get(i).getCellId())
                                    .setInTime(entry.getValue().get(i).getEventTime()));
                        }
                    }
                    return Map.entry(entry.getKey(), temp);
                })
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private List<ObjectGroupDTO> getGroupByConfig(String id) {
        List<ObjectGroup> groups = customObjectGroupRepository.findGroupOverPeriod(id);
        Map<String, List<ObjectGroupMapping>> groupShips = customObjectGroupMappingRepository.getObjectGroupMappingByObjectGroupUuid(groups.stream().map(ObjectGroup::getUuid).collect(toList()))
                .stream()
                .collect(groupingBy(ObjectGroupMapping::getGroupId));

        return groups.stream()
                .map(group -> {
                    ObjectGroupDTO map = modelMapper.map(group, ObjectGroupDTO.class);
                    map.setShips(groupShips.getOrDefault(group.getUuid(), null));
                    return map;
                })
                .collect(toList());
    }

    private String generateKey(BigInteger mmsiFirstShip, BigInteger mmsi) {
        return mmsiFirstShip + ":" + mmsi;
    }
}