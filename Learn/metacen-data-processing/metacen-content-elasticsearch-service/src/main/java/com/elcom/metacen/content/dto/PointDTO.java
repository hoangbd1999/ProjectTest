package com.elcom.metacen.content.dto;

import lombok.Data;

@Data
public class PointDTO {
    private double longitude;
    private double latitude;

    public static PointDTO getPoint(double bearing, PointDTO point, float distance) {
        bearing = bearing * 3.141592653589793D / 180.0D;
        double longitude = point.getLongitude() * 3.141592653589793D / 180.0D;
        double latitude = point.getLatitude() * 3.141592653589793D / 180.0D;
        double desLatitude = Math.asin(Math.sin(latitude) * Math.cos((double)distance / 6371000.0D) + Math.cos(latitude) * Math.sin((double)distance / 6371000.0D) * Math.cos(bearing));
        double desLongitude = longitude + Math.atan2(Math.sin(bearing) * Math.sin((double)distance / 6371000.0D) * Math.cos(latitude), Math.cos((double)distance / 6371000.0D) - Math.sin(latitude) * Math.sin(desLatitude));
        PointDTO destinationPoint = new PointDTO();
        destinationPoint.setLongitude((desLongitude * 180.0D / 3.141592653589793D + 360.0D) % 360.0D);
        destinationPoint.setLatitude((desLatitude * 180.0D / 3.141592653589793D + 360.0D) % 360.0D);
        return destinationPoint;
    }
}
