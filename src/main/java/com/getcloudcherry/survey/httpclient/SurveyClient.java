package com.getcloudcherry.survey.httpclient;


import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.getcloudcherry.survey.helper.SurveyCC;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SurveyClient {
    public static final String BASE_URL = "https://api.getcloudcherry.com/api/";
    private static API api;

    static {
        setupRestClient();
    }

    private SurveyClient() {
    }

    private static void setupRestClient() {
        HttpLoggingInterceptor aLogging = new HttpLoggingInterceptor();
        // set your desired log level
        aLogging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient aClient = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).retryOnConnectionFailure(false)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        // Request customization: add request headers
                        Request.Builder requestBuilder = original.newBuilder().header("Connection", "close").header("Authorization", "Bearer " + "") // <-- this is the important line
                                .method(original.method(), original.body());
                        Request request = requestBuilder.build();
                        Response response = chain.proceed(request);
                        boolean unAuthorized = (response.code() == 401);
                        if (unAuthorized) {
                            postMessage("Session Expired");
                        }
                        return response;
                    }
                }).addInterceptor(aLogging).build();

        Retrofit aRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(aClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = aRetrofit.create(API.class);
    }

    public static API get() {
        return api;
    }

    private static final Handler handler = new Handler(Looper.getMainLooper());

    public static void postMessage(final String iMessage) {
        handler.post(
                new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SurveyCC.getInstance().getContext(), iMessage, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
}