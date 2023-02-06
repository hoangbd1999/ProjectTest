package com.elcom.vn.object.request;

import java.io.Serializable;

/**
 *
 * @author Admin
 */
public class GeoModelCreateCoveragestoresDetails implements Serializable {
    
    private String workspace;
    private String name;
    private String type;
    private String extension;
    private boolean enabled;
    private boolean __default__;
    private String url;

    public GeoModelCreateCoveragestoresDetails(String workspace, String name, String type, String extension, boolean enabled, boolean __default__, String url) {
        this.workspace = workspace;
        this.name = name;
        this.type = type;
        this.extension = extension;
        this.enabled = enabled;
        this.__default__ = __default__;
        this.url = url;
    }
    
    /**
     * @return the workspace
     */
    public String getWorkspace() {
        return workspace;
    }

    /**
     * @param workspace the workspace to set
     */
    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the extension
     */
    public String getExtension() {
        return extension;
    }

    /**
     * @param extension the extension to set
     */
    public void setExtension(String extension) {
        this.extension = extension;
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return the __default__
     */
    public boolean isDefault__() {
        return __default__;
    }

    /**
     * @param __default__ the __default__ to set
     */
    public void setDefault__(boolean __default__) {
        this.__default__ = __default__;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }
}
