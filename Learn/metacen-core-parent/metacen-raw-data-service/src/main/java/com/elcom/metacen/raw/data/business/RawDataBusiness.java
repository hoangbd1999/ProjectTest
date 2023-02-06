package com.elcom.metacen.raw.data.business;

import com.elcom.metacen.constant.ResourcePath;
import com.elcom.metacen.dto.redis.Countries;
import com.elcom.metacen.dto.redis.VsatVesselType;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.RequestMessage;
import com.elcom.metacen.message.ResponseMessage;
import com.elcom.metacen.raw.data.config.ApplicationConfig;
import com.elcom.metacen.raw.data.constant.Constant;
import com.elcom.metacen.raw.data.model.SatelliteImageData;
import com.elcom.metacen.raw.data.model.VsatMediaRelation;
import com.elcom.metacen.raw.data.model.dto.*;
import com.elcom.metacen.raw.data.service.ObjectTripService;
import com.elcom.metacen.raw.data.service.PositionService;
import com.elcom.metacen.raw.data.service.SatelliteImageDataService;
import com.elcom.metacen.raw.data.service.VsatMediaDataService;
import com.elcom.metacen.raw.data.validation.ObjectTripValidation;
import com.elcom.metacen.raw.data.validation.PositionValidation;
import com.elcom.metacen.raw.data.validation.SatelliteImageDataValidation;
import com.elcom.metacen.raw.data.validation.VsatMediaValidation;
import com.elcom.metacen.utils.StringUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import com.elcom.metacen.raw.data.messaging.rabbitmq.RabbitMQProperties;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Controller
public class RawDataBusiness extends BaseBusiness {

    private static final Logger LOGGER = LoggerFactory.getLogger(RawDataBusiness.class);

    @Autowired
    private PositionService positionService;

    @Autowired
    private VsatMediaDataService vsatMediaDataService;

    @Autowired
    private SatelliteImageDataService satelliteImageDataService;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${vsat_ais.max.records}")
    private Integer maxLimitVsatAis;

    @Value("${satellite_data.max.records}")
    private Integer maxLimitSatelliteData;

    @Autowired
    private ObjectTripService objectTripService;

    public ResponseMessage filterVsatMediaRawData(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
        LOGGER.info("Filter vsat media raw data with request >>> {}", bodyParam);

        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }

        ABACResponseDTO abacStatus = authorizeABAC("LIST", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            if (bodyParam == null || bodyParam.isEmpty()) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            }

            VsatMediaFilterDTO vsatMediaFilterDTO = buildVsatMediaFilterRequest(bodyParam);
            String validationMsg = new VsatMediaValidation().validateFilterVsatMediaRawData(vsatMediaFilterDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }

