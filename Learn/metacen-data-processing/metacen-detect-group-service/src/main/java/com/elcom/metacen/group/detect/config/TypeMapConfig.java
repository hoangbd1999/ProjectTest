package com.elcom.metacen.group.detect.config;

import com.elcom.metacen.group.detect.converter.CoordinatesConverter;
import com.elcom.metacen.group.detect.model.ObjectGroupConfig;
import com.elcom.metacen.group.detect.model.dto.ObjectGroupConfigDTO;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class TypeMapConfig {
    @Autowired private ModelMapper modelMapper;

    @Bean
    public TypeMap<ObjectGroupConfig, ObjectGroupConfigDTO> objectGroupConfigMap() {
        TypeMap<ObjectGroupConfig, ObjectGroupConfigDTO> typeMap = modelMapper.typeMap(ObjectGroupConfig.class, ObjectGroupConfigDTO.class)
                .addMappings(mapper -> {
                    mapper.using(new CoordinatesConverter())
                            .map(ObjectGroupConfig::getCoordinates, ObjectGroupConfigDTO::setCoordinates);
                });
        return typeMap;
    }
}
