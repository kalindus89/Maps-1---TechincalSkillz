package com.techincalskillz.retrofit_singleton_pattern.response;

import com.google.gson.annotations.SerializedName;
import com.techincalskillz.retrofit_singleton_pattern.Model.PredictionArrayModel;

import java.util.List;

public class AutoCompleteResponse {
    @SerializedName("status")
    String status;
    @SerializedName("predictions")
    List<PredictionArrayModel> predictions;

    public AutoCompleteResponse(String status, List<PredictionArrayModel> predictions) {
        this.status = status;
        this.predictions = predictions;
    }

    public String getStatus() {
        return status;
    }

    public List<PredictionArrayModel> getPredictions() {
        return predictions;
    }
}
