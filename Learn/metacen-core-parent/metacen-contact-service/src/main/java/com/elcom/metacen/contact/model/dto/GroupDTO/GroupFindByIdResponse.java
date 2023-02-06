package com.elcom.metacen.contact.model.dto.GroupDTO;

import com.elcom.metacen.contact.model.dto.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupFindByIdResponse extends BaseDTO<GroupFindByIdResponse> {
    private String name;
    private String note;
    private String sideId;
    private List<Object> groupObject;
}
