package com.th_koeln.steve.klamottenverteiler;

/**
 * Created by Frank on 15.01.2018.
 */

public class Request {

    private String name;
    private String art;
    private String size;
    private String brand;



    private String status;

    public Request(String name, String art, String size, String brand, String status) {
        this.name = name;
        this.art = art;
        this.size = size;
        this.brand = brand;
        this.status = status;
    }




    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArt() {
        return art;
    }

    public void setArt(String art) {
        this.art = art;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }




}
