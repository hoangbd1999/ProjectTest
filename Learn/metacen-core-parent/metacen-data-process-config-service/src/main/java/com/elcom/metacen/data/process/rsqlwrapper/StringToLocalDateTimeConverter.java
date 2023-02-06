package com.elcom.metacen.data.process.rsqlwrapper;

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