            Page<VsatMediaDTO> pagedResult = vsatMediaDataService.filterVsatMediaRawData(vsatMediaFilterDTO);
            return new ResponseMessage(new MessageContent(pagedResult.getContent(), pagedResult.getTotalElements()));
        } else {
            return new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(),
                    "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage filterVsatMediaDataOverall(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
        LOGGER.info("Filter vsat media overall with request >>> {}", bodyParam);

        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }

        ABACResponseDTO abacStatus = authorizeABAC("LIST", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            if (bodyParam == null || bodyParam.isEmpty()) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            }

            VsatMediaOverallFilterDTO vsatMediaFilterDTO = buildVsatMediaOverallFilterRequest(bodyParam);
            String validationMsg = new VsatMediaValidation().validateFilterVsatMediaOverall(vsatMediaFilterDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }

            Page<VsatMediaOverallDTO> pagedResult = vsatMediaDataService.filterVsatMediaDataOverall(vsatMediaFilterDTO);
            return new ResponseMessage(new MessageContent(pagedResult.getContent(), pagedResult.getTotalElements()));
        } else {
            return new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(),
                    "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage vsatMediaOverallStatistic(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
        LOGGER.info("Vsat media overall statistic with request >>> {}", bodyParam);

        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }

        ABACResponseDTO abacStatus = authorizeABAC("LIST", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            if (bodyParam == null || bodyParam.isEmpty()) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            }

            VsatMediaOverallStatisticFilterDTO vsatMediaOverallStatisticFilterDTO = buildVsatMediaOverallStatisticRequest(bodyParam);
            String validationMsg = new VsatMediaValidation().validateVsatMediaOverallStatistc(vsatMediaOverallStatisticFilterDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }

            VsatMediaOverallStatisticResponseDTO vsatMediaOverallStatisticResponseDTO = vsatMediaDataService.vsatMediaOverallStatistic(vsatMediaOverallStatisticFilterDTO);
            return new ResponseMessage(new MessageContent(vsatMediaOverallStatisticResponseDTO));
        } else {
            return new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(),
                    "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage getDetailMediaRelation(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
        LOGGER.info("Get detail media relation with request >>> {}", bodyParam);

        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }

        Map<String, Object> body = new HashMap<>();
        ABACResponseDTO abacStatus = authorizeABAC("LIST", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            if (bodyParam == null || bodyParam.isEmpty()) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            }

            DetailMediaRelationRequestDTO detailMediaRelationRequestDTO = buildDetailMediaRelationRequestDTO(bodyParam);
            String validationMsg = new VsatMediaValidation().validateDetailMediaRelationRequestDTO(detailMediaRelationRequestDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }

            return new ResponseMessage(vsatMediaDataService.getDetailMediaRelation(detailMediaRelationRequestDTO));
        } else {
            return new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(),
                    "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage filterVsatMediaRelationRawData(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
        LOGGER.info("Filter vsat media relation raw data with request >>> {}", bodyParam);

        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }

        Map<String, Object> body = new HashMap<>();
        ABACResponseDTO abacStatus = authorizeABAC("LIST", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            if (bodyParam == null || bodyParam.isEmpty()) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            }

            VsatMediaRelationFilterDTO vsatMediaRelationFilterDTO = buildVsatMediaRelationFilterRequest(bodyParam);
            String validationMsg = new VsatMediaValidation().validateFilterVsatMediaRelationRawData(vsatMediaRelationFilterDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }

            Page<VsatMediaRelation> pagedResult = vsatMediaDataService.filterVsatMediaRelationRawData(vsatMediaRelationFilterDTO);
            return new ResponseMessage(new MessageContent(pagedResult.getContent(), pagedResult.getTotalElements()));
        } else {
            return new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(),
                    "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage fetchMailInfo(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
        LOGGER.info("Fetch mail info with request >>> {}", bodyParam);

        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }

        Map<String, Object> body = new HashMap<>();
        ABACResponseDTO abacStatus = authorizeABAC("LIST", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            if (bodyParam == null || bodyParam.isEmpty()) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            }

            String emlFilePath = (String) bodyParam.get("emlFilePath");
            if (StringUtil.isNullOrEmpty(emlFilePath)) {
                return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                        new MessageContent(HttpStatus.OK.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            }

            return new ResponseMessage(new MessageContent(vsatMediaDataService.fetchMailInfo(emlFilePath)));
        } else {
            return new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(),
                    "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage fetchM3U8File(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
        LOGGER.info("Get M3U8 file with request >>> {}", bodyParam);

        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }

        Map<String, Object> body = new HashMap<>();
        ABACResponseDTO abacStatus = authorizeABAC("LIST", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            if (bodyParam == null || bodyParam.isEmpty()) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            }

            String filePathLocal = (String) bodyParam.get("filePathLocal");
            if (StringUtil.isNullOrEmpty(filePathLocal)) {
                return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                        new MessageContent(HttpStatus.OK.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            }

            return new ResponseMessage(new MessageContent(vsatMediaDataService.getM3U8File(filePathLocal)));
        } else {
            return new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(),
                    "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage convertAndFetchVideo(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
        LOGGER.info("Convert and fetch video with request >>> {}", bodyParam);

        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }

        Map<String, Object> body = new HashMap<>();
        ABACResponseDTO abacStatus = authorizeABAC("LIST", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            if (bodyParam == null || bodyParam.isEmpty()) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            }

            ConvertAndFetchVideoRequestDTO convertAndFetchVideoRequestDTO = buildConvertAndFetchVideoRequest(bodyParam);
            String validationMsg = new VsatMediaValidation().validateConvertAndFetchVideo(convertAndFetchVideoRequestDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }

            return new ResponseMessage(new MessageContent(vsatMediaDataService.convertAndFetchVideo(convertAndFetchVideoRequestDTO)));
        } else {
            return new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(),
                    "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage filterVsatAisRawData(Map<String, Object> bodyParam, Map<String, String> headerParam, String requestPath) {

        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }

        if (bodyParam == null || bodyParam.isEmpty()) {
            return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
        }

        ABACResponseDTO abacStatus = authorizeABAC("LIST", dto.getUuid(), requestPath);
        if (abacStatus == null || !abacStatus.getStatus()) {
            return new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }

        try {
            VsatAisFilterDTO vsatAisFilterDTO = buildVsatAisDataFilterRequest(bodyParam);
            String validationMsg = new PositionValidation().validateSearchVsatAis(vsatAisFilterDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.OK.value(), validationMsg, new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }

            Stopwatch stopWatch = Stopwatch.createStarted();

            MessageContent messageContent = this.positionService.filterVsatAisRawData(vsatAisFilterDTO);

            stopWatch.stop();
            LOGGER.info("Fetch VsatAisLst from DB elapsed: [ {} ] ms", stopWatch.elapsed(TimeUnit.MILLISECONDS));

            if (messageContent == null || messageContent.getData() == null) {
                return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, new MessageContent(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, null));
            }

            List<VsatAisDTO> aisLst = (List<VsatAisDTO>) messageContent.getData();
            if (aisLst == null || aisLst.isEmpty()) {
                return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, new MessageContent(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, null));
            }

            try {
                // get name of vessel type from redis
                String key = Constant.REDIS_VESSEL_LST_KEY;

                stopWatch.reset();
                stopWatch.start();

                if (this.redisTemplate.hasKey(key)) {
                    List<VsatVesselType> vsatVesselTypesFromCaches = (List<VsatVesselType>) this.redisTemplate.opsForList().range(key, 0, Constant.REDIS_VESSEL_LST_FETCH_MAX);
                    if (vsatVesselTypesFromCaches != null && !vsatVesselTypesFromCaches.isEmpty()) {
                        for (VsatAisDTO ais : aisLst) {
                            for (VsatVesselType vsatVesselType : vsatVesselTypesFromCaches) {
                                if (vsatVesselType.getTypeCode().equals(ais.getTypeId() + "")) {
                                    ais.setTypeName(vsatVesselType.getTypeName());
                                    break;
                                }
                            }
                        }
                    }
                }

                stopWatch.stop();
                LOGGER.info("Filter from redis for vesselTypeName elapsed: [ {} ] ms", stopWatch.elapsed(TimeUnit.MILLISECONDS));

            } catch (Exception ex) {
                LOGGER.error("ex: ", ex);
            }

            try {
                // get name of country from redis
                String key = Constant.REDIS_COUNTRIES_LST_KEY;

                stopWatch.reset();
                stopWatch.start();

                if (this.redisTemplate.hasKey(key)) {
                    List<Countries> countriesFromCaches = (List<Countries>) this.redisTemplate.opsForList().range(key, 0, Constant.REDIS_COUNTRIES_LST_FETCH_MAX);
                    if (countriesFromCaches != null && !countriesFromCaches.isEmpty()) {
                        for (VsatAisDTO ais : aisLst) {
                            for (Countries countries : countriesFromCaches) {
                                if (countries.getCountryId().equals(ais.getCountryId())) {
                                    ais.setCountryName(countries.getName());
                                    break;
                                }
                            }
                        }
                    }
                }

                stopWatch.stop();
                LOGGER.info("Filter from redis for countryName elapsed: [ {} ] ms", stopWatch.elapsed(TimeUnit.MILLISECONDS));

            } catch (Exception ex) {
                LOGGER.error("ex: ", ex);
            }

            return new ResponseMessage(messageContent);

        } catch (Exception e) {
            LOGGER.error("ex: ", e);
        }
        return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), new MessageContent(HttpStatus.OK.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
    }

    public ResponseMessage filterAisRawData(Map<String, Object> bodyParam, Map<String, String> headerParam, String requestPath) {

        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }

        if (bodyParam == null || bodyParam.isEmpty()) {
            return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
        }

        ABACResponseDTO abacStatus = authorizeABAC("LIST", dto.getUuid(), requestPath);
        if (abacStatus == null || !abacStatus.getStatus()) {
            return new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }

        try {
            AisDataFilterDTO aisDataFilterDTO = buildAisDataFilterRequest(bodyParam);
            String validationMsg = new PositionValidation().validateSearchAisData(aisDataFilterDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.OK.value(), validationMsg, new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }

            Stopwatch stopWatch = Stopwatch.createStarted();

            MessageContent messageContent = this.positionService.filterAisRawData(aisDataFilterDTO);

            stopWatch.stop();
            LOGGER.info("Fetch AisLst from DB elapsed: [ {} ] ms", stopWatch.elapsed(TimeUnit.MILLISECONDS));

            if (messageContent == null || messageContent.getData() == null) {
                return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, new MessageContent(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, null));
            }

            /*List<AisDataDTO> aisLst = (List<AisDataDTO>) messageContent.getData();

            // get name of country from redis
            try {
                String key = Constant.REDIS_COUNTRIES_LST_KEY;
                if (this.redisTemplate.hasKey(key)) {
                    List<Countries> countriesFromCaches = (List<Countries>) this.redisTemplate.opsForList().range(key, 0, Constant.REDIS_COUNTRIES_LST_FETCH_MAX);
                    if (countriesFromCaches != null && !countriesFromCaches.isEmpty() && aisLst != null && !aisLst.isEmpty()) {
                        for (AisDataDTO ais : aisLst) {
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
            }*/
            return new ResponseMessage(messageContent);

        } catch (Exception e) {
            LOGGER.error("ex: ", e);
        }
        return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), new MessageContent(HttpStatus.OK.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
    }

    public ResponseMessage findPositionOverall(Map<String, Object> bodyParam, Map<String, String> headerParam, String requestPath) {

        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null)
            return unauthorizedResponse();

        if (bodyParam == null || bodyParam.isEmpty())
            return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));

        ABACResponseDTO abacStatus = authorizeABAC("LIST", dto.getUuid(), requestPath);
        if (abacStatus == null || !abacStatus.getStatus())
            return new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));

        try {
            PositionOverallRequest request = buildPositionOverallRequest(bodyParam);

            String validationMsg = new PositionValidation().validateSearchPositionGeneral(request);
            if (validationMsg != null)
                return new ResponseMessage(HttpStatus.OK.value(), validationMsg, new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));

            Stopwatch stopWatch = Stopwatch.createStarted();

            List<PositionResponseDTO> positionLstResponse = null;

            if ( ( request.getSourceType().toUpperCase().contains("AIS") && request.getSourceType().toUpperCase().contains("VSAT") )
                    || StringUtil.isNullOrEmpty(request.getSourceType()) ) { // Nếu khai thác cả 2 hệ thống AIS và VSAT

                List<PositionResponseDTO> vsatPositionLst = this.positionService.findPositionOverallFromVsatSystem(request);

                if (request.isMediaFilterAccept() && vsatPositionLst != null && !vsatPositionLst.isEmpty()) {
                    List<Long> objLstOnMedia = this.positionService.findObjLstOnMedia(request.getFromTime(), request.getToTime(), request.getMediaFilterType(), request.getMediaFilterFormat());
                    if (objLstOnMedia == null || objLstOnMedia.isEmpty()) {
                        LOGGER.info("No ais record found for this media filter!");
                        vsatPositionLst = null;
                    } else {
                        vsatPositionLst = Collections.synchronizedList(vsatPositionLst.stream().filter(
                                position -> objLstOnMedia.contains(position.getMmsi().longValue())
                        ).collect(Collectors.toList()));
                    }
//                    if (vsatPositionLst == null || vsatPositionLst.isEmpty())
//                        LOGGER.info("No ais record found for this media filter!");
                }

                List<PositionResponseDTO> aisPositionLst = this.positionService.findPositionOverallFromAisSystem(request);

                if (vsatPositionLst != null && !vsatPositionLst.isEmpty()) {

                    if (aisPositionLst != null && !aisPositionLst.isEmpty())
                        vsatPositionLst.addAll(aisPositionLst);

                    positionLstResponse = Collections.synchronizedList(vsatPositionLst);

                } else if (aisPositionLst != null && !aisPositionLst.isEmpty())
                    positionLstResponse = Collections.synchronizedList(aisPositionLst);
                
                /*if( positionLstResponse != null && !positionLstResponse.isEmpty() ) {
                    // Ordering ingestTime by descending
                    Collections.sort(positionLstResponse, Collections.reverseOrder());
                    
                    // Lọc bỏ những bản tin có long/lat trùng nhau, nếu trùng thì giữ lại vị trí gần nhất (theo ingestTime)
                    positionLstResponse = Collections
                                            .synchronizedList(positionLstResponse
                                                                .stream()
                                                                .filter(super.distinctListByKeys(PositionResponseDTO::getLongitude, PositionResponseDTO::getLatitude))
                                                                .collect(Collectors.toList())
                                            );
                }*/
                // LOGGER.info("positionLstResponse   -> {}", JSONConverter.toJSON(positionLstResponse));
            }
            else if ("AIS".equalsIgnoreCase(request.getSourceType())) { // Nếu chỉ khai thác hệ thống AIS
                positionLstResponse = this.positionService.findPositionOverallFromAisSystem(request);
            }
            else if ("VSAT".equalsIgnoreCase(request.getSourceType())) { // Nếu chỉ khai thác hệ thống VSAT
                positionLstResponse = this.positionService.findPositionOverallFromVsatSystem(request);

                if (request.isMediaFilterAccept() && positionLstResponse != null && !positionLstResponse.isEmpty()) {
                    List<Long> objLstOnMedia = this.positionService.findObjLstOnMedia(request.getFromTime(), request.getToTime(), request.getMediaFilterType(), request.getMediaFilterFormat());

                    if (objLstOnMedia == null || objLstOnMedia.isEmpty()) {
                        LOGGER.info("No ais record found for this media filter!");
                        return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, new MessageContent(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, Collections.emptyList()));
                    }

                    positionLstResponse = Collections.synchronizedList(positionLstResponse.stream().filter(
                            position -> objLstOnMedia.contains(position.getMmsi().longValue())
                    ).collect(Collectors.toList()));

                    if (positionLstResponse == null || positionLstResponse.isEmpty()) {
                        LOGGER.info("No ais record found for this media filter!");
                        return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, new MessageContent(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, Collections.emptyList()));
                    }
                }
            }

            stopWatch.stop();
            LOGGER.info("Fetch PositionLst from DB elapsed: [ {} ] ms", stopWatch.elapsed(TimeUnit.MILLISECONDS));

            if (positionLstResponse == null || positionLstResponse.isEmpty())
                return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, new MessageContent(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, Collections.emptyList()));

            // Ordering ingestTime by descending
            Collections.sort(positionLstResponse, Collections.reverseOrder());

            // Lọc bỏ những bản tin có long/lat trùng nhau, nếu trùng thì giữ lại vị trí gần nhất (theo ingestTime)
            positionLstResponse = Collections
                                    .synchronizedList(positionLstResponse
                                                        .stream()
                                                        .filter(super.distinctListByKeys(PositionResponseDTO::getLongitude, PositionResponseDTO::getLatitude))
                                                        .collect(toList())
                                    );
            try {
                // get name of vessel type from redis
                String key = Constant.REDIS_VESSEL_LST_KEY;

                stopWatch.reset();
                stopWatch.start();

                if ( this.redisTemplate.hasKey(key) ) {
                    List<VsatVesselType> vsatVesselTypesFromCaches = (List<VsatVesselType>) this.redisTemplate.opsForList().range(key, 0, Constant.REDIS_VESSEL_LST_FETCH_MAX);
                    if (vsatVesselTypesFromCaches != null && !vsatVesselTypesFromCaches.isEmpty())
                        for (PositionResponseDTO ais : positionLstResponse)
                            for (VsatVesselType vsatVesselType : vsatVesselTypesFromCaches)
                                if (vsatVesselType.getTypeCode().equals(ais.getTypeId() + "")) {
                                    ais.setTypeName(vsatVesselType.getTypeName());
                                    break;
                                }
                }

                stopWatch.stop();
                LOGGER.info("Filter from redis for vesselTypeName elapsed: [ {} ] ms", stopWatch.elapsed(TimeUnit.MILLISECONDS));

            } catch (Exception ex) {
                LOGGER.error("ex: ", ex);
            }

            try {
                // get name of country from redis
                String key = Constant.REDIS_COUNTRIES_LST_KEY;

                stopWatch.reset();
                stopWatch.start();

                if (this.redisTemplate.hasKey(key)) {
                    List<Countries> countriesFromCaches = (List<Countries>) this.redisTemplate.opsForList().range(key, 0, Constant.REDIS_COUNTRIES_LST_FETCH_MAX);
                    if (countriesFromCaches != null && !countriesFromCaches.isEmpty())
                        for (PositionResponseDTO ais : positionLstResponse)
                            for (Countries countries : countriesFromCaches)
                                if (countries.getCountryId().equals(ais.getCountryId())) {
                                    ais.setCountryName(countries.getName());
                                    break;
                                }
                }

                stopWatch.stop();
                LOGGER.info("Filter from redis for countryName elapsed: [ {} ] ms", stopWatch.elapsed(TimeUnit.MILLISECONDS));

            } catch (Exception ex) {
                LOGGER.error("ex: ", ex);
            }

            return new ResponseMessage(
                    new MessageContent(positionLstResponse)
            );

        } catch (Exception e) {
            LOGGER.error("ex: ", e);
        }
        return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), new MessageContent(HttpStatus.OK.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
    }

    public ResponseMessage filterSatelliteImageRawData(Map<String, Object> bodyParam, Map<String, String> headerParam, String requestPath) {
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
            SatelliteImageDataFilterDTO satelliteImageDataFilterDTO = buildSatelliteImageDataFilterRequest(bodyParam);
            String validationMsg = new SatelliteImageDataValidation().validateSatelliteImageDataFilter(satelliteImageDataFilterDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.OK.value(), validationMsg, new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }

            Page<SatelliteImageDataDTO> pagedResult = satelliteImageDataService.filterSatelliteImageData(satelliteImageDataFilterDTO);
            if (pagedResult == null) {
                return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, new MessageContent(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, null));
            }
            for (int i = 0; i < pagedResult.getContent().size(); i++) {
                String tileNumberReplace = pagedResult.getContent().get(i).getTileNumber();
                pagedResult.getContent().get(i).setTileNumber(tileNumberReplace.substring(1, tileNumberReplace.length()));
            }
            return new ResponseMessage(new MessageContent(pagedResult.getContent(), pagedResult.getTotalElements()));
        } catch (Exception e) {
            LOGGER.error("ex: ", e);
            return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), new MessageContent(HttpStatus.OK.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
        }
    }

    public ResponseMessage filterSatelliteImageRawDataForMap(Map<String, Object> bodyParam, Map<String, String> headerParam, String requestPath) {
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
            SatelliteImageDataFilterDTO satelliteImageDataFilterDTO = buildSatelliteImageDataFilterRequest(bodyParam);
            String validationMsg = new SatelliteImageDataValidation().validateSatelliteImageDataFilter(satelliteImageDataFilterDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.OK.value(), validationMsg, new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }

            Page<SatelliteImageDataDTO> pagedResult = satelliteImageDataService.filterSatelliteImageDataForMap(satelliteImageDataFilterDTO);
            if (pagedResult == null) {
                return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, new MessageContent(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, null));
            }

            return new ResponseMessage(new MessageContent(pagedResult.getContent(), pagedResult.getTotalElements()));

        } catch (Exception e) {
            LOGGER.error("ex: ", e);
            return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), new MessageContent(HttpStatus.OK.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
        }
    }

    public ResponseMessage filterAisMapping(String urlParam, Map<String, String> headerParam, String requestPath) {

        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }

        ABACResponseDTO abacStatus = authorizeABAC("LIST", dto.getUuid(), requestPath);
        if (abacStatus == null || !abacStatus.getStatus()) {
            return new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }

        try {

            if (urlParam == null || urlParam.isEmpty()) {
                return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            }

            Map<String, String> params = StringUtil.getUrlParamValues(urlParam);
            Integer page = params.get("page") != null ? Integer.parseInt(params.get("page")) : 0;
            Integer size = params.get("size") != null ? Integer.parseInt(params.get("size")) : 20;
            String term = params.getOrDefault("term", "");

            Page<AisDataDTO> pagedResult = this.positionService.filterAisMapping(page, size, term);
            if (pagedResult == null) {
                return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, new MessageContent(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, null));
            }

            return new ResponseMessage(new MessageContent(pagedResult.getContent(), pagedResult.getTotalElements()));

        } catch (Exception e) {
            LOGGER.error("ex: ", e);
        }

        return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), new MessageContent(HttpStatus.OK.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
    }

    public ResponseMessage getDetailSatellite(String requestPath, Map<String, String> headerParam, String pathParam) throws IOException {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }

        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC("DETAIL", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            String uuid = pathParam;
            SatelliteImageData satelliteImageData = satelliteImageDataService.findByUuid(uuid);
            if (satelliteImageData == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại", null));
            }

            String rootPathLocal = satelliteImageData.getRootDataFolderPath();
            String rootPath = rootPathLocal.replace(ApplicationConfig.SATELLITE_ROOT_FOLDER_INTERNAL, ApplicationConfig.SATELLITE_MEDIA_LINK_ROOT_API);
            File folder = new File(rootPathLocal);
            File[] listOfFiles = folder.listFiles();
            List<MetaDataSatelliteDTO> listMetaData = new ArrayList<>();
            if (listOfFiles != null && listOfFiles.length > 0) {
                for (int i = 0; i < listOfFiles.length; i++) {
                    if (listOfFiles[i].isFile()) {
                        String fileName = listOfFiles[i].getName();

                        MetaDataSatelliteDTO metaDataSatelliteDTO = new MetaDataSatelliteDTO();
                        metaDataSatelliteDTO.setFilePath(rootPath + "/" + fileName);
                        metaDataSatelliteDTO.setFilePathLocal(rootPathLocal + "/" + fileName);
                        metaDataSatelliteDTO.setFileName(fileName);
                        metaDataSatelliteDTO.setFileSize(this.convertFileSize(listOfFiles[i].length()));
                        listMetaData.add(metaDataSatelliteDTO);
                    }
                }
            }

            SatelliteDetailDTO satelliteDetailDTO = modelMapper.map(satelliteImageData, SatelliteDetailDTO.class);
            satelliteDetailDTO.setMetadataFiles(listMetaData);

            String imageFilePathLocal = satelliteDetailDTO.getRootDataFolderPath() + "/infor.jpg";
            satelliteDetailDTO.setImageFilePathLocal(imageFilePathLocal);
            satelliteDetailDTO.setImageFilePath(imageFilePathLocal.replace(ApplicationConfig.SATELLITE_ROOT_FOLDER_INTERNAL, ApplicationConfig.SATELLITE_MEDIA_LINK_ROOT_API));

            return new ResponseMessage(new MessageContent(satelliteDetailDTO));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    private String convertFileSize(Long size) {
        String fileSize = "";
        if (size / 1024 == 0) {
            fileSize = size + " Bytes";
        } else if (size / 1048576 == 0) {
            fileSize = size / 1024 + " KB";
        } else if (size / 1073741824 == 0) {
            fileSize = size / 1048576 + " MB";
        } else if (size / 1099511627776l == 0) {
            fileSize = size / 1073741824 + " GB";
        } else if (size / 1125899906842624l == 0) {
            fileSize = size / 1099511627776l + " TB";
        } else if (size / 1125899906842624l == 0) {
            fileSize = size / 1125899906842624l + " PB";
        }

        return fileSize;
    }

    private VsatMediaFilterDTO buildVsatMediaFilterRequest(Map<String, Object> bodyParam) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Integer page = bodyParam.get("page") != null ? (Integer) bodyParam.get("page") : 0;
            Integer size = bodyParam.get("size") != null ? (Integer) bodyParam.get("size") : 20;
            String sort = (String) bodyParam.getOrDefault("sort", "");
            String term = (String) bodyParam.getOrDefault("term", "");
            String fromTime = (String) bodyParam.get("fromTime");
            String toTime = (String) bodyParam.get("toTime");
            List<Integer> dataSourceIds = bodyParam.get("dataSourceIds") != null ? (List<Integer>) bodyParam.get("dataSourceIds") : null;
            List<Integer> mediaTypeIds = bodyParam.get("mediaTypeIds") != null ? (List<Integer>) bodyParam.get("mediaTypeIds") : null;
            List<Integer> processStatusLst = bodyParam.get("processStatusLst") != null ? (List<Integer>) bodyParam.get("processStatusLst") : null;

            List<AdvanceFilterDTO> filterLst = null;
            if (bodyParam.get("filter") != null) {
                filterLst = mapper.convertValue(bodyParam.get("filter"), new TypeReference<List<AdvanceFilterDTO>>() {
                });
            }

            // filter theo cột
            String dataVendor = (String) bodyParam.get("dataVendor");
            String dataSourceName = (String) bodyParam.get("dataSourceName");
            String sourceIp = (String) bodyParam.get("sourceIp");
            Long sourcePort = (bodyParam.get("sourcePort") != "" && bodyParam.get("sourcePort") != null) ? ((Number) bodyParam.get("sourcePort")).longValue() : null;
            String sourcePhone = (String) bodyParam.get("sourcePhone");
            String sourceName = (String) bodyParam.get("sourceName");
            String destIp = (String) bodyParam.get("destIp");
            Long destPort = (bodyParam.get("destPort") != "" && bodyParam.get("destPort") != null) ? ((Number) bodyParam.get("destPort")).longValue() : null;
            String destPhone = (String) bodyParam.get("destPhone");
            String destName = (String) bodyParam.get("destName");
            String mediaTypeName = (String) bodyParam.get("mediaTypeName");
            String fileType = (String) bodyParam.get("fileType");
            BigInteger fileSize = (bodyParam.get("fileSize") != "" && bodyParam.get("fileSize") != null) ? BigInteger.valueOf(((Number) bodyParam.get("fileSize")).intValue()) : null;

            VsatMediaFilterDTO vsatMediaFilterDTO = VsatMediaFilterDTO.builder()
                    .page(page)
                    .size(size)
                    .sort(sort)
                    .term(term)
                    .fromTime(fromTime)
                    .toTime(toTime)
                    .dataSourceIds(dataSourceIds)
                    .mediaTypeIds(mediaTypeIds)
                    .processStatusLst(processStatusLst)
                    .filter(filterLst)
                    .dataVendor(dataVendor)
                    .dataSourceName(dataSourceName)
                    .sourceIp(sourceIp)
                    .sourcePort(sourcePort)
                    .sourcePhone(sourcePhone)
                    .sourceName(sourceName)
                    .destIp(destIp)
                    .destPort(destPort)
                    .destPhone(destPhone)
                    .destName(destName)
                    .mediaTypeName(mediaTypeName)
                    .fileType(fileType)
                    .fileSize(fileSize)
                    .build();
            return vsatMediaFilterDTO;
        } catch (Exception e) {
            LOGGER.error("ex: ", e);
        }
        return null;
    }

    private VsatMediaOverallFilterDTO buildVsatMediaOverallFilterRequest(Map<String, Object> bodyParam) {
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
            Integer direction = (bodyParam.get("direction") != "" && bodyParam.get("direction") != null) ? ((Number) bodyParam.get("direction")).intValue() : null;
            String eventTime = (String) bodyParam.getOrDefault("eventTime", "");
            String processTime = (String) bodyParam.getOrDefault("processTime", "");

            VsatMediaOverallFilterDTO vsatMediaOverallFilterDTO = VsatMediaOverallFilterDTO.builder()
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
                    .direction(direction)
                    .eventTime(eventTime)
                    .processTime(processTime)
                    .build();
            return vsatMediaOverallFilterDTO;
        } catch (Exception e) {
            LOGGER.error("ex: ", e);
        }
        return null;
    }

    private VsatMediaOverallStatisticFilterDTO buildVsatMediaOverallStatisticRequest(Map<String, Object> bodyParam) {
        try {
            String fromTime = (String) bodyParam.get("fromTime");
            String toTime = (String) bodyParam.get("toTime");
            Long sourceId = (bodyParam.get("sourceId") != "" && bodyParam.get("sourceId") != null) ? ((Number) bodyParam.get("sourceId")).longValue() : null;

            VsatMediaOverallStatisticFilterDTO vsatMediaOverallStatisticFilterDTO = VsatMediaOverallStatisticFilterDTO.builder()
                    .fromTime(fromTime)
                    .toTime(toTime)
                    .sourceId(sourceId)
                    .build();
            return vsatMediaOverallStatisticFilterDTO;
        } catch (Exception e) {
            LOGGER.error("ex: ", e);
        }
        return null;
    }

    private VsatAisFilterDTO buildVsatAisDataFilterRequest(Map<String, Object> bodyParam) {
        try {
            String fromTime = (String) bodyParam.get("fromTime");
            String toTime = (String) bodyParam.get("toTime");

            Integer limitObj = StringUtil.objectToInteger(bodyParam.get("limit"));
            Integer limit = limitObj != null && limitObj <= maxLimitVsatAis ? limitObj : maxLimitVsatAis;

            String term = (String) bodyParam.getOrDefault("term", "");
            Integer mmsi = StringUtil.objectToInteger(bodyParam.get("mmsi"));
            String sourceIps = (String) bodyParam.getOrDefault("sourceIps", "");
            String destIps = (String) bodyParam.getOrDefault("destIps", "");

            List<String> dataVendors = bodyParam.get("dataVendors") != null ? (List<String>) bodyParam.get("dataVendors") : null;
            // quốc gia
            List<Integer> countryIds = bodyParam.get("countryId") != null ? (List<Integer>) bodyParam.get("countryId") : null;
            // loại tàu
            List<Integer> typeIds = bodyParam.get("typeId") != null ? (List<Integer>) bodyParam.get("typeId") : null;
            // nguồn thu
            List<Integer> dataSources = bodyParam.get("dataSourceId") != null ? (List<Integer>) bodyParam.get("dataSourceId") : null;
            // trạng thái
            List<Integer> processStatus = bodyParam.get("processStatus") != null ? (List<Integer>) bodyParam.get("processStatus") : null;

            VsatAisFilterDTO vsatAisFilterDTO = VsatAisFilterDTO.builder()
                    .term(term)
                    .fromTime(fromTime)
                    .toTime(toTime)
                    .limit(limit)
                    .mmsi(mmsi)
                    .sourceIps(sourceIps)
                    .destIps(destIps)
                    .dataVendors(dataVendors)
                    .countryId(countryIds)
                    .typeId(typeIds)
                    .dataSourceId(dataSources)
                    .processStatus(processStatus)
                    .build();
            return vsatAisFilterDTO;
        } catch (Exception e) {
            LOGGER.error("ex: ", e);
        }
        return null;
    }

    private AisDataFilterDTO buildAisDataFilterRequest(Map<String, Object> bodyParam) {
        try {
            String term = (String) bodyParam.getOrDefault("term", "");

            Integer limitObj = StringUtil.objectToInteger(bodyParam.get("limit"));
            Integer limit = limitObj != null && limitObj <= maxLimitVsatAis ? limitObj : maxLimitVsatAis;

            String fromTime = (String) bodyParam.get("fromTime");
            String toTime = (String) bodyParam.get("toTime");
            Integer mmsi = StringUtil.objectToInteger(bodyParam.get("mmsi"));
            String imo = (String) bodyParam.getOrDefault("imo", "");
            Double longitude = (bodyParam.get("longitude") != "") && (bodyParam.get("longitude") != null) ? ((Number) bodyParam.getOrDefault("longitude", "")).doubleValue() : null;
            Double latitude = (bodyParam.get("latitude") != "") && (bodyParam.get("latitude") != null) ? ((Number) bodyParam.getOrDefault("latitude", "")).doubleValue() : null;

            List<String> dataVendors = bodyParam.get("dataVendors") != null ? (List<String>) bodyParam.get("dataVendors") : null;

            // trạng thái
            List<Integer> processStatus = bodyParam.get("processStatus") != null ? (List<Integer>) bodyParam.get("processStatus") : null;

            AisDataFilterDTO aisDataFilterDTO = AisDataFilterDTO.builder()
                    .term(term)
                    .fromTime(fromTime)
                    .toTime(toTime)
                    .mmsi(mmsi)
                    .imo(imo)
                    .longitude(longitude)
                    .latitude(latitude)
                    .dataVendors(dataVendors)
                    .limit(limit)
                    .processStatus(processStatus)
                    .build();
            return aisDataFilterDTO;
        } catch (Exception e) {
            LOGGER.error("ex: ", e);
        }
        return null;
    }

    private PositionOverallRequest buildPositionOverallRequest(Map<String, Object> bodyParam) {
        try {
            String fromTime = (String) bodyParam.get("fromTime");
            String toTime = (String) bodyParam.get("toTime");

            Integer limitObj = StringUtil.objectToInteger(bodyParam.get("limit"));
            Integer limit = limitObj != null && limitObj <= maxLimitVsatAis ? limitObj : maxLimitVsatAis;

            String term = (String) bodyParam.getOrDefault("term", "");
            String mmsiVsat = (String) bodyParam.getOrDefault("mmsiVsat", "");
            String mmsiAis = (String) bodyParam.getOrDefault("mmsiAis", "");
            String imoVsat = (String) bodyParam.getOrDefault("imoVsat", "");
            String imoAis = (String) bodyParam.getOrDefault("imoAis", "");
            String sourceIps = (String) bodyParam.getOrDefault("sourceIps", "");
            String destIps = (String) bodyParam.getOrDefault("destIps", "");
            String sourceType = (String) bodyParam.getOrDefault("sourceType", "");

            Object dataVendorsVsatObj = bodyParam.get("dataVendorsVsat");
            List<String> dataVendorsVsat = dataVendorsVsatObj != null ? (List<String>) dataVendorsVsatObj : null;

            Object dataVendorsAisObj = bodyParam.get("dataVendorsAis");
            List<String> dataVendorsAis = dataVendorsAisObj != null ? (List<String>) dataVendorsAisObj : null;

            Object groupIdsObj = bodyParam.get("groupIds");
            List<String> groupIds = groupIdsObj != null ? (List<String>) groupIdsObj : null;

            Object tileCoordinatesObj = bodyParam.get("tileCoordinates");
            List<String> tileCoordinates = tileCoordinatesObj != null ? (List<String>) tileCoordinatesObj : null;

            Object countryIdsObj = bodyParam.get("countryIds");
            List<Integer> countryIds = countryIdsObj != null ? (List<Integer>) countryIdsObj : null;

            Object dataSourceIdsObj = bodyParam.get("dataSourceIds");
            List<Integer> dataSourceIds = dataSourceIdsObj != null ? (List<Integer>) dataSourceIdsObj : null;

            Object areaIdsObj = bodyParam.get("areaIds");
            List<Integer> areaIds = areaIdsObj != null ? (List<Integer>) areaIdsObj : null;

            Boolean mediaFilterAccept = false;
            Object mediaFilterAcceptObj = (Object) bodyParam.get("mediaFilterAccept");
            if (mediaFilterAcceptObj != null) {
                mediaFilterAccept = (Boolean) mediaFilterAcceptObj;
            }

            Object mediaFilterTypeObj = bodyParam.get("mediaFilterType");
            List<Integer> mediaFilterType = mediaFilterTypeObj != null ? (List<Integer>) mediaFilterTypeObj : null;

            Object mediaFilterFormatObj = bodyParam.get("mediaFilterFormat");
            List<String> mediaFilterFormat = mediaFilterFormatObj != null ? (List<String>) mediaFilterFormatObj : null;

            return PositionOverallRequest.builder()
                    .fromTime(fromTime != null ? fromTime.trim() : null)
                    .toTime(toTime != null ? toTime.trim() : null)
                    .term(term != null ? term.trim() : null)
                    .limit(limit)
                    .mmsiVsat(mmsiVsat != null ? mmsiVsat.trim() : null)
                    .mmsiAis(mmsiAis != null ? mmsiAis.trim() : null)
                    .imoVsat(imoVsat != null ? imoVsat.trim() : null)
                    .imoAis(imoAis != null ? imoAis.trim() : null)
                    .sourceIps(sourceIps != null ? sourceIps.trim() : null)
                    .destIps(destIps != null ? destIps.trim() : null)
                    .dataVendorsVsat(dataVendorsVsat)
                    .dataVendorsAis(dataVendorsAis)
                    .countryIds(countryIds)
                    .dataSourceIds(dataSourceIds)
                    .groupIds(groupIds)
                    .tileCoordinates(tileCoordinates)
                    .areaIds(areaIds)
                    .sourceType(sourceType != null ? sourceType.trim() : null)
                    .mediaFilterAccept(mediaFilterAccept.booleanValue())
                    .mediaFilterType(mediaFilterType)
                    .mediaFilterFormat(mediaFilterFormat)
                    .build();

        } catch (Exception e) {
            LOGGER.error("ex: ", e);
        }
        return null;
    }

