package com.example.mobilprogramlamahavadurumuuygulamasi;

public class WeatherRVModel {

    private String temperature;
    private String icon;
    private String date;

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) { this.date = date; }

    public WeatherRVModel(String temperature, String icon, String date) {
        this.temperature = temperature;
        this.icon = icon;
        this.date = date.substring(5);
    }
}
