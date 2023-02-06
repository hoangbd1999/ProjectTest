/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.model.dto;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

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
@Accessors(chain = true)
public class AggregateKeywordDataObjectGeneralInfoDTO implements Serializable {

    private List<KeywordDataObjectGeneralInfoDTO> paginatedResults;
    private TotalCount totalCount;
    private List<KeywordDataDTO> paginatedRefIds;
    private Map<String, Integer> paginatedRefIdMap;

    public class TotalCount {

        private int count;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }
}
