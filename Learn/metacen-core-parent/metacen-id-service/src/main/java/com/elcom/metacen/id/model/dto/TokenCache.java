package com.elcom.metacen.id.model.dto;

import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
public class TokenCache implements Serializable {
    private String uuid;
    private String token;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
