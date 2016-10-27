package com.ytx.hotfix;

import com.ytx.hotfix.bean.AppInfo;
import com.ytx.hotfix.bean.PatchInfo;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created by jianjun.lin on 16/7/14.
 */
public class HotFixService {


    private static IHotFixService sHotFixService;

    public static IHotFixService get() {
        if (sHotFixService == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .readTimeout(40, TimeUnit.SECONDS)
                    .build();

            sHotFixService = new Retrofit.Builder()
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .baseUrl("http://test.v.dx168.com/")
                    .build()
                    .create(IHotFixService.class);
        }
        return sHotFixService;
    }

    public interface IHotFixService {

        @GET("http://test.v.dx168.com/live-apis/api/app/zp/plan/getUserTradePlan")
        Observable<PatchInfo> queryPatch(@Body AppInfo appInfo);

        @GET
        Observable<ResponseBody> downloadFile(@Url String fileUrl);
    }


}
