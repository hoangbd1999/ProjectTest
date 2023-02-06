package com.elcom.metacen.raw.data.service;

import com.elcom.metacen.raw.data.model.dto.ObjectTripDTO;

import java.util.concurrent.CompletableFuture;

public interface ObjectTripService {
    CompletableFuture<ObjectTripDTO> findPositionOfShip(Integer mmsi, String startTime,
                                                        String toTime, Integer limit);
}
