package com.elcom.metacen.vsat.collector.config.db.mongo.converter;

import org.springframework.core.convert.converter.Converter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class LocalDateTimeToZonedDateTimeConverter implements Converter<LocalDateTime, ZonedDateTime> {

    private static ZoneId vnzone;

    static {
        vnzone = ZoneId.of("Asia/Ho_Chi_Minh");
    }

    @Override
    public ZonedDateTime convert(LocalDateTime source) {
        return source == null ? null : ZonedDateTime.of(source, vnzone);
    }
}
