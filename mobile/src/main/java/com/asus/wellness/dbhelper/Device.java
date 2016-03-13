package com.asus.wellness.dbhelper;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END
/**
 * Entity mapped to table device.
 */
public class Device {

    private Long id;
    private String name;
    private String blueaddr;
    private Boolean isRobin;
    private Long lastconnecttime;
    private Long stepsynctime;
    private Long ecgsynctime;

    // KEEP FIELDS - put your custom fields here
    private Long todaySteps;
    // KEEP FIELDS END

    public Device() {
    }

    public Device(Long id) {
        this.id = id;
    }

    public Device(Long id, String name, String blueaddr, Boolean isRobin, Long lastconnecttime, Long stepsynctime, Long ecgsynctime) {
        this.id = id;
        this.name = name;
        this.blueaddr = blueaddr;
        this.isRobin = isRobin;
        this.lastconnecttime = lastconnecttime;
        this.stepsynctime = stepsynctime;
        this.ecgsynctime = ecgsynctime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBlueaddr() {
        return blueaddr;
    }

    public void setBlueaddr(String blueaddr) {
        this.blueaddr = blueaddr;
    }

    public Boolean getIsRobin() {
        return isRobin;
    }

    public void setIsRobin(Boolean isRobin) {
        this.isRobin = isRobin;
    }

    public Long getLastconnecttime() {
        return lastconnecttime;
    }

    public void setLastconnecttime(Long lastconnecttime) {
        this.lastconnecttime = lastconnecttime;
    }

    public Long getStepsynctime() {
        return stepsynctime;
    }

    public void setStepsynctime(Long stepsynctime) {
        this.stepsynctime = stepsynctime;
    }

    public Long getEcgsynctime() {
        return ecgsynctime;
    }

    public void setEcgsynctime(Long ecgsynctime) {
        this.ecgsynctime = ecgsynctime;
    }

    // KEEP METHODS - put your custom methods here
    public void setTodaySteps(Long todaySteps)
    {
        this.todaySteps = todaySteps;
    }
    public Long getTodaySteps()
    {
        return todaySteps;
    }
    // KEEP METHODS END

}