package com.elcom.metacen.enrich.data.business;

import com.elcom.metacen.dto.redis.Countries;
import com.elcom.metacen.dto.redis.VsatVesselType;
import com.elcom.metacen.enrich.data.model.dto.*;
import com.elcom.metacen.enrich.data.service.*;
import com.elcom.metacen.enrich.data.validation.SatelliteImageChangeValidation;
import com.elcom.metacen.enrich.data.validation.VsatMediaDataObjectAnalyzedValidation;
import com.elcom.metacen.enums.DataDeleteStatus;
import com.elcom.metacen.enums.DataNoteStatus;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.ResponseMessage;
import com.elcom.metacen.enrich.data.config.ApplicationConfig;
import com.elcom.metacen.enrich.data.constant.Constant;
import com.elcom.metacen.enrich.data.model.SatelliteImageChanges;
import com.elcom.metacen.enrich.data.model.SatelliteImageData;
import com.elcom.metacen.enrich.data.model.SatelliteImageDataAnalyzed;
import com.elcom.metacen.enrich.data.model.SatelliteImageObjectAnalyzed;
import com.elcom.metacen.enrich.data.model.VsatMediaDataObjectAnalyzed;
import com.elcom.metacen.enrich.data.utils.DateUtil;
import com.elcom.metacen.enrich.data.validation.SatelliteImageDataValidation;
import com.elcom.metacen.enrich.data.validation.VsatMediaAnalyzedValidation;
import com.elcom.metacen.utils.JSONConverter;
import com.elcom.metacen.utils.StringUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.math.BigInteger;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Controller
public class EnrichDataBusiness extends BaseBusiness {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnrichDataBusiness.class);

    @Autowired
    private SatelliteImageDataAnalyzedService satelliteImageDataAnalyzedService;

    @Autowired
    private SatelliteImageObjectAnalyzedService satelliteImageObjectAnalyzedService;

    @Autowired
    private VsatMediaAnalyzedService vsatMediaAnalyzedService;

    @Autowired
    private SatelliteImageDataService satelliteImageDataService;

    @Autowired
    private SatelliteImageChangeService satelliteImageChangeService;

    @Autowired
    private SatelliteImageChangeResultService satelliteImageChangeResultService;

    @Autowired
    private VsatMediaDataObjectAnalyzedService vsatMediaDataObjectAnalyzedService;

    @Autowired
    private VsatAisDataService vsatAisDataService;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${satellite_data.max.records}")
    private Integer maxLimitSatelliteData;

    @Value("${vsat_ais_data.max.records}")
    private Integer maxLimitVsatAisData;

    @Value("${kafka.topic.request.compare.satellite.image}")
    private String topicRequestCompareSatelliteImage;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    // Satellite Image
    public ResponseMessage filterSatelliteImageEnrichData(Map<String, Object> bodyParam, Map<String, String> headerParam, String requestPath) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }

        //  Check ABAC
        ABACResponseDTO abacStatus = authorizeABAC("LIST", dto.getUuid(), requestPath);
        if (abacStatus == null || !abacStatus.getStatus()) {
            return new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }

        if (bodyParam == null || bodyParam.isEmpty()) {
            return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
        }

        try {
            SatelliteImageDataAnalyzedFilterDTO satelliteImageDataAnalyzedFilterDTO = buildSatelliteImageDataFilterRequest(bodyParam);
            String validationMsg = new SatelliteImageDataValidation().validateSatelliteImageDataFilter(satelliteImageDataAnalyzedFilterDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.OK.value(), validationMsg, new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }

            Page<SatelliteImageDataAnalyzedDTO> pagedResult = satelliteImageDataAnalyzedService.filterSatelliteImageData(satelliteImageDataAnalyzedFilterDTO);
            if (pagedResult == null) {
                return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, new MessageContent(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, null));
            }
            List<SatelliteImageDataAnalyzedDTO> listData = pagedResult.stream().map(data
                    -> modelMapper.map(data, SatelliteImageDataAnalyzedDTO.class)).collect(Collectors.toList());
            for (int i = 0; i < listData.size(); i++) {
                String satelliteImageUuidKey = listData.get(i).getUuidKey();
                String tileNumberReplace = listData.get(i).getTileNumber();
                listData.get(i).setTileNumber(tileNumberReplace.substring(1,tileNumberReplace.length()));
                List<SatelliteImageObjectAnalyzed> satelliteImageObjectAnalyzeds = satelliteImageObjectAnalyzedService.satelliteImageUuidKey(satelliteImageUuidKey);
                listData.get(i).setTotalObjects(satelliteImageObjectAnalyzeds.size());
            }
            return new ResponseMessage(new MessageContent(listData, pagedResult.getTotalElements()));

        } catch (Exception e) {
            LOGGER.error("ex: ", e);
            return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), new MessageContent(HttpStatus.OK.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
        }
    }

    public ResponseMessage getDetailSatellite(String requestPath, Map<String, String> headerParam, String pathParam) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }

        // Check ABAC
        ABACResponseDTO abacStatus = authorizeABAC("DETAIL", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            String uuid = pathParam;
            SatelliteImageDataAnalyzed satelliteImageDataAnalyzed = satelliteImageDataAnalyzedService.findByUuid(uuid);
            if (satelliteImageDataAnalyzed == null) {
                return new ResponseMessage(HttpStatus.OK.value(), "Dữ liệu không tồn tại",
                        new MessageContent(HttpStatus.OK.value(), "Dữ liệu không tồn tại", null));
            }
            SatelliteDetailDTO satelliteDetailDTO = modelMapper.map(satelliteImageDataAnalyzed, SatelliteDetailDTO.class);

            List<SatelliteImageObjectAnalyzed> satelliteImageObjectAnalyzeds = satelliteImageObjectAnalyzedService.satelliteImageUuidKey(uuid);
            List<SatelliteImageObjectAnalyzedDTO> listData = satelliteImageObjectAnalyzeds.stream().map(data
                    -> modelMapper.map(data, SatelliteImageObjectAnalyzedDTO.class)).collect(Collectors.toList());
            for (int i = 0; i < listData.size(); i++) {
                String filePathLocal = listData.get(i).getImageFilePath();
                listData.get(i).setImageFilePath(filePathLocal.replace(ApplicationConfig.SATELLITE_ROOT_FOLDER_INTERNAL, ApplicationConfig.SATELLITE_MEDIA_LINK_ROOT_API));
            }
            satelliteDetailDTO.setListObject(listData);
            String imageFilePathLocal = satelliteDetailDTO.getRootDataFolderPath() + "/infor.jpg";
            satelliteDetailDTO.setImageFilePathLocal(imageFilePathLocal);
            satelliteDetailDTO.setImageFilePath(imageFilePathLocal.replace(ApplicationConfig.SATELLITE_ROOT_FOLDER_INTERNAL, ApplicationConfig.SATELLITE_MEDIA_LINK_ROOT_API));
            return new ResponseMessage(new MessageContent(satelliteDetailDTO));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage filterSatelliteImageEnrichDataForMap(Map<String, Object> bodyParam, Map<String, String> headerParam, String requestPath) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }

        //  Check ABAC
        ABACResponseDTO abacStatus = authorizeABAC("LIST", dto.getUuid(), requestPath);
        if (abacStatus == null || !abacStatus.getStatus()) {
            return new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }

        if (bodyParam == null || bodyParam.isEmpty()) {
            return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
        }

        try {
            SatelliteImageDataAnalyzedFilterDTO satelliteImageDataAnalyzedFilterDTO = buildSatelliteImageDataFilterRequest(bodyParam);
            String validationMsg = new SatelliteImageDataValidation().validateSatelliteImageDataFilter(satelliteImageDataAnalyzedFilterDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.OK.value(), validationMsg, new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }

            List<SatelliteImageDataAnalyzedDTO> results = satelliteImageDataAnalyzedService.filterSatelliteImageDataForMap(satelliteImageDataAnalyzedFilterDTO);
            List<SatelliteDetailDTO> listDataSatelliteDetails = results.stream().map(data
                    -> modelMapper.map(data, SatelliteDetailDTO.class)).collect(Collectors.toList());

            for (int i = 0; i < listDataSatelliteDetails.size(); i++) {
                String uuid = listDataSatelliteDetails.get(i).getUuidKey();
                List<SatelliteImageObjectAnalyzed> satelliteImageObjectAnalyzeds = satelliteImageObjectAnalyzedService.satelliteImageUuidKey(uuid);
                List<SatelliteImageObjectAnalyzedDTO> listData = satelliteImageObjectAnalyzeds.stream().map(data
                        -> modelMapper.map(data, SatelliteImageObjectAnalyzedDTO.class)).collect(Collectors.toList());

                for (int j = 0; j < listData.size(); j++) {
                    String filePathLocal = listData.get(j).getImageFilePath();
                    listData.get(j).setImageFilePath(filePathLocal.replace(ApplicationConfig.SATELLITE_ROOT_FOLDER_INTERNAL, ApplicationConfig.SATELLITE_MEDIA_LINK_ROOT_API));
                }
                listDataSatelliteDetails.get(i).setListObject(listData);
            }
            if (listDataSatelliteDetails == null) {
                return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, new MessageContent(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, null));
            }
            return new ResponseMessage(new MessageContent(listDataSatelliteDetails));

        } catch (Exception e) {
            LOGGER.error("ex: ", e);
            return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                    new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
        }
    }

    public ResponseMessage getDetailSatelliteAnalyzed(String requestPath, Map<String, String> headerParam, String pathParam) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }

        // Check ABAC
        ABACResponseDTO abacStatus = authorizeABAC("DETAIL", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            String uuid = pathParam;
            SatelliteImageObjectAnalyzed satelliteImageObjectAnalyzed = satelliteImageObjectAnalyzedService.findByUuid(uuid);
            if (satelliteImageObjectAnalyzed == null) {
                return new ResponseMessage(HttpStatus.OK.value(), "Dữ liệu không tồn tại",
                        new MessageContent(HttpStatus.OK.value(), "Dữ liệu không tồn tại", null));
            }
            SatelliteImageObjectAnalyzedDTO satelliteImageObjectAnalyzedDTO = modelMapper.map(satelliteImageObjectAnalyzed, SatelliteImageObjectAnalyzedDTO.class);

            String imageFilePathLocal = satelliteImageObjectAnalyzed.getImageFilePath();
            satelliteImageObjectAnalyzedDTO.setImageFilePath(imageFilePathLocal.replace(ApplicationConfig.SATELLITE_ROOT_FOLDER_INTERNAL, ApplicationConfig.SATELLITE_MEDIA_LINK_ROOT_API));

            return new ResponseMessage(new MessageContent(satelliteImageObjectAnalyzedDTO));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage noteChange(Map<String, Object> bodyParam) {
        try {
            String uuid = (String) bodyParam.getOrDefault("uuid", "");
            Integer isNoted = bodyParam.get("isNoted") != null ? (Integer) bodyParam.get("isNoted") : 0;
            SatelliteImageDataAnalyzed satelliteImageDataAnalyzed = satelliteImageDataAnalyzedService.findByUuid(uuid);
            if (satelliteImageDataAnalyzed == null) {
                return new ResponseMessage(HttpStatus.OK.value(), "Dữ liệu không tồn tại",
                        new MessageContent(HttpStatus.OK.value(), "Dữ liệu không tồn tại", null));
            }
            this.satelliteImageDataAnalyzedService.noteChange(isNoted, uuid);
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "success", null));
        } catch (Exception e) {
            LOGGER.error("Fail", e);
            return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                    new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
        }
    }

    public ResponseMessage deleteSatelliteAnalyzed(String requestPath, Map<String, String> headerParam, String pathParam) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        ABACResponseDTO abacStatus = authorizeABAC("DELETE", dto.getUuid(), requestPath);

        if (abacStatus != null && abacStatus.getStatus()) {
            SatelliteImageObjectAnalyzed satelliteImageObjectAnalyzed = satelliteImageObjectAnalyzedService.findByUuid(pathParam);
            if (satelliteImageObjectAnalyzed == null) {
                return new ResponseMessage(HttpStatus.OK.value(), "Dữ liệu không tồn tại",
                        new MessageContent(HttpStatus.OK.value(), "Dữ liệu không tồn tại", null));
            }
            this.satelliteImageObjectAnalyzedService.delete(DataDeleteStatus.DELETED.code(), pathParam);
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Xóa dữ liệu thành công", null));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    private SatelliteImageDataAnalyzedFilterDTO buildSatelliteImageDataFilterRequest(Map<String, Object> bodyParam) {
        try {
            Integer page = bodyParam.get("page") != null ? (Integer) bodyParam.get("page") : 0;
            Integer size = StringUtil.objectToInteger(bodyParam.get("size")) > maxLimitSatelliteData ? maxLimitSatelliteData : StringUtil.objectToInteger(bodyParam.get("size"));
            String sort = (String) bodyParam.getOrDefault("sort", "");
            String term = (String) bodyParam.getOrDefault("term", null);
            String fromTime = (String) bodyParam.getOrDefault("fromTime", "");
            String toTime = (String) bodyParam.getOrDefault("toTime", "");
            List<String> dataVendorLst = bodyParam.get("dataVendorLst") != null ? (List<String>) bodyParam.get("dataVendorLst") : null;
            List<String> tileNumberLst = bodyParam.get("tileNumberLst") != null ? (List<String>) bodyParam.get("tileNumberLst") : null;
            List<String> tileNumberResult = new ArrayList<>();
            if(tileNumberLst!=null && !tileNumberLst.isEmpty()) {
                for (String tileNumberList : tileNumberLst) {
                    tileNumberResult.add("T" + tileNumberList);
                }
            }
            List<Integer> processStatusLst = bodyParam.get("processStatusLst") != null ? (List<Integer>) bodyParam.get("processStatusLst") : null;
            List<Integer> commentLst = bodyParam.get("commentLst") != null ? (List<Integer>) bodyParam.get("commentLst") : null;
            SatelliteImageDataAnalyzedFilterDTO satelliteImageDataAnalyzedFilterDTO = SatelliteImageDataAnalyzedFilterDTO.builder()
                    .page(page)
                    .size(size)
                    .sort(sort)
                    .term(term)
                    .fromTime(fromTime)
                    .toTime(toTime)
                    .dataVendorLst(dataVendorLst)
                    .tileNumberLst(tileNumberResult)
                    .processStatusLst(processStatusLst)
                    .commentLst(commentLst)
                    .build();

            return satelliteImageDataAnalyzedFilterDTO;
        } catch (Exception e) {
            LOGGER.error("ex: ", e);
        }

        return null;
    }

    // VSAT Media
    public ResponseMessage filterVsatMediaAnalyzed(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
        LOGGER.info("Filter vsat media analyzed with request >>> {}", bodyParam);

        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }

        // Check ABAC
        ABACResponseDTO abacStatus = authorizeABAC("LIST", dto.getUuid(), requestPath);
        if (abacStatus == null || !abacStatus.getStatus()) {
            return new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }

        if (bodyParam == null || bodyParam.isEmpty()) {
            return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
        }

        VsatMediaAnalyzedFilterDTO vsatMediaAnalyzedFilterDTO = buildVsatMediaAnalyzedFilterRequest(bodyParam);
        String validationMsg = new VsatMediaAnalyzedValidation().validateFilterVsatMediaAnalyzed(vsatMediaAnalyzedFilterDTO);
        if (validationMsg != null) {
            return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg, new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
        }

        Page<VsatMediaAnalyzedDTO> pagedResult = vsatMediaAnalyzedService.filterVsatMediaAnalyzed(vsatMediaAnalyzedFilterDTO);
        if (pagedResult == null) {
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, null));
        }

        return new ResponseMessage(new MessageContent(pagedResult.getContent(), pagedResult.getTotalElements()));
    }

    public ResponseMessage getDetailVsatMediaAnalyzed(String requestPath, Map<String, String> headerParam, String pathParam) {
        LOGGER.info("Get detail vsat media analyzed with uuid {}", pathParam);

        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }

        // Check ABAC
        ABACResponseDTO abacStatus = authorizeABAC("DETAIL", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            String uuid = pathParam;
            VsatMediaAnalyzedDTO vsatMediaAnalyzedDTO = vsatMediaAnalyzedService.getDetailVsatMediaAnalyzed(uuid);
            if (vsatMediaAnalyzedDTO == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại", null));
            }

            return new ResponseMessage(new MessageContent(vsatMediaAnalyzedDTO));
        } else {
            return new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(),
                    "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage findDetailVessel(Map<String, String> headerParam, String pathParam, String requestPath) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        ABACResponseDTO abacStatus = authorizeABAC("DETAIL", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            String mmsi = pathParam;
            if (!StringUtil.isNumeric(mmsi)) {
                return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            } else {
                MessageContent messageContent = vsatAisDataService.findDetailVessel(new Long(mmsi));
                if (messageContent == null) {
                    return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại",
                            new MessageContent(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại", null));
                }
                VsatAisResponseDTO aisLst = (VsatAisResponseDTO) messageContent.getData();
                if (aisLst == null) {
                    return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, new MessageContent(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, null));
                }
                try {
                    // get name of vessel type from redis
                    String key = Constant.REDIS_VESSEL_LST_KEY;

                    if (this.redisTemplate.hasKey(key)) {
                        List<VsatVesselType> vsatVesselTypesFromCaches = (List<VsatVesselType>) this.redisTemplate.opsForList().range(key, 0, Constant.REDIS_VESSEL_LST_FETCH_MAX);
                        if ( vsatVesselTypesFromCaches != null && !vsatVesselTypesFromCaches.isEmpty() ) {
                                for (VsatVesselType vsatVesselType : vsatVesselTypesFromCaches) {
                                    if (vsatVesselType.getTypeCode().equals(aisLst.getTypeId() + "")) {
                                        aisLst.setTypeName(vsatVesselType.getTypeName());
                                        break;
                                    }
                                }
                        }
                    }

                } catch (Exception ex) {
                    LOGGER.error("ex: ", ex);
                }

                try {
                    // get name of country from redis
                    String key = Constant.REDIS_COUNTRIES_LST_KEY;

                    if (this.redisTemplate.hasKey(key)) {
                        List<Countries> countriesFromCaches = (List<Countries>) this.redisTemplate.opsForList().range(key, 0, Constant.REDIS_COUNTRIES_LST_FETCH_MAX);
                        if ( countriesFromCaches != null && !countriesFromCaches.isEmpty() ) {
                                for (Countries countries : countriesFromCaches) {
                                    if (countries.getCountryId().equals(aisLst.getCountryId())) {
                                        aisLst.setCountryName(countries.getName());
                                        break;
                                    }
                                }
                        }
                    }

                } catch (Exception ex) {
                    LOGGER.error("ex: ", ex);
                }
                return new ResponseMessage(messageContent);
            }

        } else {
            return new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(),
                    "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    private VsatMediaAnalyzedFilterDTO buildVsatMediaAnalyzedFilterRequest(Map<String, Object> bodyParam) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Integer page = bodyParam.get("page") != null ? (Integer) bodyParam.get("page") : 0;
            Integer size = bodyParam.get("size") != null ? (Integer) bodyParam.get("size") : 20;
            String sort = (String) bodyParam.getOrDefault("sort", "");
            String term = (String) bodyParam.getOrDefault("term", "");
            String fromTime = (String) bodyParam.get("fromTime");
            String toTime = (String) bodyParam.get("toTime");
            List<String> dataVendorLst = bodyParam.get("dataVendorLst") != null ? (List<String>) bodyParam.get("dataVendorLst") : null;
            List<Integer> dataSourceIds = bodyParam.get("dataSourceIds") != null ? (List<Integer>) bodyParam.get("dataSourceIds") : null;
            List<Integer> mediaTypeIds = bodyParam.get("mediaTypeIds") != null ? (List<Integer>) bodyParam.get("mediaTypeIds") : null;
            List<Integer> processStatusLst = bodyParam.get("processStatusLst") != null ? (List<Integer>) bodyParam.get("processStatusLst") : null;

            List<AdvanceFilterDTO> filterLst = null;
            if (bodyParam.get("filter") != null) {
                filterLst = mapper.convertValue(bodyParam.get("filter"), new TypeReference<List<AdvanceFilterDTO>>() {
                });
            }

            // filter theo cột
            String uuid = (String) bodyParam.getOrDefault("uuid", "");
            String dataVendor = (String) bodyParam.getOrDefault("dataVendor", "");
            String dataSourceName = (String) bodyParam.getOrDefault("dataSourceName", "");
            String sourceIp = (String) bodyParam.getOrDefault("sourceIp", "");
            Long sourcePort = (bodyParam.get("sourcePort") != "" && bodyParam.get("sourcePort") != null) ? ((Number) bodyParam.get("sourcePort")).longValue() : null;
            Long sourceId = (bodyParam.get("sourceId") != "" && bodyParam.get("sourceId") != null) ? ((Number) bodyParam.get("sourceId")).longValue() : null;
            String sourceName = (String) bodyParam.getOrDefault("sourceName", "");
            String destIp = (String) bodyParam.getOrDefault("destIp", "");
            Long destPort = (bodyParam.get("destPort") != "" && bodyParam.get("destPort") != null) ? ((Number) bodyParam.get("destPort")).longValue() : null;
            Long destId = (bodyParam.get("destId") != "" && bodyParam.get("destId") != null) ? ((Number) bodyParam.get("destId")).longValue() : null;
            String destName = (String) bodyParam.getOrDefault("destName", "");
            String mediaTypeName = (String) bodyParam.getOrDefault("mediaTypeName", "");
            String fileType = (String) bodyParam.getOrDefault("fileType", "");
            String eventTime = (String) bodyParam.getOrDefault("eventTime", "");
            String processTime = (String) bodyParam.getOrDefault("processTime", "");

            VsatMediaAnalyzedFilterDTO vsatMediaAnalyzedFilterDTO = VsatMediaAnalyzedFilterDTO.builder()
                    .page(page)
                    .size(size)
                    .sort(sort)
                    .term(term)
                    .fromTime(fromTime)
                    .toTime(toTime)
                    .dataVendorLst(dataVendorLst)
                    .dataSourceIds(dataSourceIds)
                    .mediaTypeIds(mediaTypeIds)
                    .processStatusLst(processStatusLst)
                    .filter(filterLst)
                    .uuid(uuid)
                    .dataVendor(dataVendor)
                    .dataSourceName(dataSourceName)
                    .sourceIp(sourceIp)
                    .sourcePort(sourcePort)
                    .sourceId(sourceId)
                    .sourceName(sourceName)
                    .destIp(destIp)
                    .destPort(destPort)
                    .destId(destId)
                    .destName(destName)
                    .mediaTypeName(mediaTypeName)
                    .fileType(fileType)
                    .eventTime(eventTime)
                    .processTime(processTime)
                    .build();
            return vsatMediaAnalyzedFilterDTO;
        } catch (Exception e) {
            LOGGER.error("ex: ", e);
        }
        return null;
    }

    // Satellite Image CaptureTime - tileNumber
    public ResponseMessage getListSatelliteCaptureTime(String requestPath, Map<String, String> headerParam, String urlParam) {

        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }

        ABACResponseDTO abacStatus = authorizeABAC("LIST", dto.getUuid(), requestPath);
        if (abacStatus == null || !abacStatus.getStatus()) {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }

        Map<String, String> params = StringUtil.getUrlParamValues(urlParam);
        String tileNumber = "T" + params.get("tileNumber");
        List<SatelliteImageData> satelliteImageData = satelliteImageDataService.findByTileNumber(tileNumber);
        if (satelliteImageData.isEmpty()) {
            return new ResponseMessage(HttpStatus.OK.value(), "Dữ liệu không tồn tại", new MessageContent(HttpStatus.OK.value(), "Dữ liệu không tồn tại", null));
        }

        List<SatelliteCaptureTimeDTO> listData = satelliteImageData.stream().map(data -> modelMapper.map(data, SatelliteCaptureTimeDTO.class)).collect(Collectors.toList());
        for (int i = 0; i < satelliteImageData.size(); i++) {

            String filePathLocal = satelliteImageData.get(i).getRootDataFolderPath() + "/infor.jpg";

            if (!new File(filePathLocal).exists()) {
                continue;
            }

            listData.get(i).setImageFilePath(filePathLocal.replace(ApplicationConfig.SATELLITE_ROOT_FOLDER_INTERNAL, ApplicationConfig.SATELLITE_MEDIA_LINK_ROOT_API));
        }

        return new ResponseMessage(new MessageContent(listData));
    }

    // Satellite Image Compare
    public ResponseMessage insertSatelliteComparison(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {

        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }

        ABACResponseDTO abacStatus = authorizeABAC("POST", dto.getUuid(), requestPath);
        if (abacStatus == null || !abacStatus.getStatus()) {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }

        if (bodyParam == null || bodyParam.isEmpty()) {
            return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
        }

        SatelliteImageChangeRequestDTO satelliteImageChangeRequestDTO = buildSatelliteComparisonDTO(bodyParam, dto.getUserName());
        if (satelliteImageChangeRequestDTO == null) {
            return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
        }

        String validationMsg = new SatelliteImageChangeValidation().ValidationSatelliteImageChange(satelliteImageChangeRequestDTO);
        if (validationMsg != null) {
            return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg, new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
        }

        if (!new File(satelliteImageChangeRequestDTO.getImagePathFileOrigin() + File.separator + "bgr.jpg").exists()
                || !new File(satelliteImageChangeRequestDTO.getImagePathFileCompare() + File.separator + "bgr.jpg").exists()) {
            return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg, new MessageContent(HttpStatus.BAD_REQUEST.value(),
                    "Thư mục không có file `bgr.jpg` để so sánh!", null));
        }

        this.satelliteImageChangeService.save(satelliteImageChangeRequestDTO);

        //TODO: bắn kafka gửi request yêu cầu xử lý so sánh sự thay đổi:
        File folder = new File(ApplicationConfig.SATELLITE_ROOT_FOLDER_INTERNAL
                + File.separator + "shape-changed" + File.separator
                + DateUtil.dateToFolderName(new Date(), "yyyyMMdd") + File.separator + satelliteImageChangeRequestDTO.getUuidKey());
        folder.mkdir();
        folder.setReadable(true, false);
        folder.setExecutable(true, false);
        folder.setWritable(true, false);

        SatelliteImageRequestToCompareEngineDTO satelliteImageRequestToCompareEngine
                = new SatelliteImageRequestToCompareEngineDTO(
                        satelliteImageChangeRequestDTO.getUuidKey(), satelliteImageChangeRequestDTO.getImagePathFileOrigin(), satelliteImageChangeRequestDTO.getImagePathFileCompare(), folder.getPath(), 0);

        String msgAsJson = JSONConverter.toJSON(satelliteImageRequestToCompareEngine);

        ListenableFuture<SendResult<String, String>> sendFuture = this.kafkaTemplate.send(
                this.topicRequestCompareSatelliteImage, satelliteImageRequestToCompareEngine.getUuidKey(), msgAsJson
        );
        sendFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onSuccess(SendResult<String, String> result) {
                LOGGER.info("Sent OK -> [" + topicRequestCompareSatelliteImage + "], offset [" + result.getRecordMetadata().offset() + "], key [" + result.getProducerRecord().key() + "] partition [" + result.getRecordMetadata().partition() + "]");
            }

            @Override
            public void onFailure(Throwable ex) {
                LOGGER.error("Unable to send message = [" + msgAsJson + "], due to : " + ex.getMessage());
            }
        });

        return new ResponseMessage(new MessageContent(satelliteImageChangeRequestDTO));
    }

    public ResponseMessage filterSatelliteComparison(Map<String, Object> bodyParam, Map<String, String> headerParam, String requestPath) {

        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }

        ABACResponseDTO abacStatus = authorizeABAC("LIST", dto.getUuid(), requestPath);
        if (abacStatus == null || !abacStatus.getStatus()) {
            return new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }

        if (bodyParam == null || bodyParam.isEmpty()) {
            return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
        }

        try {
            SatelliteImageChangeFilterDTO satelliteImageChangeFilterDTO = buildSatelliteImageChangeFilterRequest(bodyParam);
            /*String validationMsg = new SatelliteImageChangeValidation().validateSatelliteImageChangeFilter(satelliteImageChangeFilterDTO);
            if ( validationMsg != null )
                return new ResponseMessage(HttpStatus.OK.value(), validationMsg, new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));*/

            Page<SatelliteImageChangeResponseDTO> pagedResult = satelliteImageChangeService.filterSatelliteImageChange(satelliteImageChangeFilterDTO);
            if (pagedResult == null || pagedResult.getContent() == null || pagedResult.getContent().isEmpty()) {
                return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, new MessageContent(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, null));
            }

            for (SatelliteImageChangeResponseDTO item : pagedResult.getContent()) {
                if (!item.getImagePathFileOrigin().startsWith("http")) {
                    item.setImagePathFileOrigin(item.getImagePathFileOrigin()
                            .replace(ApplicationConfig.SATELLITE_ROOT_FOLDER_INTERNAL, ApplicationConfig.SATELLITE_MEDIA_LINK_ROOT_API) + File.separator + "infor.jpg");
                }
                if (!item.getImagePathFileCompare().startsWith("http")) {
                    item.setImagePathFileCompare(item.getImagePathFileCompare()
                            .replace(ApplicationConfig.SATELLITE_ROOT_FOLDER_INTERNAL, ApplicationConfig.SATELLITE_MEDIA_LINK_ROOT_API) + File.separator + "infor.jpg");
                }
            }

            return new ResponseMessage(new MessageContent(pagedResult.getContent(), pagedResult.getTotalElements()));

        } catch (Exception e) {
            LOGGER.error("ex: ", e);
        }
        return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), new MessageContent(HttpStatus.OK.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
    }

    public ResponseMessage deleteSatelliteComparison(String requestPath, Map<String, String> headerParam, String pathParam) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        ABACResponseDTO abacStatus = authorizeABAC("DELETE", dto.getUuid(), requestPath);

        if (abacStatus != null && abacStatus.getStatus()) {
            SatelliteImageChanges satelliteImageChanges = satelliteImageChangeService.findByUuid(pathParam);
            if (satelliteImageChanges == null) {
                return new ResponseMessage(HttpStatus.OK.value(), "Dữ liệu không tồn tại",
                        new MessageContent(HttpStatus.OK.value(), "Dữ liệu không tồn tại", null));
            }
            this.satelliteImageChangeService.delete(DataDeleteStatus.DELETED.code(), pathParam);
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Xóa dữ liệu thành công", null));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage getDetailSatelliteComparison(String requestPath, Map<String, String> headerParam, String pathParam) {

        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }

        ABACResponseDTO abacStatus = authorizeABAC("DETAIL", dto.getUuid(), requestPath);
        if (abacStatus == null || !abacStatus.getStatus()) {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }

        String uuid = pathParam;
        SatelliteImageChanges satelliteImageChanges = satelliteImageChangeService.findByUuid(uuid);
        if (satelliteImageChanges == null) {
            return new ResponseMessage(HttpStatus.OK.value(), "Dữ liệu không tồn tại", new MessageContent(HttpStatus.OK.value(), "Dữ liệu không tồn tại", null));
        }

        SatelliteImageChangeDetailDTO satelliteImageChangeDetail = modelMapper.map(satelliteImageChanges, SatelliteImageChangeDetailDTO.class);
        if (satelliteImageChangeDetail == null) {
            return new ResponseMessage(HttpStatus.OK.value(), "Dữ liệu không tồn tại", new MessageContent(HttpStatus.OK.value(), "Dữ liệu không tồn tại", null));
        }

        satelliteImageChangeDetail.setListRegion(this.satelliteImageChangeResultService.findByUuid(uuid));

        satelliteImageChangeDetail.setImagePathFileOrigin(
                satelliteImageChangeDetail.getImagePathFileOrigin().replace(ApplicationConfig.SATELLITE_ROOT_FOLDER_INTERNAL, ApplicationConfig.SATELLITE_MEDIA_LINK_ROOT_API)
                + File.separator + "infor.jpg"
        );

        satelliteImageChangeDetail.setImagePathFileCompare(
                satelliteImageChangeDetail.getImagePathFileCompare().replace(ApplicationConfig.SATELLITE_ROOT_FOLDER_INTERNAL, ApplicationConfig.SATELLITE_MEDIA_LINK_ROOT_API)
                + File.separator + "infor.jpg"
        );

        for (SatelliteImageChangeResultDTO satelliteImageChangeResult : satelliteImageChangeDetail.getListRegion()) {
            satelliteImageChangeResult.setImageFilePathOrigin(
                    satelliteImageChangeResult.getImageFilePathOrigin().replace(ApplicationConfig.SATELLITE_ROOT_FOLDER_INTERNAL, ApplicationConfig.SATELLITE_MEDIA_LINK_ROOT_API));
            satelliteImageChangeResult.setImageFilePathCompare(
                    satelliteImageChangeResult.getImageFilePathCompare().replace(ApplicationConfig.SATELLITE_ROOT_FOLDER_INTERNAL, ApplicationConfig.SATELLITE_MEDIA_LINK_ROOT_API));
        }

        return new ResponseMessage(new MessageContent(satelliteImageChangeDetail));
    }

    private SatelliteImageChangeRequestDTO buildSatelliteComparisonDTO(Map<String, Object> bodyParam, String createdUser) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String uuidKey = UUID.randomUUID().toString();
            String tileNumber = (String) bodyParam.getOrDefault("tileNumber", "");
            String timeFileOrigin = (String) bodyParam.getOrDefault("timeFileOrigin", "");
            String timeFileCompare = (String) bodyParam.getOrDefault("timeFileCompare", "");
            String imagePathFileOrigin = (String) bodyParam.getOrDefault("imagePathFileOrigin", "");
            String imagePathFileCompare = (String) bodyParam.getOrDefault("imagePathFileCompare", "");
            String newDate = dateFormat.format(new Date());

            SatelliteImageChangeRequestDTO satelliteImageChangeRequestDTO = SatelliteImageChangeRequestDTO.builder()
                    .uuidKey(uuidKey)
                    .tileNumber(tileNumber)
                    .timeFileOrigin(timeFileOrigin)
                    .timeFileCompare(timeFileCompare)
                    .imagePathFileOrigin(imagePathFileOrigin)
                    .imagePathFileCompare(imagePathFileCompare)
                    .createdBy(createdUser)
                    .timeReceiveResult(newDate)
                    .ingestTime(newDate)
                    .isDeleted(DataDeleteStatus.NOT_DELETED.code())
                    .build();

            return satelliteImageChangeRequestDTO;
        } catch (Exception e) {
            LOGGER.error("ex: ", e);
        }
        return null;
    }

    private SatelliteImageChangeFilterDTO buildSatelliteImageChangeFilterRequest(Map<String, Object> bodyParam) {
        try {
            Integer page = bodyParam.get("page") != null ? (Integer) bodyParam.get("page") : 0;
            Integer size = StringUtil.objectToInteger(bodyParam.get("size")) > maxLimitSatelliteData ? maxLimitSatelliteData : StringUtil.objectToInteger(bodyParam.get("size"));
            String sort = (String) bodyParam.getOrDefault("sort", "");
            String fromTime = (String) bodyParam.getOrDefault("fromTime", "");
            String toTime = (String) bodyParam.getOrDefault("toTime", "");
            List<String> tileNumberLst = bodyParam.get("tileNumberLst") != null ? (List<String>) bodyParam.get("tileNumberLst") : null;
            List<String> createdByLst = bodyParam.get("createdByLst") != null ? (List<String>) bodyParam.get("createdByLst") : null;
            List<Integer> processStatusLst = bodyParam.get("processStatusLst") != null ? (List<Integer>) bodyParam.get("processStatusLst") : null;
            String tileNumber = (String) bodyParam.getOrDefault("tileNumber", "");
            String timeFileOrigin = (String) bodyParam.getOrDefault("timeFileOrigin", "");
            String timeFileCompare = (String) bodyParam.getOrDefault("timeFileCompare", "");
            String timeReceiveResult = (String) bodyParam.getOrDefault("timeReceiveResult", "");
            String createdBy = (String) bodyParam.getOrDefault("createdBy", "");
            String ingestTime = (String) bodyParam.getOrDefault("ingestTime", "");
            Integer processStatus = bodyParam.get("processStatus") != null ? (Integer) bodyParam.get("processStatus") : null;
            String term = (String) bodyParam.getOrDefault("term", null);

            SatelliteImageChangeFilterDTO satelliteImageChangeFilterDTO = SatelliteImageChangeFilterDTO.builder()
                    .page(page)
                    .size(size)
                    .sort(sort)
                    .fromTime(fromTime)
                    .toTime(toTime)
                    .tileNumberLst(tileNumberLst)
                    .createdByLst(createdByLst)
                    .processStatusLst(processStatusLst)
                    .tileNumber(tileNumber)
                    .timeFileOrigin(timeFileOrigin)
                    .timeFileCompare(timeFileCompare)
                    .timeReceiveResult(timeReceiveResult)
                    .createdBy(createdBy)
                    .ingestTime(ingestTime)
                    .processStatus(processStatus)
                    .term(term)
                    .build();

            return satelliteImageChangeFilterDTO;
        } catch (Exception e) {
            LOGGER.error("ex: ", e);
        }

        return null;
    }

    // Vsat Media Data Object Analyzed
    public ResponseMessage insertVsatMediaDataObjectAnalyzed(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
        LOGGER.info("Create with request >>> {}", bodyParam);

        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        ABACResponseDTO abacStatus = authorizeABAC("POST", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            if (bodyParam == null || bodyParam.isEmpty()) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            }

            VsatMediaDataObjectAnalyzedRequestDTO vsatMediaDataObjectAnalyzedRequestDTO = buildVsatMediaDataObjectAnalyzedDTO(bodyParam);
            String validationMsg = new VsatMediaDataObjectAnalyzedValidation().ValidationVsatMediaDataObjectAnalyzed(vsatMediaDataObjectAnalyzedRequestDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }

            if (vsatMediaDataObjectAnalyzedRequestDTO != null) {
                callLinkObject(bodyParam);
                return new ResponseMessage(new MessageContent("success"));
            } else {
                callLinkObject(bodyParam);
                return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK.toString(),
                        new MessageContent(HttpStatus.OK.value(), HttpStatus.OK.toString(), null));

            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage getListVsatMediaDataObjectAnalyzed(String requestPath, Map<String, String> headerParam, String pathParam) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        ABACResponseDTO abacStatus = authorizeABAC("LIST", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            String vsatMediaDataAnalyzedUuidKey = pathParam;
            List<VsatMediaDataObjectAnalyzed> list = vsatMediaDataObjectAnalyzedService.findAll(vsatMediaDataAnalyzedUuidKey);
            return new ResponseMessage(new MessageContent(list));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage deleteVsatMediaDataObjectAnalyzed(String requestPath, Map<String, String> headerParam, String pathParam) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        ABACResponseDTO abacStatus = authorizeABAC("DELETE", dto.getUuid(), requestPath);

        if (abacStatus != null && abacStatus.getStatus()) {
            VsatMediaDataObjectAnalyzed vsatMediaDataObjectAnalyzed = vsatMediaDataObjectAnalyzedService.findByUuid(pathParam);
            if (vsatMediaDataObjectAnalyzed == null) {
                return new ResponseMessage(HttpStatus.OK.value(), "Dữ liệu không tồn tại",
                        new MessageContent(HttpStatus.OK.value(), "Dữ liệu không tồn tại", null));
            }
            this.vsatMediaDataObjectAnalyzedService.delete(DataDeleteStatus.DELETED.code(), pathParam);
            List<VsatMediaDataObjectAnalyzed> list = vsatMediaDataObjectAnalyzedService.findAll(vsatMediaDataObjectAnalyzed.getVsatMediaDataAnalyzedUuidKey());
            Map<String, Object> bodyParam = new HashMap<>();
            LinkObjectDTO linkObjectDTO = new LinkObjectDTO();
            linkObjectDTO.setVsatMediaDataAnalyzedUuidKey(vsatMediaDataObjectAnalyzed.getVsatMediaDataAnalyzedUuidKey());
            List<VsatMediaDataObjectAnalyzedDetailDTO> listData = list.stream().map(data
                    -> modelMapper.map(data, VsatMediaDataObjectAnalyzedDetailDTO.class)).collect(Collectors.toList());
            linkObjectDTO.setListObject(listData);
            bodyParam.put("vsatMediaDataAnalyzedUuidKey", linkObjectDTO.getVsatMediaDataAnalyzedUuidKey());
            bodyParam.put("listObject", linkObjectDTO.getListObject());

            callLinkObject(bodyParam);
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Xóa dữ liệu thành công", null));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    private VsatMediaDataObjectAnalyzedRequestDTO buildVsatMediaDataObjectAnalyzedDTO(Map<String, Object> bodyParam) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String vsatMediaDataAnalyzedUuidKey = (String) bodyParam.getOrDefault("vsatMediaDataAnalyzedUuidKey", "");
            String newDate = dateFormat.format(new Date());
            List<VsatMediaDataObjectAnalyzedDetailDTO> listData = null;
            if (bodyParam.get("listObject") != null) {
                listData = mapper.convertValue(
                        bodyParam.get("listObject"),
                        new TypeReference<List<VsatMediaDataObjectAnalyzedDetailDTO>>() {
                });
            }
            this.vsatMediaDataObjectAnalyzedService.checkExist(vsatMediaDataAnalyzedUuidKey);
            VsatMediaDataObjectAnalyzedRequestDTO vsatMediaDataObjectAnalyzedRequestDTO = null;
            for (int i = 0; i < listData.size(); i++) {
                String uuidKey = UUID.randomUUID().toString();
                String objectId = listData.get(i).getObjectId();
                String objectMmsi = null;
                if (!StringUtil.isNullOrEmpty(listData.get(i).getObjectMmsi())) {
                    objectMmsi = listData.get(i).getObjectMmsi();
                } else {
                    objectMmsi = null;

                }
                String objectUuid = listData.get(i).getObjectUuid();
                String objectType = listData.get(i).getObjectType();
                String objectName = listData.get(i).getObjectName();
                vsatMediaDataObjectAnalyzedRequestDTO = VsatMediaDataObjectAnalyzedRequestDTO.builder()
                        .uuidKey(uuidKey)
                        .vsatMediaDataAnalyzedUuidKey(vsatMediaDataAnalyzedUuidKey)
                        .objectId(objectId)
                        .objectMmsi(objectMmsi)
                        .objectUuid(objectUuid)
                        .objectType(objectType)
                        .objectName(objectName)
                        .ingestTime(newDate)
                        .isDeleted(DataDeleteStatus.NOT_DELETED.code())
                        .build();
                this.vsatMediaDataObjectAnalyzedService.save(vsatMediaDataObjectAnalyzedRequestDTO);
            }
            return vsatMediaDataObjectAnalyzedRequestDTO;
        } catch (Exception e) {
            LOGGER.error("ex: ", e);
        }
        return null;
    }

    public ResponseMessage updateNameObjectInternal(Map<String, Object> bodyParam) {
        String objectUuid = (String) bodyParam.get("objectUuid");
        String objectName = (String) bodyParam.get("objectName");
        List<VsatMediaDataObjectAnalyzed> listVsatMedia = vsatMediaDataObjectAnalyzedService.findAllByObjectUuid(objectUuid);
        vsatMediaDataObjectAnalyzedService.updateNameObjectInternal(objectUuid, objectName);
        if (listVsatMedia == null) {
            return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại",
                    new MessageContent(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại", null));
        } else {
            return new ResponseMessage(new MessageContent(listVsatMedia));
        }

    }

    public ResponseMessage searchAisListAllGeneral(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        ABACResponseDTO abacStatus = authorizeABAC("POST", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            try {
                VsatAisFilterListRequestDTO vsatAisFilterListRequestDTO = buildVsatAisFilterListRequest(bodyParam);

                MessageContent messageContent = vsatAisDataService.searchAisListAllGeneral(vsatAisFilterListRequestDTO);
                if (messageContent == null)
                    return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND,
                            new MessageContent(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, null));

                List<VsatAisResponseDTO> aisLst = (List<VsatAisResponseDTO>) messageContent.getData();
                if (aisLst == null || aisLst.isEmpty()) {
                    return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, new MessageContent(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, null));
                }

                try {
                    // get name of vessel type from redis
                    String key = Constant.REDIS_VESSEL_LST_KEY;

                    if (this.redisTemplate.hasKey(key)) {
                        List<VsatVesselType> vsatVesselTypesFromCaches = (List<VsatVesselType>) this.redisTemplate.opsForList().range(key, 0, Constant.REDIS_VESSEL_LST_FETCH_MAX);
                        if ( vsatVesselTypesFromCaches != null && !vsatVesselTypesFromCaches.isEmpty() ) {
                            for (VsatAisResponseDTO ais : aisLst) {
                                for (VsatVesselType vsatVesselType : vsatVesselTypesFromCaches) {
                                    if (vsatVesselType.getTypeCode().equals(ais.getTypeId() + "")) {
                                        ais.setTypeName(vsatVesselType.getTypeName());
                                        break;
                                    }
                                }
                            }
                        }
                    }

                } catch (Exception ex) {
                    LOGGER.error("ex: ", ex);
                }

                try {
                    // get name of country from redis
                    String key = Constant.REDIS_COUNTRIES_LST_KEY;

                    if (this.redisTemplate.hasKey(key)) {
                        List<Countries> countriesFromCaches = (List<Countries>) this.redisTemplate.opsForList().range(key, 0, Constant.REDIS_COUNTRIES_LST_FETCH_MAX);
                        if ( countriesFromCaches != null && !countriesFromCaches.isEmpty() ) {
                            for (VsatAisResponseDTO ais : aisLst) {
                                for (Countries countries : countriesFromCaches) {
                                    if (countries.getCountryId().equals(ais.getCountryId())) {
                                        ais.setCountryName(countries.getName());
                                        break;
                                    }
                                }
                            }
                        }
                    }

                } catch (Exception ex) {
                    LOGGER.error("ex: ", ex);
                }

                return new ResponseMessage(messageContent);
            } catch (Exception e) {
                LOGGER.error("ex: ", e);
                return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.OK.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        }
        return null;
    }

    private VsatAisFilterListRequestDTO buildVsatAisFilterListRequest(Map<String, Object> bodyParam) {
        try {
            String fromTime = (String) bodyParam.getOrDefault("fromTime", "");
            String toTime = (String) bodyParam.getOrDefault("toTime", "");
            String mmsi = (String) bodyParam.getOrDefault("mmsi", "");
            Integer limit = StringUtil.objectToInteger(bodyParam.get("limit")) > maxLimitVsatAisData ? maxLimitVsatAisData : StringUtil.objectToInteger(bodyParam.get("limit"));

            VsatAisFilterListRequestDTO vsatAisFilterListRequestDTO = VsatAisFilterListRequestDTO.builder()
                    .limit(limit)
                    .fromTime(fromTime)
                    .toTime(toTime)
                    .mmsi(mmsi)
                    .build();

            return vsatAisFilterListRequestDTO;
        } catch (Exception e) {
            LOGGER.error("ex: ", e);
        }

        return null;
    }

    public ResponseMessage checkExistByObjectUuid(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
//        // Check isLogged
//        AuthorizationResponseDTO dto = authenToken(headerParam);
//        if (dto == null) {
//            return unauthorizedResponse();
//        }
//        // Check ABAC
//        ABACResponseDTO abacStatus = authorizeABAC("DETAIL", dto.getUuid(), requestPath);
//        if (abacStatus != null && abacStatus.getStatus()) {
//            String vsatMediaDataAnalyzedObjectUuid = (String) bodyParam.getOrDefault("uuid", null);
//            String vsatMediaDataAnalyzedObjectType = (String) bodyParam.getOrDefault("objectType", null);
//            Boolean object = vsatMediaDataObjectAnalyzedService.checkExistByObjectUuid(vsatMediaDataAnalyzedObjectUuid, vsatMediaDataAnalyzedObjectType);
//            return new ResponseMessage(new MessageContent(object));
//        } else {
//            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
//                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
//        }

        String vsatMediaDataAnalyzedObjectUuid = (String) bodyParam.getOrDefault("uuid", null);
        String vsatMediaDataAnalyzedObjectType = (String) bodyParam.getOrDefault("objectType", null);
        Boolean object = vsatMediaDataObjectAnalyzedService.checkExistByObjectUuid(vsatMediaDataAnalyzedObjectUuid, vsatMediaDataAnalyzedObjectType);
//            return new ResponseMessage(new MessageContent(object));
        if (object) {
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Object exist", null));
        } else {
            return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                    new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
        }
    }
}
