package com.elcom.metacen.raw.data.messaging.rabbitmq;

import com.elcom.metacen.constant.ResourcePath;
import com.elcom.metacen.message.RequestMessage;
import com.elcom.metacen.message.ResponseMessage;
import com.elcom.metacen.utils.StringUtil;
import com.elcom.metacen.raw.data.business.RawDataBusiness;
import com.elcom.metacen.raw.data.exception.ValidationException;
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
    private RawDataBusiness rawDataBusiness;

    @RabbitListener(queues = "${raw-data.rpc.queue}")
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
                        if ("/raw-data/ais-mapping/filter".equalsIgnoreCase(requestPath))
                            response = this.rawDataBusiness.filterAisMapping(urlParam, headerParam,requestPath);
                        else if ("/raw-data/satellite".equalsIgnoreCase(requestPath))
                            response = this.rawDataBusiness.getDetailSatellite(requestPath, headerParam, pathParam);
                        break;
                    case "POST":
                        
                        if ("/raw-data/vsat-ais/filter".equalsIgnoreCase(requestPath))
                            response = this.rawDataBusiness.filterVsatAisRawData(bodyParam, headerParam,requestPath);
                        else if ("/raw-data/ais/filter".equalsIgnoreCase(requestPath))
                            response = this.rawDataBusiness.filterAisRawData(bodyParam, headerParam,requestPath);
                        
                        else if ("/raw-data/position/overall".equalsIgnoreCase(requestPath)) // API Khai thác tổng thể
                            response = this.rawDataBusiness.findPositionOverall(bodyParam, headerParam,requestPath);
                        else if ("/raw-data/position/overall/media".equalsIgnoreCase(requestPath)) // API Media Khai thác tổng thể
                            response = this.rawDataBusiness.filterVsatMediaDataOverall(headerParam, bodyParam, requestPath);
                        else if ("/raw-data/position/overall/statistic/media".equalsIgnoreCase(requestPath)) // API Thống kê Media Khai thác tổng thể
                            response = this.rawDataBusiness.vsatMediaOverallStatistic(headerParam, bodyParam, requestPath);
              
                        else if ("/raw-data/vsat-media/filter".equalsIgnoreCase(requestPath))
                            response = this.rawDataBusiness.filterVsatMediaRawData(headerParam, bodyParam, requestPath);
                        else if ("/raw-data/vsat-media/fetch-mail-info".equalsIgnoreCase(requestPath))
                            response = this.rawDataBusiness.fetchMailInfo(headerParam, bodyParam, requestPath);
                        else if ("/raw-data/vsat-media/fetch-m3u8-file".equalsIgnoreCase(requestPath))
                            response = this.rawDataBusiness.fetchM3U8File(headerParam, bodyParam, requestPath);
                        else if ("/raw-data/vsat-media/convert-and-fetch-video".equalsIgnoreCase(requestPath))
                            response = this.rawDataBusiness.convertAndFetchVideo(headerParam, bodyParam, requestPath);
                        
                        else if ("/raw-data/vsat-media-relation/filter".equalsIgnoreCase(requestPath))
                            response = this.rawDataBusiness.filterVsatMediaRelationRawData(headerParam, bodyParam, requestPath);
                        else if ("/raw-data/vsat-media-relation/detail".equalsIgnoreCase(requestPath))
                            response = this.rawDataBusiness.getDetailMediaRelation(headerParam, bodyParam, requestPath);
                        
                        else if ("/raw-data/satellite/filter".equalsIgnoreCase(requestPath))
//                            response = this.rawDataBusiness.filterSatelliteRawData(bodyParam, headerParam,requestPath);
                            response = this.rawDataBusiness.filterSatelliteImageRawData(bodyParam, headerParam,requestPath);
                        else if ("/raw-data/satellite/filter-for-map".equalsIgnoreCase(requestPath))
                            response = this.rawDataBusiness.filterSatelliteImageRawDataForMap(bodyParam, headerParam,requestPath);

                        else if ("/raw-data/trip".equalsIgnoreCase(requestPath))
                            response = this.rawDataBusiness.getTripOfShips(bodyParam, headerParam, requestPath);
                        break;
                    case "PUT":
                        break;
                    case "PATCH":
                        break;
                    case "DELETE":
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
