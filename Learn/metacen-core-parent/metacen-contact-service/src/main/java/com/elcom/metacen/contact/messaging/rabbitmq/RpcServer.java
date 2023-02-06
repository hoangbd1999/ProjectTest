package com.elcom.metacen.contact.messaging.rabbitmq;

import com.elcom.metacen.constant.ResourcePath;
import com.elcom.metacen.contact.controller.*;
import com.elcom.metacen.contact.exception.ValidationException;
import com.elcom.metacen.message.RequestMessage;
import com.elcom.metacen.message.ResponseMessage;
import com.elcom.metacen.utils.StringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * @author Admin
 */
public class RpcServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

    @Autowired
    private CommonController commonController;

    @Autowired
    private AeroController aeroController;

    @Autowired
    private ObjectTypesController objectTypesController;

    @Autowired
    private MarineVesselController marineVesselController;

    @Autowired
    private OrganisationController organisationController;

    @Autowired
    private CommentsController commentsController;

    @Autowired
    private GroupController groupController;

    @Autowired
    private SideController sideController;

    @Autowired
    private KeywordController keywordController;

    @Autowired
    private PeopleController peopleController;

    @Autowired
    private DataCollectorConfigController dataCollectorConfigController;

    @Autowired
    private AreasController areasController;

    @Autowired
    private EventController eventController;

    @Autowired
    private InfrastructureController infrastructureController;

    @Autowired
    private OtherVehicleController otherVehicleController;

    @Autowired
    private OtherObjectController otherObjectController;

    @Autowired
    private TileMapController tileMapController;

    @Autowired
    private ObjectGroupController objectGroupController;

    @Autowired
    private ObjectGroupDefineController objectGroupDefineController;

    @RabbitListener(queues = "${contact.rpc.queue}")
    public String processService(String json) throws ValidationException {
        try {
            LOGGER.info(" [-->] Server received request for " + json);

            ObjectMapper mapper = new ObjectMapper();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mapper.setDateFormat(df);
            RequestMessage request = mapper.readValue(json, RequestMessage.class);

            //Process here
            ResponseMessage response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null);
            if (request != null) {
                String requestPath = request.getRequestPath().replace(request.getVersion() != null
                        ? request.getVersion() : ResourcePath.VERSION, "");
                String urlParam = request.getUrlParam();
                String pathParam = request.getPathParam();
                Map<String, Object> bodyParam = request.getBodyParam();
                Map<String, String> headerParam = request.getHeaderParam();
                switch (request.getRequestMethod()) {
                    case "GET":
                        if ("/contact/country".equalsIgnoreCase(requestPath)) {
                            response = commonController.getListCountry(headerParam);
                        } else if ("/contact/object-type".equalsIgnoreCase(requestPath)) {
                            if (StringUtils.isNotBlank(pathParam)) {
                                response = objectTypesController.getObjectTypeById(requestPath, headerParam, pathParam);
                            } else {
                                response = commonController.getListObjectType(headerParam);
                            }
                        } else if ("/contact/vessel-type".equalsIgnoreCase(requestPath)) {
                            response = commonController.getListVesselType(headerParam);
                        } else if ("/contact/vsat-data-source".equalsIgnoreCase(requestPath)) {
                            response = commonController.getListVsatDataSource(headerParam);
                        } else if ("/contact/marine-vessel-info".equalsIgnoreCase(requestPath)) {
                            response = marineVesselController.getMarineVesselInfo(requestPath, headerParam, pathParam);
                        } else if ("/contact/aero".equalsIgnoreCase(requestPath)) {
                            response = aeroController.getAeroById(requestPath, headerParam, pathParam);
                        }
                        // comment
                        else if ("/contact/comment".equalsIgnoreCase(requestPath)) {
                            if (StringUtils.isNotBlank(pathParam)) {
                                response = commentsController.getCommentById(requestPath, headerParam, pathParam);
                            } else {
                                response = commentsController.getCommentList(requestPath, headerParam, urlParam);
                            }
                        }
                        // group
                        else if ("/contact/group".equalsIgnoreCase(requestPath)) {
                            if (StringUtils.isNotBlank(pathParam)) {
                                response = groupController.getGroupById(requestPath, headerParam, pathParam);
                            } else {
                                response = groupController.getGroupList(requestPath, headerParam, urlParam);
                            }
                        }
                        // side
                        else if ("/contact/side".equalsIgnoreCase(requestPath)) {
                            if (StringUtils.isNotBlank(pathParam)) {
                                response = sideController.getSideById(requestPath, headerParam, pathParam);
                            } else {
                                response = sideController.getSideList(requestPath, headerParam, urlParam);
                            }
                        }
                        // infrastructure
                        else if ("/contact/infrastructure".equalsIgnoreCase(requestPath)) {
                            response = infrastructureController.getInfrastructureById(requestPath, headerParam, pathParam);
                        }
                        // other-vehicle
                        else if ("/contact/other-vehicle".equalsIgnoreCase(requestPath)) {
                            response = otherVehicleController.getOtherVehicleById(requestPath, headerParam, pathParam);
                        }
                        // other-object
                        else if ("/contact/other-object".equalsIgnoreCase(requestPath)) {
                            response = otherObjectController.getOtherObjectById(requestPath, headerParam, pathParam);
                        }
                        // people
                        else if ("/contact/people".equalsIgnoreCase(requestPath)) {
                            response = peopleController.getPeopleById(requestPath, headerParam, pathParam);
                        }
                        // organisation
                        else if ("/contact/organisation".equalsIgnoreCase(requestPath)) {
                            if (StringUtils.isNotBlank(pathParam)) {
                                response = organisationController.findById(headerParam, pathParam, requestPath);
                            }
                        }
                        // data-collector-config
                        else if ("/contact/data-collector-config".equalsIgnoreCase(requestPath)) {
                            if (StringUtils.isNotBlank(urlParam)) {
                                response = dataCollectorConfigController.findById(headerParam, urlParam, requestPath);
                            } else {
                                response = dataCollectorConfigController.findAll(headerParam, requestPath);
                            }
                        }
                        // areas
                        else if ("/contact/areas".equalsIgnoreCase(requestPath)) {
                            if (StringUtils.isNotBlank(pathParam)) {
                                response = areasController.getAreasById(headerParam, pathParam, requestPath);
                            }
                        }
                        // event
                        else if ("/contact/event".equalsIgnoreCase(requestPath)) {
                            if (StringUtils.isNotBlank(pathParam)) {
                                response = eventController.getEventById(headerParam, requestPath, pathParam);
                            }
                        }
                        // keyword
                        else if ("/contact/keyword".equalsIgnoreCase(requestPath)) {
                            if (StringUtils.isNotBlank(pathParam)) {
                                response = keywordController.findById(headerParam, pathParam, requestPath);
                            } else {
                                response = keywordController.findAll(headerParam, urlParam, requestPath);
                            }
                        }
                        // get list keyword internal
                        else if ("/contact/keyword/internal".equalsIgnoreCase(requestPath)) {
                                response = keywordController.findAllKeyword();
                        }
                        // keyword-data media
                        else if ("/contact/keyword-data/media".equalsIgnoreCase(requestPath)) {
                                response = keywordController.getKeywordDataMedia(requestPath, headerParam, urlParam);
                        }
                        // find config uuid (check object group exist)
                        else if ("/contact/object-group/check-exist/internal".equalsIgnoreCase(requestPath)) {
                                response = objectGroupController.checkExistObjectGroup(pathParam);
                        }
                        break;
                    case "POST":
                        if ("/contact/object-type".equalsIgnoreCase(requestPath)) {
                            response = objectTypesController.insertObjectType(requestPath, bodyParam, headerParam);
                        } else if ("/contact/aero".equalsIgnoreCase(requestPath)) {
                            response = aeroController.insertAero(requestPath, bodyParam, headerParam);
                        } else if ("/contact/marine-vessel-info".equalsIgnoreCase(requestPath)) {
                            response = marineVesselController.insertMarineVesselInfo(requestPath, bodyParam, headerParam);
                        } else if ("/contact/organisation".equalsIgnoreCase(requestPath)) {
                            response = organisationController.create(headerParam, bodyParam, requestPath);
                        } else if ("/contact/comment".equalsIgnoreCase(requestPath)) {
                            response = commentsController.insertComment(requestPath, bodyParam, headerParam);
                        } else if ("/contact/group".equalsIgnoreCase(requestPath)) {
                            response = groupController.insertGroup(headerParam, bodyParam, requestPath);
                        } else if ("/contact/side".equalsIgnoreCase(requestPath)) {
                            response = sideController.insertSide(headerParam, bodyParam, requestPath);
                        } else if ("/contact/people".equalsIgnoreCase(requestPath)) {
                            response = peopleController.insertPeople(headerParam, bodyParam, requestPath);
                        } else if ("/contact/infrastructure".equalsIgnoreCase(requestPath)) {
                            response = infrastructureController.insertInfrastructure(headerParam, bodyParam, requestPath);
                        } else if ("/contact/other-vehicle".equalsIgnoreCase(requestPath)) {
                            response = otherVehicleController.insertOtherVehicle(headerParam, bodyParam, requestPath);
                        } else if ("/contact/other-object".equalsIgnoreCase(requestPath)) {
                            response = otherObjectController.insertOtherObject(headerParam, bodyParam, requestPath);
                        } else if ("/contact/areas".equalsIgnoreCase(requestPath)) {
                            response = areasController.insertAreas(headerParam, bodyParam, requestPath);
                        } else if ("/contact/event".equalsIgnoreCase(requestPath)) {
                            response = eventController.insertEvent(headerParam, bodyParam, requestPath);
                        } else if ("/contact/keyword".equalsIgnoreCase(requestPath)) {
                            response = keywordController.create(headerParam, bodyParam, requestPath);
                        } else if ("/contact/keyword/grant".equalsIgnoreCase(requestPath)) {
                            response = keywordController.createKeywordGrant(headerParam, bodyParam, requestPath);
                        } else if ("/contact/keyword-data".equalsIgnoreCase(requestPath)) {
                            response = keywordController.createKeywordData(headerParam, bodyParam, requestPath);
                        } else if ("/contact/keyword-data/internal".equalsIgnoreCase(requestPath)) {
                            response = keywordController.insertKeywordDataInternal(bodyParam);
                        }

                        // filter organisation
                        else if ("/contact/organisation/filter".equalsIgnoreCase(requestPath)) {
                            response = organisationController.filterOrganisation(headerParam, bodyParam, requestPath);
                        }
                        // filter people
                        else if ("/contact/people/filter".equalsIgnoreCase(requestPath)) {
                            response = peopleController.filterPeople(headerParam, bodyParam, requestPath);
                        }
                        // filter event
                        else if ("/contact/event/filter".equalsIgnoreCase(requestPath)) {
                            response = eventController.filterEvent(headerParam, bodyParam, requestPath);
                        }
                        // filter areas
                        else if ("/contact/areas/filter".equalsIgnoreCase(requestPath)) {
                            response = areasController.filterAreas(headerParam, bodyParam, requestPath);
                        }
                        // filter marine-vessel
                        else if ("/contact/marine-vessel-info/filter".equalsIgnoreCase(requestPath)) {
                            response = marineVesselController.filterMarineVesselInfo(headerParam, bodyParam, requestPath);
                        }
                        // filter infrastructure
                        else if ("/contact/infrastructure/filter".equalsIgnoreCase(requestPath)) {
                            response = infrastructureController.filterInfrastructure(headerParam, bodyParam, requestPath);
                        }
                        // filter other-vehicle
                        else if ("/contact/other-vehicle/filter".equalsIgnoreCase(requestPath)) {
                            response = otherVehicleController.filterOtherVehicle(headerParam, bodyParam, requestPath);
                        }
                        // filter other-vehicle
                        else if ("/contact/other-object/filter".equalsIgnoreCase(requestPath)) {
                            response = otherObjectController.filterOtherObject(headerParam, bodyParam, requestPath);
                        }
                        // filter object
                        else if ("/contact/object/filter".equalsIgnoreCase(requestPath)) {
                            response = commonController.filterObject(headerParam, bodyParam, requestPath);
                        }
                        // filter aero
                        else if ("/contact/aero/filter".equalsIgnoreCase(requestPath)) {
                            response = aeroController.filterAero(headerParam, bodyParam, requestPath);
                        }
                        // filter tile-Map
                        else if ("/contact/tile-map/filter".equalsIgnoreCase(requestPath)) {
                            response = tileMapController.filterTileMap(headerParam, bodyParam);
                        }
                        // Get the object list of keywords
                        else if ("/contact/keyword-data/object".equalsIgnoreCase(requestPath)) {
                            response = keywordController.getKeywordDataObject(headerParam, bodyParam, requestPath);
                        } else if ("/contact/marine-vessel-info/internal".equalsIgnoreCase(requestPath)) {
                            response = marineVesselController.insertMarineVesselInfoInternal(requestPath, bodyParam, headerParam);
                        }
                        // filter object group unconfirmed
                        else if ("/contact/object-group/unconfirmed/filter".equalsIgnoreCase(requestPath))
                            response = objectGroupController.filterObjectGroupUnconfirmed(headerParam, bodyParam, requestPath);
                        // filter object group confirmed
                        else if ("/contact/object-group/confirmed/filter".equalsIgnoreCase(requestPath))
                            response = objectGroupController.filterObjectGroupConfirmed(headerParam, bodyParam, requestPath);
                        // filter object group define
                        else if ("/contact/object-group-define/filter".equalsIgnoreCase(requestPath))
                            response = objectGroupDefineController.filterObjectGroupDefine(headerParam, bodyParam, requestPath);
                        // create object group mapping
                        else if ("/contact/object-group/object-mapping".equalsIgnoreCase(requestPath))
                            response = objectGroupController.insertObjectMapping(headerParam, bodyParam, requestPath);
                        // create object group define
                        else if ("/contact/object-group-define".equalsIgnoreCase(requestPath))
                            response = objectGroupDefineController.insert(headerParam, bodyParam, requestPath);
                        // change object main
                        else if ("/contact/object-group-define/status-main-object-change".equalsIgnoreCase(requestPath))
                            response = objectGroupDefineController.statusMainObjectChange(headerParam, bodyParam, requestPath);

                        // get list metacen id by mmsi marine-vessel
                        else if ("/contact/marine-vessel/ships-info".equalsIgnoreCase(requestPath))
                            response = marineVesselController.getObjectMappingByMmsiLst(headerParam, bodyParam, requestPath);
                        break;
                    case "PUT":
                        if ("/contact/object-type".equalsIgnoreCase(requestPath)) {
                            response = objectTypesController.updateObjectType(requestPath, bodyParam, headerParam, pathParam);
                        } else if ("/contact/aero".equalsIgnoreCase(requestPath)) {
                            response = aeroController.updateAero(requestPath, bodyParam, headerParam, pathParam);
                        } else if ("/contact/marine-vessel-info".equalsIgnoreCase(requestPath)) {
                            response = marineVesselController.updateMarineVesselInfo(requestPath, bodyParam, headerParam, pathParam);
                        } else if ("/contact/organisation".equalsIgnoreCase(requestPath)) {
                            response = organisationController.update(headerParam, bodyParam, pathParam, requestPath);
                        } else if ("/contact/comment".equalsIgnoreCase(requestPath)) {
                            response = commentsController.updateComment(requestPath, bodyParam, headerParam, pathParam);
                        } else if ("/contact/group".equalsIgnoreCase(requestPath)) {
                            response = groupController.updateGroup(headerParam, bodyParam, pathParam, requestPath);
                        } else if ("/contact/side".equalsIgnoreCase(requestPath)) {
                            response = sideController.updateSide(headerParam, bodyParam, pathParam, requestPath);
                        } else if ("/contact/people".equalsIgnoreCase(requestPath)) {
                            response = peopleController.updatePeople(headerParam, bodyParam, pathParam, requestPath);
                        } else if ("/contact/infrastructure".equalsIgnoreCase(requestPath)) {
                            response = infrastructureController.updateInfrastructure(headerParam, bodyParam, pathParam, requestPath);
                        } else if ("/contact/other-vehicle".equalsIgnoreCase(requestPath)) {
                            response = otherVehicleController.updateOtherVehicle(headerParam, bodyParam, pathParam, requestPath);
                        } else if ("/contact/other-object".equalsIgnoreCase(requestPath)) {
                            response = otherObjectController.updateOtherObject(headerParam, bodyParam, pathParam, requestPath);
                        } else if ("/contact/data-collector-config-value".equalsIgnoreCase(requestPath)) {
                            response = dataCollectorConfigController.updateConfigValue(requestPath, bodyParam, headerParam, urlParam);
                        } else if ("/contact/data-collector-is-running".equalsIgnoreCase(requestPath)) {
                            response = dataCollectorConfigController.updateIsRunningProgress(requestPath, bodyParam, headerParam, urlParam);
                        } else if ("/contact/areas".equalsIgnoreCase(requestPath)) {
                            response = areasController.updateAreas(headerParam, bodyParam, pathParam, requestPath);
                        } else if ("/contact/event".equalsIgnoreCase(requestPath)) {
                            response = eventController.updateEvent(headerParam, bodyParam, pathParam, requestPath);
                        } else if ("/contact/keyword".equalsIgnoreCase(requestPath)) {
                            response = keywordController.update(headerParam, bodyParam, pathParam, requestPath);
                        }
                        // update and confirm object group and object group mapping
                        else if ("/contact/object-group".equalsIgnoreCase(requestPath))
                            response = objectGroupController.update(headerParam, bodyParam, pathParam, requestPath);
                        // update name object group
                        else if ("/contact/object-group/name".equalsIgnoreCase(requestPath))
                            response = objectGroupController.updateObjectGroupName(headerParam, bodyParam, pathParam, requestPath);
                        // update Object Mapping
                        else if ("/contact/object-group/object-mapping".equalsIgnoreCase(requestPath))
                            response = objectGroupController.updateObjectMapping(headerParam, bodyParam, requestPath);
                        // update Object group define
                        else if ("/contact/object-group-define".equalsIgnoreCase(requestPath))
                            response = objectGroupDefineController.update(headerParam, bodyParam, pathParam, requestPath);
                        break;
                    case "PATCH":
                        break;
                    case "DELETE":
                        if ("/contact/object-type".equalsIgnoreCase(requestPath)) {
                            response = objectTypesController.deleteObjectType(requestPath, headerParam, pathParam);
                        } else if ("/contact/aero".equalsIgnoreCase(requestPath)) {
                            response = aeroController.deleteAero(requestPath, headerParam, pathParam);
                        } else if ("/contact/marine-vessel-info".equalsIgnoreCase(requestPath)) {
                            response = marineVesselController.deleteMarineVesselInfo(requestPath, headerParam, pathParam);
                        } else if ("/contact/organisation".equalsIgnoreCase(requestPath)) {
                            response = organisationController.delete(headerParam, pathParam, requestPath);
                        } else if ("/contact/comment".equalsIgnoreCase(requestPath)) {
                            response = commentsController.deleteComment(requestPath, headerParam, pathParam);
                        } else if ("/contact/group".equalsIgnoreCase(requestPath)) {
                            response = groupController.deleteGroup(requestPath, headerParam, pathParam);
                        } else if ("/contact/side".equalsIgnoreCase(requestPath)) {
                            response = sideController.deleteSide(requestPath, headerParam, pathParam);
                        } else if ("/contact/people".equalsIgnoreCase(requestPath)) {
                            response = peopleController.deletePeople(requestPath, headerParam, pathParam);
                        } else if ("/contact/infrastructure".equalsIgnoreCase(requestPath)) {
                            response = infrastructureController.deleteInfrastructure(requestPath, headerParam, pathParam);
                        } else if ("/contact/other-vehicle".equalsIgnoreCase(requestPath)) {
                            response = otherVehicleController.deleteOtherVehicle(requestPath, headerParam, pathParam);
                        } else if ("/contact/other-object".equalsIgnoreCase(requestPath)) {
                            response = otherObjectController.deleteOtherObject(requestPath, headerParam, pathParam);
                        } else if ("/contact/areas".equalsIgnoreCase(requestPath)) {
                            response = areasController.deleteAreas(requestPath, headerParam, pathParam);
                        } else if ("/contact/event".equalsIgnoreCase(requestPath)) {
                            response = eventController.deleteEvent(requestPath, headerParam, pathParam);
                        } else if ("/contact/keyword".equalsIgnoreCase(requestPath)) {
                            response = keywordController.delete(headerParam, pathParam, requestPath);
                        }
                        // delete Object
                        else if ("/contact/object".equalsIgnoreCase(requestPath)) {
                            response = commonController.deleteObject(headerParam, bodyParam, requestPath);
                        }
                        // check Relationship
                        else if ("/contact/object/check".equalsIgnoreCase(requestPath)) {
                            response = commonController.checkDeleteObject(headerParam, bodyParam, requestPath);
                        }
                        // delete keyword
                        else if ("/contact/keyword-data".equalsIgnoreCase(requestPath)) {
                            response = keywordController.deleteKeywordData(headerParam, bodyParam, requestPath);
                        }
                        // delete object group confirm
                        else if ("/contact/object-group/delete-confirm".equalsIgnoreCase(requestPath))
                            response = objectGroupController.delete(requestPath, headerParam, pathParam);
                        // delete object group unconfirmed
                        else if ("/contact/object-group/delete-unconfirmed".equalsIgnoreCase(requestPath))
                            response = objectGroupController.deleteUnconfirmed(requestPath, headerParam, pathParam);
                        // delete object mapping
                        else if ("/contact/object-group/object-mapping".equalsIgnoreCase(requestPath))
                            response = objectGroupController.deleteObjectMapping(requestPath, headerParam, bodyParam);
                        // delete object group define
                        else if ("/contact/object-group-define".equalsIgnoreCase(requestPath))
                            response = objectGroupDefineController.delete(requestPath, headerParam, pathParam);
                        break;
                    default:
                        break;
                }
            }

            LOGGER.info(" [<--] Server returned status {}", (response != null ? response.getStatus() : "null"));
            
            return response != null ? response.toJsonString() : null;
            
        } catch (Exception ex) {
            LOGGER.error("Error to processService >>> " + StringUtil.printException(ex));
            ex.printStackTrace();
        }

        return null;
    }
}
