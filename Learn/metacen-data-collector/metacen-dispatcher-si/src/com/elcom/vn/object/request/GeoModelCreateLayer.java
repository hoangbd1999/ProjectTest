package com.elcom.vn.object.request;

import java.io.Serializable;

/**
 *
 * @author Admin
 */
public class GeoModelCreateLayer implements Serializable {

    private GeoModelCreateLayerDetails coverage;

    public GeoModelCreateLayer(GeoModelCreateLayerDetails coverage) {
        this.coverage = coverage;
    }

    /**
     * @return the coverage
     */
    public GeoModelCreateLayerDetails getCoverage() {
        return coverage;
    }

    /**
     * @param coverage the coverage to set
     */
    public void setCoverage(GeoModelCreateLayerDetails coverage) {
        this.coverage = coverage;
    }
}
