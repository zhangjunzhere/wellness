package com.asus.wellness.dbhelper;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table SYSTEM_INFO.
 */
public class SystemInfo {

    private Long systemId;
    private Boolean firtuse;

    public SystemInfo() {
    }

    public SystemInfo(Long systemId) {
        this.systemId = systemId;
    }

    public SystemInfo(Long systemId, Boolean firtuse) {
        this.systemId = systemId;
        this.firtuse = firtuse;
    }

    public Long getSystemId() {
        return systemId;
    }

    public void setSystemId(Long systemId) {
        this.systemId = systemId;
    }

    public Boolean getFirtuse() {
        return firtuse;
    }

    public void setFirtuse(Boolean firtuse) {
        this.firtuse = firtuse;
    }

}
