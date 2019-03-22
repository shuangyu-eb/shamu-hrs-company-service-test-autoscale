package com.tardisone.companyservice.dto;

public class NormalObjectDto {
    private String fid;
    private Object value;

    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
