/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.notify.model.dto;

import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 *
 * @author Admin
 */
@Setter
@Getter
@RequiredArgsConstructor
@SuperBuilder
public class BaseDto<T> implements Serializable {

    private Long id;

    private Long createdBy;

    private Date createdDate;

    private Long modifiedBy;

    private Date modifiedDate;

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

        BaseDto other = (BaseDto) obj;
        return this.getId().longValue() == other.getId().longValue();
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }
}
