package com.elcom.metacen.common;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author hanh
 */
@Setter
@Getter
@ToString
public class IdNamePair<I, N> implements Serializable {

    private I id;
    private N name;

    public IdNamePair() {
    }

    public IdNamePair(I id, N name) {
        this.id = id;
        this.name = name;
    }
}
