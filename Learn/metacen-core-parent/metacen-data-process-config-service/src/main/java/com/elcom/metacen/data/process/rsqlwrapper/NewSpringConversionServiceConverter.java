package com.elcom.metacen.data.process.rsqlwrapper;

import com.github.rutledgepaulv.rqe.conversions.StringToTypeConverter;
import com.github.rutledgepaulv.rqe.conversions.parsers.StringToObjectBestEffortConverter;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

/**
 *
 * @author Admin
 */
public class NewSpringConversionServiceConverter implements StringToTypeConverter {

    private ConversionService conversionService;

    public NewSpringConversionServiceConverter() {
        DefaultConversionService conversions = new DefaultConversionService();
        //conversions.addConverter(new StringToInstantConverter());
        conversions.addConverter(new StringToLocalDateTimeConverter());
        conversions.addConverter(new StringToObjectBestEffortConverter());
        this.conversionService = conversions;
    }

    public NewSpringConversionServiceConverter(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return conversionService.canConvert(String.class, clazz);
    }

    @Override
    public Object apply(String s, Class<?> aClass) {
        return conversionService.convert(s, aClass);
    }
}
