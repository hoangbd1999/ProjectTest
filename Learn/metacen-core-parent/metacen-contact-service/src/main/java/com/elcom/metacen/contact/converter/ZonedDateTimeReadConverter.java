package com.elcom.metacen.contact.converter;

import org.springframework.core.convert.converter.Converter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class ZonedDateTimeReadConverter implements Converter<Date, ZonedDateTime> {

    private static ZoneId vnzone;

    static {
        vnzone = ZoneId.of("Asia/Ho_Chi_Minh");
    }

    @Override
    public ZonedDateTime convert(Date date) {
        return date == null ? null : date.toInstant().atZone(vnzone);
    }
}