//    public static void main(String[] args) {
//        String sql = " SELECT t.mmsi AS mmsi, t.draught AS draught, dimA AS dimA, dimB AS dimB, dimC AS dimC, dimD AS dimD, t.name AS name, t.callSign AS callSign, t.imo AS imo "
//                + " , t.rot AS rot, t.sog AS sog, t.cog AS cog, t.longitude AS longitude, t.latitude AS latitude, t.eventTime AS eventTime, t.sourceIp AS sourceIp, t.destIp AS destIp "
//                + " , t.typeId AS typeId, t.countryId AS countryId, t.dataSourceName AS dataSourceName, t.dataVendor AS dataVendor "
//                + " FROM ( SELECT * FROM ( "
//                + "    SELECT uuidKey, mmsi, eventTime, ingestTime, row_number() OVER ( PARTITION BY mmsi ORDER BY eventTime DESC, ingestTime DESC ) AS rank "
//                + "    FROM %s t1 "
//                + "    WHERE "
//                + "    SETTINGS allow_experimental_window_functions = 1 "
//                + " ) WHERE rank = 1 ) r "
//                + " INNER JOIN ( "
//                + "   SELECT uuidKey, mmsi, draught, dimA, dimB, dimC, dimD, name "
//                + " , callSign, imo, rot, sog, cog, longitude, latitude, eventTime, ingestTime, sourceIp, destIp "
//                + " , typeId, countryId, dataSourceName, dataVendor "
//                + "   FROM %s "
//                + "   WHERE "
//                + " ) t ON r.uuidKey = t.uuidKey LIMIT :limit OFFSET 0 ";
//        String sqlFinal = String.format(sql, "vsat_ais", "vsat_ais") + " UNION ALL " + String.format(sql, "ais_data", "ais_data");
//        System.out.println("sqlFinal: " + sqlFinal);
//    }
//    public static void main(String[] args) {
//        
//        List<String> tileNumbers = new ArrayList<>();
//        tileNumbers.add("2.2,110.123450###88.5,220.123456");
//        tileNumbers.add("5.2,90.123450###15.5,201.123456");
//        tileNumbers.add("1.2,70.123450###3.5,190.123456");
//        
//        if( !tileNumbers.isEmpty() ) {
//
//            DecimalFormat df = new DecimalFormat("#.000000");
//
//            List<Float> latitudeOriginLst = new ArrayList<>();
//            List<Float> latitudeCornerLst = new ArrayList<>();
//            List<Float> longitudeOriginLst = new ArrayList<>();
//            List<Float> longitudeCornerLst = new ArrayList<>();
//
//            for( String s : tileNumbers ) {
//                String[] arrTmp = s.split("###");
//                String[] originCoordinate = arrTmp[0].split(",");
//                String[] cornerCoordinate = arrTmp[1].split(",");
//
//                latitudeOriginLst.add(Float.parseFloat(originCoordinate[0]));
//                latitudeCornerLst.add(Float.parseFloat(cornerCoordinate[0]));
//                longitudeOriginLst.add(Float.parseFloat(originCoordinate[1]));
//                longitudeCornerLst.add(Float.parseFloat(cornerCoordinate[1]));
//            }
//
//            Float latitudeOriginMin = Collections.min(latitudeOriginLst);
//            Float longitudeOriginMin = Collections.min(longitudeOriginLst);
//            Float latitudeOriginMax = Collections.max(latitudeCornerLst);
//            Float longitudeOriginMax = Collections.max(longitudeCornerLst);
//
//            if( latitudeOriginMin != null && latitudeOriginMax != null && longitudeOriginMin != null && longitudeOriginMax != null ) {
//                String ss = " AND ( latitude BETWEEN " + df.format(latitudeOriginMin) + " AND " + df.format(latitudeOriginMax) +
//                                 " AND longitude BETWEEN " + df.format(longitudeOriginMin) + " AND " + df.format(longitudeOriginMax) + " ) ";
//                System.out.println("val -> " + ss);
//            } else
//                System.out.println("ERROR");
//        }
//    }
    private SatelliteImageDataFilterDTO buildSatelliteImageDataFilterRequest(Map<String, Object> bodyParam) {
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
            if (tileNumberLst != null && !tileNumberLst.isEmpty()) {
                for (String tileNumberList : tileNumberLst) {
                    tileNumberResult.add("T" + tileNumberList);
                }
            }
            String dataVendor = (String) bodyParam.getOrDefault("dataVendor", "");
            String satelliteName = (String) bodyParam.getOrDefault("satelliteName", "");
            String coordinates = (String) bodyParam.getOrDefault("coordinates", "");
            String captureTime = (String) bodyParam.getOrDefault("captureTime", "");

            SatelliteImageDataFilterDTO satelliteImageDataFilterDTO = SatelliteImageDataFilterDTO.builder()
                    .page(page)
                    .size(size)
                    .sort(sort)
                    .term(term)
                    .fromTime(fromTime)
                    .toTime(toTime)
                    .dataVendorLst(dataVendorLst)
                    .tileNumberLst(tileNumberResult)
                    .dataVendor(dataVendor)
                    .satelliteName(satelliteName)
                    .coordinates(coordinates)
                    .captureTime(captureTime)
                    .build();

            return satelliteImageDataFilterDTO;
        } catch (Exception e) {
            LOGGER.error("ex: ", e);
        }

        return null;
    }

    private ConvertAndFetchVideoRequestDTO buildConvertAndFetchVideoRequest(Map<String, Object> bodyParam) {
        String filePath = (String) bodyParam.getOrDefault("filePath", "");
        String targetExtension = (String) bodyParam.getOrDefault("targetExtension", "");

        ConvertAndFetchVideoRequestDTO convertAndFetchVideoRequestDTO = ConvertAndFetchVideoRequestDTO.builder()
                .filePath(filePath)
                .targetExtension(targetExtension)
                .build();
        return convertAndFetchVideoRequestDTO;
    }

    private VsatMediaRelationFilterDTO buildVsatMediaRelationFilterRequest(Map<String, Object> bodyParam) {
        ObjectMapper mapper = new ObjectMapper();

        Integer page = bodyParam.get("page") != null ? (Integer) bodyParam.get("page") : 0;
        Integer size = bodyParam.get("size") != null ? (Integer) bodyParam.get("size") : 20;
        String sort = (String) bodyParam.getOrDefault("sort", "");
        String term = (String) bodyParam.getOrDefault("term", "");
        String fromTime = (String) bodyParam.get("fromTime");
        String toTime = (String) bodyParam.get("toTime");
        List<Integer> dataSourceIds = bodyParam.get("dataSourceIds") != null ? (List<Integer>) bodyParam.get("dataSourceIds") : null;
        List<Integer> mediaTypeIds = bodyParam.get("mediaTypeIds") != null ? (List<Integer>) bodyParam.get("mediaTypeIds") : null;
        List<Integer> processStatusLst = bodyParam.get("processStatusLst") != null ? (List<Integer>) bodyParam.get("processStatusLst") : null;

        List<AdvanceFilterDTO> filterLst = null;
        if (bodyParam.get("filter") != null) {
            filterLst = mapper.convertValue(bodyParam.get("filter"), new TypeReference<List<AdvanceFilterDTO>>() {
            });
        }

        // filter theo cột
        String dataVendor = (String) bodyParam.get("dataVendor");
        String dataSourceName = (String) bodyParam.get("dataSourceName");
        String sourceIp = (String) bodyParam.get("sourceIp");
        String sourceName = (String) bodyParam.get("sourceName");
        String destIp = (String) bodyParam.get("destIp");
        String destName = (String) bodyParam.get("destName");
        String mediaTypeName = (String) bodyParam.get("mediaTypeName");
        String fileType = (String) bodyParam.get("fileType");
        BigInteger fileSize = (bodyParam.get("fileSize") != "" && bodyParam.get("fileSize") != null) ? BigInteger.valueOf(((Number) bodyParam.get("fileSize")).intValue()) : null;
        Integer direction = (bodyParam.get("direction") != "") && (bodyParam.get("direction") != null) ? ((Number) bodyParam.getOrDefault("direction", "")).intValue() : null;

        VsatMediaRelationFilterDTO vsatMediaRelationFilterDTO = VsatMediaRelationFilterDTO.builder()
                .page(page)
                .size(size)
                .sort(sort)
                .term(term)
                .fromTime(fromTime)
                .toTime(toTime)
                .dataSourceIds(dataSourceIds)
                .mediaTypeIds(mediaTypeIds)
                .processStatusLst(processStatusLst)
                .filter(filterLst)
                .dataVendor(dataVendor)
                .dataSourceName(dataSourceName)
                .sourceIp(sourceIp)
                .sourceName(sourceName)
                .destIp(destIp)
                .destName(destName)
                .mediaTypeName(mediaTypeName)
                .fileType(fileType)
                .fileSize(fileSize)
                .direction(direction)
                .build();
        return vsatMediaRelationFilterDTO;
    }

    private DetailMediaRelationRequestDTO buildDetailMediaRelationRequestDTO(Map<String, Object> bodyParam) {
        String uuidKeyFrom = (String) bodyParam.getOrDefault("uuidKeyFrom", "");
        String uuidKeyTo = (String) bodyParam.getOrDefault("uuidKeyTo", "");
        Integer partName = bodyParam.get("partName") != null ? (Integer) bodyParam.get("partName") : 0;

        DetailMediaRelationRequestDTO detailMediaRelationRequestDTO = DetailMediaRelationRequestDTO.builder()
                .uuidKeyFrom(uuidKeyFrom)
                .uuidKeyTo(uuidKeyTo)
                .partName(partName)
                .build();

        return detailMediaRelationRequestDTO;
    }

    public ResponseMessage getTripOfShips(Map<String, Object> bodyParam, Map<String, String> headerParam, String requestPath) {
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null)
            return unauthorizedResponse();

        if (bodyParam == null || bodyParam.isEmpty())
            return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));

        ABACResponseDTO abacStatus = authorizeABAC("LIST", dto.getUuid(), requestPath);
        if (abacStatus == null || !abacStatus.getStatus())
            return new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));

        ObjectTripRequest request = buildObjectTripRequest(bodyParam);

        String validationMsg = new ObjectTripValidation().validateObjectTripRequest(request);
        if (validationMsg != null) {
            return new ResponseMessage(HttpStatus.OK.value(), validationMsg, new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
        }

        List<ObjectTripDTO> tripOfShipsMap = null;
        if (request.getMmsiLst() != null) {
            List<CompletableFuture<ObjectTripDTO>> tripOfShips = request.getMmsiLst().stream().map(mmsi -> {
                        return objectTripService.findPositionOfShip(mmsi, request.getFromTime(), request.getToTime(), request.getLimit());
                    })
                    .collect(toList());
            tripOfShipsMap = CompletableFuture.allOf(tripOfShips.toArray(new CompletableFuture[0])).thenApply(v -> {
                return tripOfShips.stream()
                        .map(CompletableFuture::join)
                        .collect(toList());
            }).join();
        }

        if (tripOfShipsMap == null) {
            return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, new MessageContent(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, null));
        }

        try {
            // get name of vessel type from redis
            String key = Constant.REDIS_VESSEL_LST_KEY;

            if ( this.redisTemplate.hasKey(key) ) {
                List<VsatVesselType> vsatVesselTypesFromCaches = (List<VsatVesselType>) this.redisTemplate.opsForList().range(key, 0, Constant.REDIS_VESSEL_LST_FETCH_MAX);
                if (vsatVesselTypesFromCaches != null && !vsatVesselTypesFromCaches.isEmpty())
                    for (ObjectTripDTO object : tripOfShipsMap)
                        for (VsatVesselType vsatVesselType : vsatVesselTypesFromCaches)
                            if (vsatVesselType.getTypeCode().equals(object.getTypeId() + "")) {
                                object.setTypeName(vsatVesselType.getTypeName());
                                break;
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
                if (countriesFromCaches != null && !countriesFromCaches.isEmpty())
                    for (ObjectTripDTO object : tripOfShipsMap)
                        for (Countries countries : countriesFromCaches)
                            if (countries.getCountryId().equals(object.getCountryId())) {
                                object.setCountryName(countries.getName());
                                break;
                            }
            }

        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        }

        try {
            // Thêm id đối tượng metacen
            Map<BigInteger, String> shipMetacenIdMap = getLstMarineVesselId(headerParam, bodyParam);
            if (shipMetacenIdMap != null && !shipMetacenIdMap.isEmpty()) {
                tripOfShipsMap.stream().forEach(tripOfShip -> {
                    tripOfShip.setMappingId(shipMetacenIdMap.getOrDefault(tripOfShip.getMmsi(), null));
                });
            }
        } catch (Exception e) {
            LOGGER.error("e: ", e);
        }

        return new ResponseMessage(new MessageContent(tripOfShipsMap));
    }

    private static ObjectTripRequest buildObjectTripRequest(Map<String, Object> bodyParam) {
        ObjectTripRequest request = ObjectTripRequest.builder()
                .mmsiLst((List<Integer>) bodyParam.getOrDefault("mmsiLst", null))
                .fromTime((String) bodyParam.getOrDefault("fromTime", null))
                .toTime((String) bodyParam.getOrDefault("toTime", null))
                .limit((Integer) bodyParam.getOrDefault("limit", 5000))
                .build();
        return request;
    }

    public Map<BigInteger, String> getLstMarineVesselId(Map<String, String> headerMap, Map<String, Object> bodyMap) {
        bodyMap.put("objectUuid", bodyMap.getOrDefault("uuid", null));

        RequestMessage userRpcRequest = new RequestMessage();
        userRpcRequest.setRequestMethod("POST");
        userRpcRequest.setRequestPath(RabbitMQProperties.SHIP_INFO_METACEN_URL); // /contact/marine-vessel/ships-info
        userRpcRequest.setVersion(ResourcePath.VERSION);
        userRpcRequest.setBodyParam(bodyMap);
        userRpcRequest.setUrlParam(null);
        userRpcRequest.setHeaderParam(headerMap);
        String result = rabbitMQClient.callRpcService(RabbitMQProperties.CONTACT_RPC_EXCHANGE,
                RabbitMQProperties.CONTACT_RPC_QUEUE, RabbitMQProperties.CONTACT_RPC_KEY, userRpcRequest.toJsonString());
        LOGGER.info("checkExistMappingRelation - result: " + result);

        if (result != null) {
            ObjectMapper mapper = new ObjectMapper();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mapper.setDateFormat(df);
            ResponseMessage resultResponse = null;
            try {
                resultResponse = mapper.readValue(result, ResponseMessage.class);
                if (resultResponse != null && resultResponse.getStatus() == HttpStatus.OK.value() && resultResponse.getData() != null) {
                    JsonNode jsonNode = mapper.readTree(result);

                    List<MarineVesselInfoDTO> resultCheckDto = List.of(mapper.readValue(jsonNode.get("data").get("data").toString(), MarineVesselInfoDTO[].class));
                    return resultCheckDto.stream().collect(toMap(MarineVesselInfoDTO::getMmsi, MarineVesselInfoDTO::getId));
                }
                return null;
            } catch (Exception ex) {
                return null;
            }
        } else {
            return null;
        }
    }
}
