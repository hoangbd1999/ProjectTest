package com.elcom.metacen.vsat.collector.repository.mongodb.rsqlwrapper;

import com.github.rutledgepaulv.qbuilders.structures.FieldPath;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.function.BiFunction;

import static org.apache.commons.lang3.reflect.FieldUtils.getField;

/**
 *
 * @author Admin
 */
@Slf4j
public class EnhancedEntityFieldTypeResolver implements BiFunction<FieldPath, Class<?>, Class<?>> {

    @Override
    public Class<?> apply(FieldPath path, Class<?> root) {
        String[] splitField = path.asKey().split("\\.", 2);

        try {
            if (splitField.length == 1) {
                return normalize(getField(root, splitField[0], true));
            } else {
                return apply(new FieldPath(splitField[1]), normalize(getField(root, splitField[0], true)));
            }
        } catch (Exception e) {
            log.error("RSQL parser. Not understand field {} in the rsql", splitField);
            return null;
        }
    }

    private static Class<?> normalize(Field field) throws Exception {
        if (Collection.class.isAssignableFrom(field.getType())) {
            return getFirstTypeParameterOf(field);
        } else if (field.getType().isArray()) {
            return field.getType().getComponentType();
        } else {
            return field.getType();
        }
    }

    private static Class<?> getFirstTypeParameterOf(Field field) {
        return (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
    }

}
