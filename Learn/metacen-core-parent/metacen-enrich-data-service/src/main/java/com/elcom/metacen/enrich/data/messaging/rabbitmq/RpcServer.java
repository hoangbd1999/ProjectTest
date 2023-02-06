package com.elcom.metacen.enrich.data.messaging.rabbitmq;

import com.elcom.metacen.constant.ResourcePath;
import com.elcom.metacen.enrich.data.exception.ValidationException;
import com.elcom.metacen.message.RequestMessage;
import com.elcom.metacen.message.ResponseMessage;
import com.elcom.metacen.utils.StringUtil;
import com.elcom.metacen.enrich.data.business.EnrichDataBusiness;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

/**
 * @author Admin
 */
public class RpcServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

    @Autowired
    private EnrichDataBusiness enrichDataBusiness;

    @RabbitListener(queues = "${enrich-data.rpc.queue}")
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
                        if ("/enrich-data/satellite".equalsIgnoreCase(requestPath))
                            response = this.enrichDataBusiness.getDetailSatellite(requestPath, headerParam, pathParam);
                        else if ("/enrich-data/satellite-analyzed".equalsIgnoreCase(requestPath))
                            response = this.enrichDataBusiness.getDetailSatelliteAnalyzed(requestPath, headerParam, pathParam);
                        else if ("/enrich-data/satellite-capture-time".equalsIgnoreCase(requestPath))
                            response = this.enrichDataBusiness.getListSatelliteCaptureTime(requestPath, headerParam, urlParam);
                        else if ("/enrich-data/satellite-comparison".equalsIgnoreCase(requestPath))
                            response = this.enrichDataBusiness.getDetailSatelliteComparison(requestPath, headerParam, pathParam);
                        else if ("/enrich-data/vsat-media-data-object-analyzed".equalsIgnoreCase(requestPath))
                            response = this.enrichDataBusiness.getListVsatMediaDataObjectAnalyzed(requestPath, headerParam, pathParam);
                        else if ("/enrich-data/vsat-media-analyzed".equalsIgnoreCase(requestPath))
                            response = this.enrichDataBusiness.getDetailVsatMediaAnalyzed(requestPath, headerParam, pathParam);
                        else if( "/enrich-data/vessel/detail".equalsIgnoreCase(requestPath) && pathParam != null && pathParam.length() > 0 )
                            response = this.enrichDataBusiness.findDetailVessel(headerParam, pathParam, requestPath);
                        break;
                    case "POST":
                        if ("/enrich-data/satellite/filter".equalsIgnoreCase(requestPath))
                            response = this.enrichDataBusiness.filterSatelliteImageEnrichData(bodyParam, headerParam, requestPath);
                        else if ("/enrich-data/satellite/filter-for-map".equalsIgnoreCase(requestPath))
                            response = this.enrichDataBusiness.filterSatelliteImageEnrichDataForMap(bodyParam, headerParam, requestPath);
                        else if ("/enrich-data/vsat-media-analyzed/filter".equalsIgnoreCase(requestPath))
                            response = this.enrichDataBusiness.filterVsatMediaAnalyzed(headerParam, bodyParam, requestPath);
                        else if ("/enrich-data/note/internal".equalsIgnoreCase(requestPath))
                            response = this.enrichDataBusiness.noteChange(bodyParam);
                        else if ("/enrich-data/satellite-comparison".equalsIgnoreCase(requestPath))
                            response = this.enrichDataBusiness.insertSatelliteComparison(headerParam, bodyParam, requestPath);
                        else if ("/enrich-data/satellite-comparison/filter".equalsIgnoreCase(requestPath))
                            response = this.enrichDataBusiness.filterSatelliteComparison(bodyParam, headerParam, requestPath);
                        else if ("/enrich-data/vsat-media-data-object-analyzed".equalsIgnoreCase(requestPath))
                            response = this.enrichDataBusiness.insertVsatMediaDataObjectAnalyzed(headerParam, bodyParam, requestPath);
                        else if ("/enrich-data/vsat-media-data-object-analyzed/check-exist".equalsIgnoreCase(requestPath))
                            response = this.enrichDataBusiness.checkExistByObjectUuid(headerParam, bodyParam, requestPath);
                        // khai thac tong the (xem hành trình nhóm đối tượng)
                        else if( "/enrich-data/vsat-ais/search-list-all-general".equalsIgnoreCase(requestPath) )
                            response = this.enrichDataBusiness.searchAisListAllGeneral(headerParam,bodyParam, requestPath);
                        break;
                    case "PUT":
                        if ("/enrich-data/vsat-media-data-object-analyzed/change-name".equalsIgnoreCase(requestPath))
                        response = enrichDataBusiness.updateNameObjectInternal(bodyParam);
                        break;
                    case "PATCH":
                        break;
                    case "DELETE":
                        if ("/enrich-data/satellite".equalsIgnoreCase(requestPath))
                            response = this.enrichDataBusiness.deleteSatelliteAnalyzed(requestPath, headerParam, pathParam);
                        else if ("/enrich-data/satellite-comparison".equalsIgnoreCase(requestPath))
                            response = this.enrichDataBusiness.deleteSatelliteComparison(requestPath, headerParam, pathParam);
                        else if ("/enrich-data/vsat-media-data-object-analyzed".equalsIgnoreCase(requestPath))
                            response = this.enrichDataBusiness.deleteVsatMediaDataObjectAnalyzed(requestPath, headerParam, pathParam);
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
