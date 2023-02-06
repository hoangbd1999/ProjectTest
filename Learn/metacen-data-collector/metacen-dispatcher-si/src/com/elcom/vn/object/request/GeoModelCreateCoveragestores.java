package com.elcom.vn.object.request;

import java.io.Serializable;

/**
 *
 * @author Admin
 */
public class GeoModelCreateCoveragestores implements Serializable {

    private GeoModelCreateCoveragestoresDetails coverageStore;

    public GeoModelCreateCoveragestores(GeoModelCreateCoveragestoresDetails coverageStore) {
        this.coverageStore = coverageStore;
    }
    
    /**
     * @return the coverageStore
     */
    public GeoModelCreateCoveragestoresDetails getCoverageStore() {
        return coverageStore;
    }

    /**
     * @param coverageStore the coverageStore to set
     */
    public void setCoverageStore(GeoModelCreateCoveragestoresDetails coverageStore) {
        this.coverageStore = coverageStore;
    }
}
