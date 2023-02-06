package com.elcom.metacen.common;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author hanh
 */
@Builder
@Setter
@Getter
@ToString
public class NameValuePair implements Serializable {

    private String name;
    private Object value;

    public NameValuePair() {
    }

    public NameValuePair(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        NameValuePair other = (NameValuePair) obj;
        return this.getName().equals(other.getName());
    }

    @Override
    public int hashCode() {
        return name == null ? 0 : name.hashCode();
    }

}
