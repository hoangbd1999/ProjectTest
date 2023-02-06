package com.elcom.metacen.group.detect.converter;

import org.modelmapper.AbstractConverter;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CoordinatesConverter extends AbstractConverter<String, List<Point2D>> {

    @Override
    protected List<Point2D> convert(String s) {
        s = s.trim();
        return Arrays.stream(s.split("; "))
                .map(latlng -> {
                    String[] pair = latlng.split(",");
                    return new Point2D.Double(Double.parseDouble(pair[0]), Double.parseDouble(pair[1]));
                })
                .collect(Collectors.toList());
    }
}
