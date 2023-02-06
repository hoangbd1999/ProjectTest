package elcom.com.neo4j.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarineVesselDTO extends BaseDTO<MarineVesselDTO> {
    private String id;
    private String uuid;
    private Long mmsi;
    private String name;
    private String imo;
    private Long countryId;
    private String typeId;
    private Double dimA;
    private Double dimC;
    private String payroll;
    private String description;
    private String equipment;
    private Long draught;
    private Long grossTonnage;
    private Double speedMax;
    private String sideId;
}