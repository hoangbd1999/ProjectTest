///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//
//import com.elcom.metacen.message.RequestMessage;
//import com.elcom.metacen.rbac.messaging.rabbitmq.RabbitMQClient;
//import java.util.HashMap;
//import java.util.Map;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.MockitoJUnitRunner;
//import org.springframework.amqp.core.AmqpAdmin;
//import org.springframework.amqp.core.AmqpTemplate;
//import org.springframework.amqp.core.Queue;
//import org.springframework.beans.factory.annotation.Qualifier;
//
///**
// *
// * @author Admin
// */
//@RunWith(MockitoJUnitRunner.class)
//public class TestCall {
//
//    @InjectMocks
//    private RabbitMQClient rabbitMQClient = new RabbitMQClient();
//    
//    @Mock
//    private AmqpAdmin myService;
//    
//    @Mock
//    private AmqpTemplate amqpTemplate;
//    
//    @Mock
//    @Qualifier("directAutoDeleteQueue")
//    private Queue directAutoDeleteQueue;
//
//    @Test
//    public void testCall() {
//        //
//        Map<String, Object> bodyParam = new HashMap<>();
//        bodyParam.put("requestMethod", "DELETE");
//        bodyParam.put("uuid", "f8b3ef52-a8c5-499f-a204-7e825b653155");
//        bodyParam.put("apiPath", "/v1.0/user/list");
//        RequestMessage userRequest = new RequestMessage("POST", "/v1.0/rbac/authorization", null, null, bodyParam, null);
//        String result = rabbitMQClient.callRpcService("rbac_rpc_exchange", "rbac_rpc_queue", "rbac_rpc", userRequest.toJsonString());
//        System.out.println("result: " + result);
//    }
//}
