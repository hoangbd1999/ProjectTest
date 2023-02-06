/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elcom.com.neo4j.message;

import com.google.gson.Gson;

import java.util.Map;

/**
 *
 * @author Admin
 */
public class RequestMessage {
    private String requestMethod;
    private String requestPath;
    private String version;
    private String urlParam;
    private String pathParam;
    private Map<String, Object> bodyParam;
    private Map<String, String> headerParam;

    public RequestMessage() {
    }

    public RequestMessage(String requestMethod, String requestPath, String version, String urlParam, String pathParam, Map<String, Object> bodyParam, Map<String, String> headerParam) {
        this.requestMethod = requestMethod;
        this.requestPath = requestPath;
        this.version = version;
        this.urlParam = urlParam;
        this.pathParam = pathParam;
        this.bodyParam = bodyParam;
        this.headerParam = headerParam;
    }

    public String getRequestMethod() {
        return this.requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestPath() {
        return this.requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public String getVersion() {
        return this.version == null ? "/v1.0" : this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrlParam() {
        return this.urlParam;
    }

    public void setUrlParam(String urlParam) {
        this.urlParam = urlParam;
    }

    public String getPathParam() {
        return this.pathParam;
    }

    public void setPathParam(String pathParam) {
        this.pathParam = pathParam;
    }

    public Map<String, Object> getBodyParam() {
        return this.bodyParam;
    }

    public void setBodyParam(Map<String, Object> bodyParam) {
        this.bodyParam = bodyParam;
    }

    public Map<String, String> getHeaderParam() {
        return this.headerParam;
    }

    public void setHeaderParam(Map<String, String> headerParam) {
        this.headerParam = headerParam;
    }

    public String toJsonString() {
        return (new Gson()).toJson(this);
    }
}
