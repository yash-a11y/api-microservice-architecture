package com.yash.api_rogers.models;


import com.fasterxml.jackson.annotation.JsonProperty;

public class Plan {
    private String planType;

    @JsonProperty("planname")
    private String planname;

    private String details;

    @JsonProperty("planprice")
    private String planprice;

    // Constructors, Getters, and Setters
    public Plan() {

    }

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public String getPlanName() {
        return planname;
    }

    public void setPlanName(String planName) {
        this.planname = planName;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getPrice() {
        return planprice;
    }

    public void setPrice(String price) {
        this.planprice = price;
    }

    public Plan(String planType, String planname, String details, String planprice) {
        this.planType = planType;
        this.planname = planname;
        this.details = details;
        this.planprice = planprice;
    }

}
