package com.asus.wellness.dbhelper;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table location_change.
 */
public class Location_change {

    private Long id;
    private Long get_location_time;
    private Double latitude;
    private Double longitude;
    private String district;

    public Location_change() {
    }

    public Location_change(Long id) {
        this.id = id;
    }

    public Location_change(Long id, Long get_location_time, Double latitude, Double longitude, String district) {
        this.id = id;
        this.get_location_time = get_location_time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.district = district;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGet_location_time() {
        return get_location_time;
    }

    public void setGet_location_time(Long get_location_time) {
        this.get_location_time = get_location_time;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

}
