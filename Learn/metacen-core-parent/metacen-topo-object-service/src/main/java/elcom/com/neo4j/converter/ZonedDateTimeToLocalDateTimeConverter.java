package elcom.com.neo4j.converter;

import org.springframework.core.convert.converter.Converter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ZonedDateTimeToLocalDateTimeConverter implements Converter<ZonedDateTime, LocalDateTime> {

    private static ZoneId vnzone;

    static {
        vnzone = ZoneId.of("Asia/Ho_Chi_Minh");
    }

    @Override
    public LocalDateTime convert(ZonedDateTime source) {
        return source == null ? null : LocalDateTime.ofInstant(source.toInstant(), vnzone);
    }
}
