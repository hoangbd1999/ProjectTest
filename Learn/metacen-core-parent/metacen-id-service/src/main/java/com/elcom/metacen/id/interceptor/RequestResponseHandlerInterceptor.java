/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.id.interceptor;

import com.elcom.metacen.id.service.TokenService;
import java.io.IOException;
import com.elcom.metacen.utils.StringUtil;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 *
 * @author Admin
 */
public class RequestResponseHandlerInterceptor implements ClientHttpRequestInterceptor {

    private static final String AUTHORIZATION = "Authorization";

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestResponseHandlerInterceptor.class);

    @Autowired
    private TokenService tokenService;

    /**
     * This method will intercept every request and response and based on
     * response status code if its 401 then will retry once
     *
     * @param request
     * @param body
     * @param execution
     * @return
     * @throws java.io.IOException
     */
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        //Add Bearer Token 2 Header
        String accessToken = tokenService.getAccessToken();
        LOGGER.info("ITS Core request accessToken: {}", accessToken);
        request.getHeaders().add(AUTHORIZATION, accessToken);
        //Trace request
        //traceRequest(request, body);
        //Execute
        ClientHttpResponse response = execution.execute(request, body);
        LOGGER.info("ITS Core response status code: {}", response.getStatusCode());
        //Process if accessToken expired
        if (HttpStatus.UNAUTHORIZED == response.getStatusCode()) {
            tokenService.removeAccessToken();
            accessToken = tokenService.getAccessToken();
            if (!StringUtil.isNullOrEmpty(accessToken)) {
                request.getHeaders().remove(AUTHORIZATION);
                request.getHeaders().add(AUTHORIZATION, accessToken);
                LOGGER.info("ITS Core refresh request accessToken: {}", accessToken);
                //retry                    
                response = execution.execute(request, body);
            }
        }
        //Trace response
        //traceResponse(response);
        return response;
    }

    private void traceRequest(HttpRequest request, byte[] body) throws IOException {
        LOGGER.info("request URI : " + request.getURI());
        LOGGER.info("request method : " + request.getMethod());
        LOGGER.info("request headers : {}", request.getHeaders());
        LOGGER.info("request headers Authorization: {}", request.getHeaders().get(AUTHORIZATION));
        LOGGER.info("request body : " + getRequestBody(body));
    }

    private String getRequestBody(byte[] body) throws UnsupportedEncodingException {
        if (body != null && body.length > 0) {
            return (new String(body, "UTF-8"));
        } else {
            return null;
        }
    }

    private void traceResponse(ClientHttpResponse response) throws IOException {
        String body = getBodyString(response);
        LOGGER.info("response status code: " + response.getStatusCode());
        LOGGER.info("response status text: " + response.getStatusText());
        LOGGER.info("response body : " + body);
    }

    private String getBodyString(ClientHttpResponse response) {
        try {
            if (response != null && response.getBody() != null) {// && isReadableResponse(response))
                StringBuilder inputStringBuilder = new StringBuilder();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getBody(),
                        StandardCharsets.UTF_8));
                String line = bufferedReader.readLine();
                while (line != null) {
                    inputStringBuilder.append(line);
                    inputStringBuilder.append('\n');
                    line = bufferedReader.readLine();
                }
                return inputStringBuilder.toString();
            } else {
                return null;
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }
}
