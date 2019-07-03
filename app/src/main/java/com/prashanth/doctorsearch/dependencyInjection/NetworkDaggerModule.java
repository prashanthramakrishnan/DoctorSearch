package com.prashanth.doctorsearch.dependencyInjection;

import android.app.Application;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.prashanth.doctorsearch.BuildConfig;
import com.prashanth.doctorsearch.network.DoctorSearchAPI;
import com.prashanth.doctorsearch.storage.LoginSharedPreferences;
import dagger.Module;
import dagger.Provides;
import java.util.concurrent.TimeUnit;
import javax.inject.Named;
import javax.inject.Singleton;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class NetworkDaggerModule {

    private String loginUrl;

    private String loggedInUrl;

    private static final int TIMEOUT = 10;

    public static final String AUTHENTICATED = "AUTHENTICATED";

    public static final String LOGIN = "LOGIN";

    public NetworkDaggerModule(String loginUrl, String loggedInUrl) {
        this.loggedInUrl = loggedInUrl;
        this.loginUrl = loginUrl;
    }

    @Provides
    @Singleton
    Cache provideHttpCache(Application application) {
        int cacheSize = 30 * 1024 * 1024;
        return new Cache(application.getCacheDir(), cacheSize);
    }

    @Provides
    @Singleton
    OkHttpClient provideOkhttpClient(Cache cache, LoginSharedPreferences loginSharedPreferences) {
        HttpLoggingInterceptor debugInterceptor = new HttpLoggingInterceptor();
        debugInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.cache(cache);
        client.followRedirects(true);
        client.followSslRedirects(true);
        client.retryOnConnectionFailure(true);
        client.connectTimeout(TIMEOUT, TimeUnit.SECONDS);
        client.readTimeout(TIMEOUT, TimeUnit.SECONDS);
        client.connectTimeout(TIMEOUT, TimeUnit.SECONDS);
        client.addInterceptor(debugInterceptor);
        client.addInterceptor(chain -> {
            Request request = chain.request();
            if ((BuildConfig.LOGIN_ENDPOINT + "oauth/token").equals((chain.request().url().toString()))) {
                request = request.newBuilder()
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .addHeader("Accept", "application/json")
                        .addHeader("Authorization", "Basic " + "aXBob25lOmlwaG9uZXdpbGxub3RiZXRoZXJlYW55bW9yZQ==")
                        .build();
            } else {
                request = request.newBuilder()
                        .addHeader("Accept", "application/json")
                        .addHeader("Authorization", "Bearer " + loginSharedPreferences.getAccessToken())
                        .build();
            }
            return chain.proceed(request);
        });
        return client.build();
    }

    @Provides
    @Singleton
    @Named(LOGIN)
    Retrofit provideLoginRetrofitBuilder(Gson gson, OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(loginUrl)
                .client(okHttpClient)
                .build();
    }

    @Provides
    @Singleton
    @Named(AUTHENTICATED)
    Retrofit provideAuthenticatedRetrofitBuilder(Gson gson, OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(loggedInUrl)
                .client(okHttpClient)
                .build();
    }

    @Provides
    @Singleton
    Gson provideGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        return gsonBuilder.create();
    }

    @Provides
    @Singleton
    @Named(LOGIN)
    DoctorSearchAPI provideLoginAPI(@Named(LOGIN) Retrofit retrofit) {
        return retrofit.create(DoctorSearchAPI.class);
    }

    @Provides
    @Singleton
    @Named(AUTHENTICATED)
    DoctorSearchAPI provideAuthenticatedAPI(@Named(AUTHENTICATED) Retrofit retrofit) {
        return retrofit.create(DoctorSearchAPI.class);
    }

}