package com.elcom.metacen.group.detect.config;

import com.elcom.metacen.group.detect.model.ObjectGroupConfig;
import com.elcom.metacen.group.detect.model.dto.ObjectGroupConfigDTO;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.convention.NamingConventions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setSourceNamingConvention(NamingConventions.JAVABEANS_ACCESSOR)
                .setMatchingStrategy(MatchingStrategies.STRICT);

        return modelMapper;
    }
}
