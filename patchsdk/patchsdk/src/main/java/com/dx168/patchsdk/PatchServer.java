package com.dx168.patchsdk;

import com.dx168.patchsdk.bean.PatchInfo;

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
class PatchServer {

    private static PatchServer instance;

    static void free() {
        instance = null;
    }

    private IPatchServer server;

    private PatchServer() {

    }

    static void init(String baseUrl) {
        if (instance == null) {
            instance = new PatchServer();
            OkHttpClient client = new OkHttpClient.Builder()
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
            instance.server = new Retrofit.Builder()
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .baseUrl(baseUrl)
                    .build()
                    .create(IPatchServer.class);
        }

    }

    static IPatchServer get() {
        if (instance == null) {
            throw new NullPointerException("PatchServer must be init before using");
        }
        return instance.server;
    }

    public interface IPatchServer {
        @GET("api/patch")
        Observable<PatchInfo> queryPatch(@Query("appUid") String appId,
                                         @Query("token") String token,
                                         @Query("tag") String tag,
                                         @Query("versionName") String versionName,
                                         @Query("versionCode") int versionCode,
                                         @Query("platform") String platform,
                                         @Query("osVersion") String osVersion,
                                         @Query("model") String model,
                                         @Query("channel") String channel,
                                         @Query("sdkVersion") String sdkVersion,
                                         @Query("deviceId") String deviceId);

        @GET
        Observable<ResponseBody> downloadFile(@Url String fileUrl);


        @GET("api/report")
        Observable<Void> report(@Query("appUid") String appId,
                                @Query("token") String token,
                                @Query("tag") String tag,
                                @Query("versionName") String versionName,
                                @Query("versionCode") int versionCode,
                                @Query("platform") String platform,
                                @Query("osVersion") String osVersion,
                                @Query("model") String model,
                                @Query("channel") String channel,
                                @Query("sdkVersion") String sdkVersion,
                                @Query("deviceId") String deviceId,
                                @Query("patchUid") String patchUid,
                                @Query("applyResult") boolean applyResult);
    }

}
