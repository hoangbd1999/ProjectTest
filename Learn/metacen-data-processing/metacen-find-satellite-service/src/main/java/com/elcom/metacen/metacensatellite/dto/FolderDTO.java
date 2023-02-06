package com.elcom.metacen.metacensatellite.dto;

import lombok.Data;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet;

import java.io.Serializable;
import java.util.List;

public class FolderDTO implements Serializable {
    String name;
    double lat1;
    double long1;
    double lat2;
    double long2;
    double lat3;
    double long3;
    double lat4;
    double long4;

    public FolderDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat1() {
        return lat1;
    }

    public void setLat1(double lat1) {
        this.lat1 = lat1;
    }

    public double getLong1() {
        return long1;
    }

    public void setLong1(double long1) {
        this.long1 = long1;
    }

    public double getLat2() {
        return lat2;
    }

    public void setLat2(double lat2) {
        this.lat2 = lat2;
    }

    public double getLong2() {
        return long2;
    }

    public void setLong2(double long2) {
        this.long2 = long2;
    }

    public double getLat3() {
        return lat3;
    }

    public void setLat3(double lat3) {
        this.lat3 = lat3;
    }

    public double getLong3() {
        return long3;
    }

    public void setLong3(double long3) {
        this.long3 = long3;
    }

    public double getLat4() {
        return lat4;
    }

    public void setLat4(double lat4) {
        this.lat4 = lat4;
    }

    public double getLong4() {
        return long4;
    }

    public void setLong4(double long4) {
        this.long4 = long4;
    }

    public FolderDTO(String name, double lat1, double long1, double lat2, double long2, double lat3, double long3, double lat4, double long4) {
        this.name = name;
        this.lat1 = lat1;
        this.long1 = long1;
        this.lat2 = lat2;
        this.long2 = long2;
        this.lat3 = lat3;
        this.long3 = long3;
        this.lat4 = lat4;
        this.long4 = long4;
    }
    public FolderDTO(String name , String path) {
        List<String> listLatLong = List.of(path.split(" "));
        this.name = name;
        this.lat1 = Double.valueOf(listLatLong.get(0));
        this.long1 = Double.valueOf(listLatLong.get(1));
        this.lat2 = Double.valueOf(listLatLong.get(2));
        this.long2 = Double.valueOf(listLatLong.get(3));
        this.lat3 = Double.valueOf(listLatLong.get(4));
        this.long3 = Double.valueOf(listLatLong.get(5));
        this.lat4 = Double.valueOf(listLatLong.get(6));
        this.long4 = Double.valueOf(listLatLong.get(7));
    }
}
