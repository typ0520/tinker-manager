package com.dx168.tmsdk;

import com.dx168.tmsdk.bean.PatchInfo;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created by jianjun.lin on 16/7/14.
 */
class TinkerManagerHttpService {

    private static ITinkerManagerHttpService service;

    public static ITinkerManagerHttpService get() {
        if (service == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .readTimeout(40, TimeUnit.SECONDS)
                    .build();

            service = new Retrofit.Builder()
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .baseUrl("http://hotfix.dx168.com/")
                    .build()
                    .create(ITinkerManagerHttpService.class);
        }
        return service;
    }

    public interface ITinkerManagerHttpService {

        @GET("/hotfix-apis/api/patch")
        Observable<PatchInfo> queryPatch(@Query("appUid") String appId,
                                         @Query("token") String token,
                                         @Query("tag") String tag,
                                         @Query("versionName") String versionName,
                                         @Query("versionCode") int versionCode,
                                         @Query("platform") String platform,
                                         @Query("osVersion") String osVersion,
                                         @Query("model") String model,
                                         @Query("sdkVersion") String sdkVersion);

        @GET
        Observable<ResponseBody> downloadFile(@Url String fileUrl);
    }

}
