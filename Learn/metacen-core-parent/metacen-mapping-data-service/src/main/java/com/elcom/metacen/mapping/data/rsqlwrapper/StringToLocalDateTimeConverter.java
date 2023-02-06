package com.elcom.metacen.mapping.data.rsqlwrapper;

import org.springframework.core.convert.converter.Converter;
import java.time.LocalDateTime;

/**
 *
 * @author Admin
 */
public class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {

    @Override
    public LocalDateTime convert(String source) {
        return LocalDateTime.parse(source);
    }
}
