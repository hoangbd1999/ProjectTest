package elcom.com.neo4j.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import elcom.com.neo4j.message.RequestMessage;
import elcom.com.neo4j.service.LinkObjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TimeZone;

@Service
public class WorkerServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerServer.class);

    @Autowired
    private LinkObjectService linkObjectService;

    @RabbitListener(queues = "${link.object.worker.queue}")
    public void workerRecevie(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mapper.setDateFormat(df);
            RequestMessage request = mapper.readValue(json, RequestMessage.class);
            Map<String,Object> body = request.getBodyParam();
            linkObjectService.addLinkObject(body);
            LOGGER.info(" [-->] Server received request for : {}", json);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    @RabbitListener(queues = "${link.object.worker.queue.contains}")
    public void workerRecevieContainsObject(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mapper.setDateFormat(df);
            RequestMessage request = mapper.readValue(json, RequestMessage.class);
            Map<String,Object> body = request.getBodyParam();
            linkObjectService.createLinkContainsObject(body);
            LOGGER.info(" [-->] Server received request for : {}", json);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    @RabbitListener(queues = "${link.object.worker.queue.delete.node}")
    public void workerRecevieDelete(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mapper.setDateFormat(df);
            RequestMessage request = mapper.readValue(json, RequestMessage.class);
            Map<String,Object> body = request.getBodyParam();
            linkObjectService.deleteNode(body);
            LOGGER.info(" [-->] Server received request for : {}", json);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    @RabbitListener(queues = "${link.object.worker.queue.updatenode}")
    public void workerRecevieUpdateNode(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mapper.setDateFormat(df);
            RequestMessage request = mapper.readValue(json, RequestMessage.class);
            Map<String,Object> body = request.getBodyParam();
            linkObjectService.updateNode(body);
            LOGGER.info(" [-->] Server received request for : {}", json);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
