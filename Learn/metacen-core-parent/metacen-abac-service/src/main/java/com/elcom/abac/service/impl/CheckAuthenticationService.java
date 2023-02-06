package com.elcom.abac.service.impl;

import com.elcom.abac.dto.*;
import com.elcom.abac.messaging.rabbitmq.RabbitMQClient;
import com.elcom.abac.model.Policy;
import com.elcom.abac.model.Resource;
import com.elcom.abac.repository.RedisRepository;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.RequestMessage;
import com.elcom.metacen.message.ResponseMessage;
import com.elcom.metacen.utils.StringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service
public class CheckAuthenticationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckAuthenticationService.class);

    @Autowired
    private RabbitMQClient rabbitMQClient;

    @Autowired
    private RedisRepository redisRepository;

    @Async("threadPoolCheckPolicy")
    public CompletableFuture<ResultCheckDto> Authentication(Resource resource, Policy policy, Map<String, Object> bodyParam, Map<String, String> headerMap) throws ExecutionException, InterruptedException, TimeoutException {
       try {
           ResultCheckDto resultCheckDto = new ResultCheckDto();
           Map<String, Object> subject = (Map<String, Object>) bodyParam.get("subject");
           Map<String, Object> attributes = (Map<String, Object>) bodyParam.get("attributes");
           String uuid = (String) bodyParam.get("uuid");
           ConditionPolicy conditionPolicy = policy.getConditionPolicy();
           List<ConditionDetail> conditionDetails = conditionPolicy.getValue();
           List<String> miss = new ArrayList<>();
           List<String> missFalse = new ArrayList<>();
           List<Object> listValue = new ArrayList<>();
           List<String> typeCondition = new ArrayList<>();
           boolean check = false;
           for (ConditionDetail condition : conditionDetails
           ) {
               String param = condition.getParam();
               if (condition.getCondition().equals("Equals") || condition.getCondition().equals("NotEquals")) {
                   if (policy.getPolicyType().equals("param")) {
                       check = checkParamString(miss, listValue, typeCondition, condition, attributes, param, condition.getCondition(), conditionPolicy.getCondition(),missFalse);
                   } else {
                       check = checkRoleGroupOwnerString(param, policy.getPolicyType(), subject, attributes, condition.getCondition(),missFalse);
                   }
               } else if (condition.getCondition().equals("IsIn") || condition.getCondition().equals("IsNotIn")) {
                   if (policy.getPolicyType().equals("param")) {
                       if (attributes.get(param) == null) {
                           if (condition.isRequire()) {
                               missFalse.add(param);
                               check = false;
                           } else {
                               check = true;
                               miss.add(param);
                               listValue.add(condition.getValue());
                               typeCondition.add(condition.getCondition());
                           }
                       } else {
                           if (attributes.get(param) instanceof Number) {
                               if (condition.getCondition().equals("IsIn"))
                                   check = false;
                               else check = true;
                               //Có trong list Vulue
                               Integer valueRequest = (Integer) attributes.get(param);
                               List<Integer> values = (List<Integer>) condition.getValue();
                               for (Integer value : values
                               ) {
                                   if (valueRequest == value) {
                                       if (condition.getCondition().equals("IsIn"))
                                           check = true;
                                       else check = false;
                                       break;
                                   }
                               }
                           } else {
                               if (condition.getCondition().equals("IsIn"))
                                   check = false;
                               else check = true;
                               //Có trong list Vulue
                               String valueRequest = (String) attributes.get(param);
                               List<String> values = (List<String>) condition.getValue();
                               for (String value : values
                               ) {
                                   if (valueRequest.equals(value)) {
                                       if (condition.getCondition().equals("IsIn"))
                                           check = true;
                                       else check = false;
                                       break;
                                   }
                               }
                           }
                       }
                   } else if(policy.getPolicyType().equals("role")){
                       if (condition.getCondition().equals("IsIn"))
                           check = false;
                       else check = true;
                       //Có trong list Vulue
                       RoleCodeUuidRedis roleCodeUuidRedis = redisRepository.findRoleCodeRedis(uuid);
                       if(roleCodeUuidRedis!=null) {
                           List<String> values = roleCodeUuidRedis.getRoleCode();
                           String valueRequest = (String) attributes.get(param);
                           if(values != null) {
                               for (String value : values
                               ) {
                                   if (valueRequest.equals(value)) {
                                       if (condition.getCondition().equals("IsIn"))
                                           check = true;
                                       else check = false;
                                       break;
                                   }
                               }
                           }else {
                               check =false;
                           }
                       }else {
                           check = false;
                       }
                   } else {
                       if (attributes.get(param) == null) {
                           missFalse.add(param);
                           check = false;
                       } else {
                           if (subject.get(param) instanceof Number) {
                               if (condition.getCondition().equals("IsIn"))
                                   check = false;
                               else check = true;
                               //Có trong list Vulue
                               Integer valueRequest = (Integer) subject.get(param);
                               List<Integer> values = (List<Integer>) attributes.get(param);
                               for (Integer value : values
                               ) {
                                   if (valueRequest == value) {
                                       if (condition.getCondition().equals("IsIn"))
                                           check = true;
                                       else check = false;
                                       break;
                                   }
                               }
                           } else {
                               if (condition.getCondition().equals("IsIn"))
                                   check = false;
                               else check = true;
                               //Có trong list Vulue
                               String valueRequest = (String) subject.get(param);
                               List<String> values = (List<String>) attributes.get(param);
                               for (String value : values
                               ) {
                                   if (valueRequest.equals(value)) {
                                       if (condition.getCondition().equals("IsIn"))
                                           check = true;
                                       else check = false;
                                       break;
                                   }
                               }
                           }
                       }
                   }
               }else if(condition.getCondition().equals("Containts")) {
                   if(policy.getPolicyType().equals("group") || policy.getPolicyType().equals("owner")) {
                       try {
                           List<Object> listParam = (List<Object>) subject.get(param);
                           List<Object> listParamAttr = (List<Object>) attributes.get(param);
                           if (listParam == null || listParam.isEmpty() || listParamAttr == null || listParamAttr.isEmpty()) {
                               check = false;
                           } else {
                               if (listParam.get(0) instanceof Number) {
                                   List<Number> listParamObject = (List<Number>) subject.get(param);
                                   List<Number> listParamAttribute = (List<Number>) attributes.get(param);
                                   for (Number number : listParamObject) {
                                       boolean result = listParamAttribute.stream().anyMatch(p -> p == number);
                                       if (result) {
                                           check = true;
                                       } else {
                                           check = false;
                                           break;
                                       }
                                   }

                               } else {
                                   List<String> listParamObject = (List<String>) subject.get(param);
                                   List<String> listParamAttribute = (List<String>) attributes.get(param);
                                   for (String string : listParamObject) {
                                       boolean result = listParamAttribute.stream().anyMatch(p -> p.equals(string));
                                       if (result) {
                                           check = true;
                                       } else {
                                           check = false;
                                           break;
                                       }
                                   }
                               }
                           }
                       } catch (Exception ex) {
                           check = false;
                           LOGGER.info("Lỗi parser List " + param);
                       }

                   }else {
                       try {
                           List<Object> listParam = (List<Object>) attributes.get(param);
                           List<Object> listParamAttr = (List<Object>) condition.getValue() ;
                           if (listParam == null || listParam.isEmpty() || listParamAttr == null || listParamAttr.isEmpty()) {
                               check = false;
                           } else {
                               if (listParam.get(0) instanceof Number) {
                                   List<Number> listParamObject = (List<Number>) attributes.get(param);
                                   List<Number> listParamAttribute = (List<Number>) condition.getValue();
                                   for (Number number : listParamObject) {
                                       boolean result = listParamAttribute.stream().anyMatch(p -> p == number);
                                       if (result) {
                                           check = true;
                                       } else {
                                           check = false;
                                           break;
                                       }
                                   }

                               } else {
                                   List<String> listParamObject = (List<String>) attributes.get(param);
                                   List<String> listParamAttribute = (List<String>) condition.getValue();
                                   for (String string : listParamObject) {
                                       boolean result = listParamAttribute.stream().anyMatch(p -> p.equals(string));
                                       if (result) {
                                           check = true;
                                       } else {
                                           check = false;
                                           break;
                                       }
                                   }
                               }
                           }
                       } catch (Exception ex) {
                           check = false;
                           LOGGER.info("Lỗi parser List " + param);
                       }
                   }
               }else if(condition.getCondition().equals("OneContaints")) {
                   if(policy.getPolicyType().equals("group") || policy.getPolicyType().equals("owner")) {
                       try {
                           List<Object> listParam = (List<Object>) subject.get(param);
                           List<Object> listParamAttr = (List<Object>) attributes.get(param);
                           if (listParam == null || listParam.isEmpty() || listParamAttr == null || listParamAttr.isEmpty()) {
                               check = false;
                           } else {
                               if (listParam.get(0) instanceof Number) {
                                   List<Number> listParamObject = (List<Number>) subject.get(param);
                                   List<Number> listParamAttribute = (List<Number>) attributes.get(param);
                                   for (Number number : listParamObject) {
                                       boolean result = listParamAttribute.stream().anyMatch(p -> p == number);
                                       if (result) {
                                           check = true;
                                           break;
                                       } else {
                                           check = false;
                                       }
                                   }

                               } else {
                                   List<String> listParamObject = (List<String>) subject.get(param);
                                   List<String> listParamAttribute = (List<String>) attributes.get(param);
                                   for (String string : listParamObject) {
                                       boolean result = listParamAttribute.stream().anyMatch(p -> p.equals(string));
                                       if (result) {
                                           check = true;
                                           break;
                                       } else {
                                           check = false;
                                       }
                                   }
                               }
                           }
                       } catch (Exception ex) {
                           check = false;
                           LOGGER.info("Lỗi parser List " + param);
                       }

                   }else {
                       try {
                           List<Object> listParam = (List<Object>) attributes.get(param);
                           List<Object> listParamAttr = (List<Object>) condition.getValue() ;
                           if (listParam == null || listParam.isEmpty() || listParamAttr == null || listParamAttr.isEmpty()) {
                               check = false;
                           } else {
                               if (listParam.get(0) instanceof Number) {
                                   List<Number> listParamObject = (List<Number>) attributes.get(param);
                                   List<Number> listParamAttribute = (List<Number>) condition.getValue();
                                   for (Number number : listParamObject) {
                                       boolean result = listParamAttribute.stream().anyMatch(p -> p == number);
                                       if (result) {
                                           check = true;
                                           break;
                                       } else {
                                           check = false;
                                       }
                                   }

                               } else {
                                   List<String> listParamObject = (List<String>) attributes.get(param);
                                   List<String> listParamAttribute = (List<String>) condition.getValue();
                                   for (String string : listParamObject) {
                                       boolean result = listParamAttribute.stream().anyMatch(p -> p.equals(string));
                                       if (result) {
                                           check = true;
                                           break;
                                       } else {
                                           check = false;
                                       }
                                   }
                               }
                           }
                       } catch (Exception ex) {
                           check = false;
                           LOGGER.info("Lỗi parser List " + param);
                       }
                   }
               } else {
                       if (policy.getPolicyType().equals("param")) {
                           check = checkParamInterger(miss, listValue, typeCondition, condition, attributes, param, condition.getCondition(), conditionPolicy.getCondition(),missFalse);
                       } else {
                           check = checkRoleGroupOwnerInterger(param, policy.getPolicyType(), subject, attributes, condition.getCondition(),missFalse);
                       }
                   }
               if (conditionPolicy.getCondition().equals("AllOf")) {
                   if (!check)
                       break;
               } else if (conditionPolicy.getCondition().equals("AnyOf")) {
                   if (check)
                       break;
               }
           }
           if (conditionPolicy.getCondition().equals("AllOf")) {
               if (check) {
                   if (miss.size() > 0) {
                       check = checkResponseParamService(resource, bodyParam, headerMap, policy, miss, listValue, typeCondition, attributes);
                   }
               }
           } else {
               if (!check) {
                   if (miss.size() > 0) {
                       check = checkResponseParamService(resource, bodyParam, headerMap, policy, miss, listValue, typeCondition, attributes);
                   }
               }
           }
           resultCheckDto.setStatus(check);
           resultCheckDto.setType(policy.getEffect());
           resultCheckDto.setDescription(missFalse);
           return CompletableFuture.completedFuture(resultCheckDto);
       }catch (Exception ex){
           ResultCheckDto resultCheckDto= new ResultCheckDto();
           resultCheckDto.setStatus(false);
           resultCheckDto.setType(ex.toString());
           resultCheckDto.setDescription(ex.toString());
           return CompletableFuture.completedFuture(resultCheckDto);
       }
    }

    private boolean checkResponseParamService(Resource resource, Map<String, Object> bodyParam, Map<String, String> headerMap, Policy policy, List<String> miss, List<Object> listValue, List<String> typeCondition, Map<String, Object> attributes) {
        RequestMessage userRpcRequest = new RequestMessage();
        userRpcRequest.setRequestMethod("GET");
        userRpcRequest.setVersion(null);
        userRpcRequest.setRequestPath(resource.getPipRpcPath());
        Map<String, Object> bodyParamSend = new HashMap<>(bodyParam);
        bodyParamSend.put("missing", miss);
        userRpcRequest.setBodyParam(bodyParamSend);
        userRpcRequest.setUrlParam(null);
        userRpcRequest.setHeaderParam(headerMap);
        String result = rabbitMQClient.callRpcService(resource.getPipRpcExchange(), resource.getPipRpcQueue(), resource.getPipRpcKey(), userRpcRequest.toJsonString());
        LOGGER.info("Nhận kết quả từ PIP gọi vào queue" + resource.getPipRpcQueue() +result );
        if (result != null) {
            LOGGER.info("[<==]" +result);
            ObjectMapper mapper = new ObjectMapper();
            //DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //mapper.setDateFormat(df);
            ResponseMessage response = null;
            try {
                response = mapper.readValue(result, ResponseMessage.class);
            } catch (JsonProcessingException ex) {
                ex.printStackTrace();
//                LOGGER.info("Lỗi parse json khi gọi user service verify: " + ex.toString());
                return false;
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (response != null && response.getStatus() == HttpStatus.OK.value()) {
                try {
                    //Process
                    MessageContent content = response.getData();
                    Object data = content.getData();
                    if (data != null) {
                        Map<String, Object> paramData = (Map<String, Object>) data;
                        Boolean check = false;
                        for (int i = 0; i < miss.size(); i++) {
                            String param = miss.get(i);
                            if (typeCondition.get(i).equals("Equals") || typeCondition.get(i).equals("NotEquals")) {
                                check = checkParamString(listValue.get(i),paramData, param, typeCondition.get(i));
                            } else if (typeCondition.get(i).equals("IsIn") || typeCondition.get(i).equals("IsNotIn")) {
                                if (paramData.get(param) == null) {
                                    check = false;
                                } else {
                                    if (paramData.get(param) instanceof Number) {
                                        if (typeCondition.get(i).equals("IsIn"))
                                            check = false;
                                        else check = true;
                                        //Có trong list Vulue
                                        Integer valueRequest = (Integer) paramData.get(param);
                                        List<Integer> values = (List<Integer>) listValue.get(i);
                                        for (Integer value : values
                                        ) {
                                            if (valueRequest == value) {
                                                if (typeCondition.get(i).equals("IsIn"))
                                                    check = true;
                                                else check = false;
                                                break;
                                            }
                                        }
                                    } else {
                                        if (typeCondition.get(i).equals("IsIn"))
                                            check = false;
                                        else check = true;
                                        //Có trong list Vulue
                                        String valueRequest = (String) paramData.get(param);
                                        List<String> values = (List<String>) listValue.get(i);
                                        for (String value : values
                                        ) {
                                            if (valueRequest.equals(value)) {
                                                if (typeCondition.get(i).equals("IsIn"))
                                                    check = true;
                                                else check = false;
                                                break;
                                            }
                                        }
                                    }
                                }
                            } else {
                                check = checkParamInteger(listValue.get(i), paramData, param, typeCondition.get(i));
                            }
                            if (policy.getConditionPolicy().getCondition().equals("AllOf")) {
                                if (!check)
                                    break;
                            } else if (policy.getConditionPolicy().getCondition().equals("AnyOf")) {
                                if (check)
                                    break;
                            }
                        }
                        return check;
                    } else
                        return false;
                } catch (Exception ex) {
//                    LOGGER.info("Lỗi giải mã AuthorizationResponseDTO khi gọi user service verify: " + ex.toString());
                    LOGGER.info("Lỗi giải mã reponse từ service lấy dữ liệu");
                    return false;
                }
            } else {
                //Forbidden
                LOGGER.info("Lỗi giải mã reponse từ service lấy dữ liệu");
                return false;
            }
        } else {
            //Forbidden
            LOGGER.info("Lỗi giải mã reponse từ service lấy dữ liệu");
            return false;
        }
    }

    private boolean checkRoleGroupOwnerInterger(String param, String typePollicy, Map<String, Object> subject, Map<String, Object> attributes, String typeCondition,List<String> missFalse) {
        try {
            Integer paramCheck = (Integer) subject.get(param);
//            if(paramCheck == null || paramCheck == 0){
//                missFalse.add(param);
//                return false;
//            }
            try {
                Integer value = (Integer) attributes.get(param);
                if(paramCheck == null || paramCheck == 0){
                    missFalse.add(param);
                    return false;
                }
                boolean check = subCheckInterger(typeCondition, paramCheck, value);
                return check;
            } catch (Exception ex) {
                ex.printStackTrace();
                LOGGER.info("Lỗi parse value");
                return false;
            }
        } catch (Exception ex) {
            LOGGER.info("Lỗi parse group owner attributte");
            return false;
        }
    }

    private boolean checkParamInterger(List<String> miss, List<Object> listValue, List<String> typeCondition, ConditionDetail condition, Map<String, Object> attributes, String param, String conditionType, String conditionResult, List<String> missFalse) {
        try {
            Integer paramCheck = (Integer) attributes.get(param);
            if (paramCheck == null) {
                if (condition.isRequire()) {
                    missFalse.add(param);
                    return false;
                } else {
                    miss.add(param);
                    listValue.add(condition.getValue());
                    typeCondition.add(conditionType);
                    if (conditionResult.equals("AllOf"))
                        return true;
                    if (conditionResult.equals("AnyOf"))
                        return false;
                    return false;
                }
            } else {
                try {
                    Integer value = (Integer) condition.getValue();
                    boolean check = subCheckInterger(conditionType, paramCheck, value);
                    return check;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    LOGGER.info("Lỗi parse value");
                    return false;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            LOGGER.info(" ko co du lieu");
            return false;
        }
    }

    private boolean checkParamString(List<String> miss, List<Object> listValue, List<String> typeCondition, ConditionDetail condition, Map<String, Object> attributes, String param, String conditionType, String conditionResult,List<String> missFalse) {
        try {
            String paramCheck = (String) attributes.get(param);
            if (paramCheck == null) {
                if (condition.isRequire()) {
                    missFalse.add(param);
                    return false;
                } else {
                    miss.add(param);
                    listValue.add(condition.getValue());
                    typeCondition.add(conditionType);
                    if (conditionResult.equals("AllOf"))
                        return true;
                    if (conditionResult.equals("AnyOf"))
                        return false;
                    return false;
                }
            } else {
                try {
                    String value = (String) condition.getValue();
                    if (conditionType.equals("Equals")) {
                        if (paramCheck.equals(value)) {
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        if (!paramCheck.equals(value)) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    LOGGER.info("Lỗi parse value");
                    return false;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private boolean checkParamString(Object ObjectValue, Map<String, Object> attributes, String param, String conditionType) {
        try {
            String paramCheck = (String) attributes.get(param);
            try {
                String value = (String) ObjectValue;
                if (conditionType.equals("Equals")) {
                    if (paramCheck.equals(value)) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    if (!paramCheck.equals(value)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                LOGGER.info("Lỗi parse value");
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private boolean checkParamInteger(Object objectValue, Map<String, Object> attributes, String param, String conditionType) {
        try {
            Integer paramCheck = (Integer) attributes.get(param);
            try {
                Integer value = (Integer) objectValue;
                boolean check = subCheckInterger(conditionType, paramCheck, value);
                return check;
            } catch (Exception ex) {
                ex.printStackTrace();
                LOGGER.info("Lỗi parse value");
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private boolean checkRoleGroupOwnerString(String param, String typePollicy, Map<String, Object> subject, Map<String, Object> attributes, String conditionType,List<String> missFalse) {
        try {
            String paramCheck = (String) subject.get(param);
            if(paramCheck ==null){
                missFalse.add(param);
                return false;
            }
            try {
                String value = (String) attributes.get(param);
                if(value ==null){
                    missFalse.add(param);
                    return false;
                }
                if (conditionType.equals("Equals")) {
                    if (paramCheck.equals(value)) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    if (!paramCheck.equals(value)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                LOGGER.info("Lỗi parse value");
                return false;
            }
        } catch (Exception ex) {
            missFalse.add(ex.toString());
            return false;
        }
    }

    private boolean subCheckInterger(String conditionType, int numberRequest, int numnumberValaue) {
        switch (conditionType) {
            case "Eq":
                if (numberRequest == numnumberValaue) {
                    return true;
                } else {
                    return false;
                }
            case "Neq":
                if (numberRequest != numnumberValaue) {
                    return true;
                } else {
                    return false;
                }
            case "Gt":
                if (numberRequest > numnumberValaue) {
                    return true;
                } else {
                    return false;
                }
            case "Gte":
                if (numberRequest >= numnumberValaue) {
                    return true;
                } else {
                    return false;
                }
            case "Lt":
                if (numberRequest < numnumberValaue) {
                    return true;
                } else {
                    return false;
                }
            case "Lte":
                if (numberRequest <= numnumberValaue) {
                    return true;
                } else {
                    return false;
                }
            default:
                return false;

        }
    }

}
