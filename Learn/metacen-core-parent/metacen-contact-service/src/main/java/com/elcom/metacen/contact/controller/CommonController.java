package com.elcom.metacen.contact.controller;

import com.elcom.metacen.contact.constant.Constant;
import com.elcom.metacen.contact.model.*;
import com.elcom.metacen.contact.model.dto.*;
import com.elcom.metacen.contact.service.*;
import com.elcom.metacen.contact.validation.ObjectValidation;
import com.elcom.metacen.dto.redis.Countries;
import com.elcom.metacen.dto.redis.VsatDataSource;
import com.elcom.metacen.dto.redis.VsatVesselType;
import com.elcom.metacen.enums.ObjectType;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.ResponseMessage;
import com.elcom.metacen.utils.ObjectMapperUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CommonController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonController.class);

    @Autowired
    private CommonService commonService;

    @Autowired
    private PeopleService peopleService;

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private MarineVesselInfoService marineVesselInfoService;

    @Autowired
    private OtherVehicleService otherVehicleService;

    @Autowired
    private EventService eventService;

    @Autowired
    private InfrastructureService infrastructureService;

    @Autowired
    private OtherObjectService otherObjectService;

    @Autowired
    private AreasService areasService;

    @Autowired
    private AeroService aeroService;

    @Autowired
    private ObjectRelationshipService objectRelationshipService;

    @Autowired
    private EnrichDataService enrichDataService;

    @Autowired
    private MappingDataService mappingDataService;

    @Autowired
    private KeywordDataService keywordDataService;

    public ResponseMessage getListCountry(Map<String, String> headerMap) {
        AuthorizationResponseDTO dto = authenToken(headerMap);
        if (dto == null) {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(),
                    new MessageContent(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), "Bạn chưa đăng nhập"));
        }

        List<Countries> lstCountry = commonService.getListCountries();
        if (lstCountry != null && !lstCountry.isEmpty()) {
            List<CountryDTO> countryDTOLst = ObjectMapperUtils.mapAll(lstCountry, CountryDTO.class);
            return new ResponseMessage(new MessageContent(countryDTOLst));
        } else {
            return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND,
                    new MessageContent(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, null));
        }
    }

    public ResponseMessage getListObjectType(Map<String, String> headerMap) {
        AuthorizationResponseDTO dto = authenToken(headerMap);
        if (dto == null) {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(),
                    new MessageContent(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), "Bạn chưa đăng nhập"));
        }

        List<ObjectTypes> lstObjectTypes = commonService.getListObjectTypes();
        if (lstObjectTypes != null && !lstObjectTypes.isEmpty()) {
            List<ObjectTypesDTO> objectTypesDTOLst = ObjectMapperUtils.mapAll(lstObjectTypes, ObjectTypesDTO.class);
            return new ResponseMessage(new MessageContent(objectTypesDTOLst));
        } else {
            return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND,
                    new MessageContent(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, null));
        }
    }

    public ResponseMessage getListVesselType(Map<String, String> headerMap) {
        AuthorizationResponseDTO dto = authenToken(headerMap);
        if (dto == null)
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), new MessageContent(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), "Bạn chưa đăng nhập"));

        List<VsatVesselType> vsatVesselTypeLst = commonService.getListVesselType();
        if (vsatVesselTypeLst == null || vsatVesselTypeLst.isEmpty())
            return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, new MessageContent(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, null));

        // List<VsatVesselType> vsatVesselTypeLst = ObjectMapperUtils.mapAll(lstVessel, VsatVesselType.class);
        return new ResponseMessage(new MessageContent(vsatVesselTypeLst));
    }

    public ResponseMessage getListVsatDataSource(Map<String, String> headerMap) {
        AuthorizationResponseDTO dto = authenToken(headerMap);
        if (dto == null) {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(),
                    new MessageContent(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), "Bạn chưa đăng nhập"));
        }

        List<VsatDataSource> lstVsatDataSource = commonService.getListVsatDataSource();
        if (lstVsatDataSource != null && !lstVsatDataSource.isEmpty()) {
            List<VsatDataSource> vsatDataSourceLst = ObjectMapperUtils.mapAll(lstVsatDataSource, VsatDataSource.class);
            return new ResponseMessage(new MessageContent(vsatDataSourceLst));
        } else {
            return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND,
                    new MessageContent(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, null));
        }
    }

    public ResponseMessage filterObject(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
        LOGGER.info("Filter object with request >>> {}", bodyParam);

        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }

        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "LIST", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            if (bodyParam == null || bodyParam.isEmpty()) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            }
            ObjectCriteria objectCriteria = buildObjectCriteria(bodyParam);
            String validationMsg = new ObjectValidation().validateObjectFilter(objectCriteria);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }

            Page<ObjectGeneralInfoDTO> pagedResult = commonService.filterObject(objectCriteria);
            return new ResponseMessage(new MessageContent(pagedResult.getContent(), pagedResult.getTotalElements()));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage checkDeleteObject(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "DELETE", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            return checkRelationShipAndMention(bodyParam, headerParam);
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage deleteObject(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "DELETE", dto.getUuid(), requestPath);

        if (abacStatus != null && abacStatus.getStatus()) {
            String object = (String) bodyParam.get("objectType");
            String uuid = (String) bodyParam.get("uuid");
            Boolean mapping = mappingDataService.isExistMappingRelation(headerParam, bodyParam);
            Boolean mention = enrichDataService.isExistObjectAnalyzed(headerParam, bodyParam);
            if (!mapping && !mention) {
                ResponseMessage responseMessage = checkObject(bodyParam, headerParam);
                return responseMessage;
            } else {
                if (mention && !mapping) {
                    return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Đối tượng đang được đề xuất",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "Đối tượng đang được đề xuất", null));
                } else if (!mention && mapping) {
                    return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Đối tượng đang có mối liên kết ánh xạ",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "Đối tượng đang có mối liên kết ánh xạ", null));
                } else {
                    return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Đối tượng đang có mối liên kết ánh xạ và được đề xuất",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "Đối tượng đang có mối liên kết ánh xạ và được đề xuất", null));
                }
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    private ResponseMessage checkRelationShipAndMention(Map<String, Object> bodyParam, Map<String, String> headerParam) {
        //Check id
        AuthorizationResponseDTO dto = authenToken(headerParam);

        String object = (String) bodyParam.get("objectType");
        String uuid = (String) bodyParam.get("uuid");
        Boolean mapping = mappingDataService.isExistMappingRelation(headerParam, bodyParam);
        Boolean mention = enrichDataService.isExistObjectAnalyzed(headerParam, bodyParam);
        if (!mapping && !mention) {
            List<ObjectRelationship> objectRelationships = objectRelationshipService.getRelationshipsByDestObjectId(object, uuid);
            if (objectRelationships.isEmpty()) {
                return new ResponseMessage(HttpStatus.OK.value(), "success",
                        new MessageContent(HttpStatus.OK.value(), "success", null));
            } else {
                return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Đối tượng đang có mối quan hệ",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Đối tượng đang có mối quan hệ", null));
            }
        } else {
            if (mention && !mapping) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Đối tượng đang được đề xuất",
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), "Đối tượng đang được đề xuất", null));
            } else if (!mention && mapping) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Đối tượng đang có mối liên kết ánh xạ",
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), "Đối tượng đang có mối liên kết ánh xạ", null));
            } else {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Đối tượng đang có mối liên kết ánh xạ và được đề xuất",
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), "Đối tượng đang có mối liên kết ánh xạ và được đề xuất", null));
            }
        }
    }

    private ResponseMessage checkObject(Map<String, Object> bodyParam, Map<String, String> headerParam) {
        AuthorizationResponseDTO dto = authenToken(headerParam);
        String object = (String) bodyParam.get("objectType");
        String uuid = (String) bodyParam.get("uuid");
        if (object.equalsIgnoreCase(String.valueOf(ObjectType.PEOPLE))) {
            People people = peopleService.findByUuid(uuid);
            if (people == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "People không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "People không tồn tại", null));
            }
            this.peopleService.delete(people, dto.getUserName());
            // delete relationship by source object id
            objectRelationshipService.deleteObjectRelationship(people.getUuid());
            // delete relationship by dest object id
            objectRelationshipService.deleteObjectRelationshipByDestObjectId(people.getUuid());
            // call topo
            Map<String, Object> bodyObject = new HashMap<>();
            bodyObject.put("objectUuid", people.getUuid());
            callLinkObjectDeleteNode(bodyObject);
            // delete in keywordData
            List<KeywordData> keywordData = keywordDataService.findByRefId(uuid);
            this.keywordDataService.delete(keywordData);
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Xóa dữ liệu thành công", null));
        } else if (object.equalsIgnoreCase(String.valueOf(ObjectType.AIRPLANE))) {
            AeroAirplaneInfo aeroAirplaneInfo = aeroService.findByUuid(uuid);
            if (aeroAirplaneInfo == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Máy bay không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Máy bay không tồn tại", null));
            }
            this.aeroService.delete(aeroAirplaneInfo, dto.getUserName());
            // delete relationship by source object id
            objectRelationshipService.deleteObjectRelationship(aeroAirplaneInfo.getUuid());
            // delete relationship by dest object id
            objectRelationshipService.deleteObjectRelationshipByDestObjectId(aeroAirplaneInfo.getUuid());
            // call topo
            Map<String, Object> bodyObject = new HashMap<>();
            bodyObject.put("objectUuid", aeroAirplaneInfo.getUuid());
            callLinkObjectDeleteNode(bodyObject);
            // delete in keywordData
            List<KeywordData> keywordData = keywordDataService.findByRefId(uuid);
            this.keywordDataService.delete(keywordData);
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Xóa dữ liệu thành công", null));
        } else if (object.equalsIgnoreCase(String.valueOf(ObjectType.ORGANISATION))) {
            Organisation organisation = organisationService.findById(uuid);
            if (organisation == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Tổ chức không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Tổ chức không tồn tại", null));
            }
            this.organisationService.delete(organisation, dto.getUserName());
            // delete relationship by source object id
            objectRelationshipService.deleteObjectRelationship(organisation.getUuid());
            // delete relationship by dest object id
            objectRelationshipService.deleteObjectRelationshipByDestObjectId(organisation.getUuid());
            // call topo
            Map<String, Object> bodyObject = new HashMap<>();
            bodyObject.put("objectUuid", organisation.getUuid());
            callLinkObjectDeleteNode(bodyObject);
            // delete in keywordData
            List<KeywordData> keywordData = keywordDataService.findByRefId(uuid);
            this.keywordDataService.delete(keywordData);
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Xóa dữ liệu thành công", null));
        } else if (object.equalsIgnoreCase(String.valueOf(ObjectType.VESSEL))) {
            MarineVesselInfo marineVesselInfo = marineVesselInfoService
                    .findById(uuid);
            if (marineVesselInfo == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Phương tiện không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Phương tiện không tồn tại", null));
            }
            this.marineVesselInfoService.delete(marineVesselInfo, dto.getUserName());
            // delete relationship by source object id
            objectRelationshipService.deleteObjectRelationship(marineVesselInfo.getUuid());
            // delete relationship by dest object id
            objectRelationshipService.deleteObjectRelationshipByDestObjectId(marineVesselInfo.getUuid());
            // call topo
            Map<String, Object> bodyObject = new HashMap<>();
            bodyObject.put("objectUuid", marineVesselInfo.getUuid());
            callLinkObjectDeleteNode(bodyObject);
            // delete in keywordData
            List<KeywordData> keywordData = keywordDataService.findByRefId(uuid);
            this.keywordDataService.delete(keywordData);
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Xóa dữ liệu thành công", null));
        } else if (object.equalsIgnoreCase(String.valueOf(ObjectType.OTHER_VEHICLE))) {
            OtherVehicle otherVehicle = otherVehicleService.findByUuid(uuid);
            if (otherVehicle == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Phương tiện không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Phương tiện không tồn tại", null));
            }
            otherVehicleService.delete(otherVehicle, dto.getUserName());
            // delete relationship by source object id
            objectRelationshipService.deleteObjectRelationship(otherVehicle.getUuid());
            // delete relationship by dest object id
            objectRelationshipService.deleteObjectRelationshipByDestObjectId(otherVehicle.getUuid());
            // call topo
            Map<String, Object> bodyObject = new HashMap<>();
            bodyObject.put("objectUuid", otherVehicle.getUuid());
            callLinkObjectDeleteNode(bodyObject);
            // delete in keywordData
            List<KeywordData> keywordData = keywordDataService.findByRefId(uuid);
            this.keywordDataService.delete(keywordData);
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Xóa dữ liệu thành công", null));
        } else if (object.equalsIgnoreCase(String.valueOf(ObjectType.EVENT))) {
            Event event = eventService.findById(uuid);
            if (event == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Sự kiện không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Sự kiện không tồn tại", null));
            }
            this.eventService.deleteEvent(event, dto.getUserName());
            // delete relationship by source object id
            objectRelationshipService.deleteObjectRelationship(event.getUuid());
            // delete relationship by dest object id
            objectRelationshipService.deleteObjectRelationshipByDestObjectId(event.getUuid());
            // call topo
            Map<String, Object> bodyObject = new HashMap<>();
            bodyObject.put("objectUuid", event.getUuid());
            callLinkObjectDeleteNode(bodyObject);
            // delete in keywordData
            List<KeywordData> keywordData = keywordDataService.findByRefId(uuid);
            this.keywordDataService.delete(keywordData);
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Xóa dữ liệu thành công", null));
        } else if (object.equalsIgnoreCase(String.valueOf(ObjectType.AREA))) {
            Areas areas = areasService.findById(uuid);
            if (areas == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Khu vực không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Khu vực không tồn tại", null));
            }
            this.areasService.deleteAreas(areas, dto.getUserName());
            // delete relationship by source object id
            objectRelationshipService.deleteObjectRelationship(areas.getUuid());
            // delete relationship by dest object id
            objectRelationshipService.deleteObjectRelationshipByDestObjectId(areas.getUuid());
            // call topo
            Map<String, Object> bodyObject = new HashMap<>();
            bodyObject.put("objectUuid", areas.getUuid());
            callLinkObjectDeleteNode(bodyObject);
            // delete in keywordData
            List<KeywordData> keywordData = keywordDataService.findByRefId(uuid);
            this.keywordDataService.delete(keywordData);
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Xóa dữ liệu thành công", null));
        } else if (object.equalsIgnoreCase(String.valueOf(ObjectType.INFRASTRUCTURE))) {
            Infrastructure infrastructure = infrastructureService.findByUuid(uuid);
            if (infrastructure == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Cơ sở hạ tầng không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Cơ sở hạ tầng không tồn tại", null));
            }
            infrastructureService.delete(infrastructure, dto.getUserName());
            // delete relationship by source object id
            objectRelationshipService.deleteObjectRelationship(infrastructure.getUuid());
            // delete relationship by dest object id
            objectRelationshipService.deleteObjectRelationshipByDestObjectId(infrastructure.getUuid());
            //call topo
            Map<String, Object> bodyObject = new HashMap<>();
            bodyObject.put("objectUuid", infrastructure.getUuid());
            callLinkObjectDeleteNode(bodyObject);
            // delete in keywordData
            List<KeywordData> keywordData = keywordDataService.findByRefId(uuid);
            this.keywordDataService.delete(keywordData);
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Xóa dữ liệu thành công", null));
        } else if (object.equalsIgnoreCase(String.valueOf(ObjectType.OTHER_OBJECT))) {
            OtherObject otherObject = otherObjectService.findByUuid(uuid);
            if (otherObject == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Đối tượng không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Đối tượng không tồn tại", null));
            }
            otherObjectService.delete(otherObject, dto.getUserName());
            // delete relationship by source object id
            objectRelationshipService.deleteObjectRelationship(otherObject.getUuid());
            // delete relationship by dest object id
            objectRelationshipService.deleteObjectRelationshipByDestObjectId(otherObject.getUuid());
            // call topo
            Map<String, Object> bodyObject = new HashMap<>();
            bodyObject.put("objectUuid", otherObject.getUuid());
            callLinkObjectDeleteNode(bodyObject);
            // delete in keywordData
            List<KeywordData> keywordData = keywordDataService.findByRefId(uuid);
            this.keywordDataService.delete(keywordData);
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Xóa dữ liệu thành công", null));
        }
        return null;
    }

    private ObjectCriteria buildObjectCriteria(Map<String, Object> bodyParam) {
        Integer page = bodyParam.get("page") != null ? (Integer) bodyParam.get("page") : 0;
        Integer size = bodyParam.get("size") != null ? (Integer) bodyParam.get("size") : 20;
        String term = (String) bodyParam.getOrDefault("term", "");
        List<String> keyId = bodyParam.get("keyId") != null ? (List<String>) bodyParam.get("keyId") : null;
        List<String> objectTypeLst = bodyParam.get("objectTypeLst") != null ? (List<String>) bodyParam.get("objectTypeLst") : null;
        List<Integer> countryIds = bodyParam.get("countryIds") != null ? (List<Integer>) bodyParam.get("countryIds") : null;
        List<String> sideIds = bodyParam.get("sideIds") != null ? (List<String>) bodyParam.get("sideIds") : null;
        List<String> keywordIds = bodyParam.get("keywordIds") != null ? (List<String>) bodyParam.get("keywordIds") : null;

        ObjectCriteria objectCriteria = ObjectCriteria.builder()
                .page(page)
                .size(size)
                .keyId(keyId)
                .term(term)
                .objectTypeLst(objectTypeLst)
                .countryIds(countryIds)
                .sideIds(sideIds)
                .keywordIds(keywordIds)
                .build();
        return objectCriteria;
    }
}
