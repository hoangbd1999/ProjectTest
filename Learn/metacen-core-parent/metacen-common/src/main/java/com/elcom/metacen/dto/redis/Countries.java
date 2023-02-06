package com.elcom.metacen.dto.redis;

//import com.fasterxml.jackson.databind.PropertyNamingStrategy;
//import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.io.Serializable;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 *
 * @author Admin
 */
@Document(collection = "countries")
@SuperBuilder
@Setter
@Getter
@RequiredArgsConstructor
@ToString
//@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Countries implements Serializable {

    @Field("id")
    private Integer countryId;

    @Field("name")
    private String name;

    @Field("flag")
    private String flag;
}
