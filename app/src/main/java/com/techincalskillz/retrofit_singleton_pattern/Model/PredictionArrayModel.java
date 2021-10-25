package com.techincalskillz.retrofit_singleton_pattern.Model;

public class PredictionArrayModel {
    String description;
    String place_id;

    public PredictionArrayModel(String description, String place_id) {
        this.description = description;
        this.place_id = place_id;
    }

    public String getDescription() {
        return description;
    }

    public String getPlace_id() {
        return place_id;
    }
}
