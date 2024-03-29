package com.prashanth.doctorsearch;

import android.app.Application;
import com.prashanth.doctorsearch.dependencyInjection.AppDaggerGraph;
import com.prashanth.doctorsearch.dependencyInjection.ApplicationModule;
import com.prashanth.doctorsearch.dependencyInjection.DaggerAppDaggerGraph;
import com.prashanth.doctorsearch.dependencyInjection.NetworkDaggerModule;
import com.prashanth.doctorsearch.dependencyInjection.UtilsModule;

public class DoctorSearchBaseApplication extends Application {

    public static AppDaggerGraph component;

    protected DaggerAppDaggerGraph.Builder daggerComponent(DoctorSearchBaseApplication application) {
        return DaggerAppDaggerGraph.builder()
                .networkDaggerModule(new NetworkDaggerModule(BuildConfig.LOGIN_ENDPOINT, BuildConfig.AUTHENTICATED_ENDPOINT))
                .utilsModule(new UtilsModule(this))
                .applicationModule(new ApplicationModule(this));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        component = daggerComponent(this).build();
    }
}