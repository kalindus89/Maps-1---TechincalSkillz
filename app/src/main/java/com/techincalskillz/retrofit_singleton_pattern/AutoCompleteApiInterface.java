package com.techincalskillz.retrofit_singleton_pattern;

import com.techincalskillz.retrofit_singleton_pattern.response.AutoCompleteResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AutoCompleteApiInterface {

    //https://developers.google.com/maps/documentation/places/web-service/autocomplete  for all methods.

    @GET("place/autocomplete/json")
    Call<AutoCompleteResponse> getAutoCompleteResponse(
            @Query("input") String input,
            @Query("key") String key
    );
}
