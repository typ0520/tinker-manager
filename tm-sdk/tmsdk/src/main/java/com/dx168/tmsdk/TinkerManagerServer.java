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
class TinkerManagerServer {

    private static TinkerManagerServer instance;

    public static TinkerManagerServer getInstance() {
        if (instance == null) {
            instance = new TinkerManagerServer();
        }
        return instance;
    }

    public static void free() {
        instance = null;
    }

    private ITinkerManagerServer server;

    private TinkerManagerServer() {

    }

    public ITinkerManagerServer get() {
        if (server == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
            server = new Retrofit.Builder()
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .baseUrl("http://127.0.0.1/")
                    .build()
                    .create(ITinkerManagerServer.class);
        }
        return server;
    }

    public interface ITinkerManagerServer {
        @GET
        Observable<PatchInfo> queryPatch(@Url String url,
                                         @Query("appUid") String appId,
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
