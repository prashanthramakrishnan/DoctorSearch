package com.prashanth.doctorsearch;

import android.app.Application;
import com.prashanth.doctorsearch.dependencyInjection.AppDaggerGraph;
import com.prashanth.doctorsearch.dependencyInjection.ApplicationModule;
import com.prashanth.doctorsearch.dependencyInjection.DaggerAppDaggerGraph;
import com.prashanth.doctorsearch.dependencyInjection.NetworkDaggerModule;
import com.prashanth.doctorsearch.dependencyInjection.UtilsModule;
import timber.log.Timber;

public class DoctorSearchApplication extends Application {

    public static AppDaggerGraph component;

    public DaggerAppDaggerGraph.Builder daggerComponent(DoctorSearchApplication application) {
        return DaggerAppDaggerGraph.builder()
                .networkDaggerModule(new NetworkDaggerModule(BuildConfig.LOGIN_ENDPOINT, BuildConfig.AUTHENTICATED_ENDPOINT))
                .utilsModule(new UtilsModule(this))
                .applicationModule(new ApplicationModule(this));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        component = daggerComponent(this).build();
        Timber.plant(new Timber.DebugTree());
    }
}