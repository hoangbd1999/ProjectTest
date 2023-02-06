package com.elcom.metacen.enrich.data.model.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AdvanceFilterDTO {

    private String field;
    private String operator; // IS, IS_NOT, IS_ONE_OF, IS_NOT_ONE_OF, IS_BETWEEN, IS_NOT_BETWEEN
    private List<String> value;
    private String unit;
}
