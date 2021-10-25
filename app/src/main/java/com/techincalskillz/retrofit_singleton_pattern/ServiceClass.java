package com.techincalskillz.retrofit_singleton_pattern;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

//calls to retrofit
public class ServiceClass {


    public static Retrofit.Builder retrofitBuilder =
            new Retrofit.Builder()
                    .baseUrl(Credentials.BASE_URl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create());//make it fast

    public static Retrofit retrofit =retrofitBuilder.build();



    private static AutoCompleteApiInterface autoCompleteApiInterface = retrofit.create(AutoCompleteApiInterface.class);

    public static  AutoCompleteApiInterface getAutoCompleteApiInterface(){
        return  autoCompleteApiInterface;
    }


}
