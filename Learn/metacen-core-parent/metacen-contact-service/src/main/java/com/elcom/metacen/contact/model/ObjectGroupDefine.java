package com.elcom.metacen.contact.model;

import com.elcom.metacen.contact.model.dto.ObjectGroupDefine.ObjectGroupDefineMappingDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author hoangbd
 */
@Document(collection = "object_group_define")
@SuperBuilder
@Setter
@Getter
@RequiredArgsConstructor
@ToString
public class ObjectGroupDefine extends AbstractDocument {

    @Size(max = 36)
    @NotNull
    @Field("uuid")
    private String uuid;

    @Field("name")
    private String name;

    @Field("note")
    private String note;

    @Field("is_deleted")
    private Integer isDeleted;

    @Field("objects")
    private List<ObjectGroupDefineMappingDTO> objects;

}
