package com.elcom.metacen.vsat.collector.model.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 *
 * @author Admin
 */
@SuperBuilder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ObjectGeneralInfoDTO implements Serializable {

    private String objectType;
    private String id;
    private String uuid;
    private String name;
    private String countryId;
    private String sideId;
    private String sideName;
    private int isDeleted;
}
