package com.lis.simpleretrofit.api;


import com.lis.simpleretrofit_library.annotation.Field;
import com.lis.simpleretrofit_library.annotation.GET;
import com.lis.simpleretrofit_library.annotation.POST;
import com.lis.simpleretrofit_library.annotation.Query;

import okhttp3.Call;


public interface SimpleWeatherApi {

    @POST("/v3/weather/weatherInfo")
    Call postWeather(@Field("city") String city, @Field("key") String key);


    @GET("/v3/weather/weatherInfo")
    Call getWeather(@Query("city") String city, @Query("key") String key);
}
