package com.elcom.metacen.contact.model.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import java.io.Serializable;

/**
 *
 * @author hoangbd
 */
@SuperBuilder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ObjectTypesRequestDTO implements Serializable {

    private String typeName;
    private String typeCode;
    private String typeDesc;
    private String typeObject;
}
