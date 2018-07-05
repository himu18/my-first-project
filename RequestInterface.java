package com.talent4assure.retrofit_get.Interface;

import com.talent4assure.retrofit_get.Model.Android;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;

/**
 * Created by krishm on 4/2/2018.
 */

public interface RequestInterface {
    @GET("android/jsonarray/")
    Observable<List<Android>> register();
}