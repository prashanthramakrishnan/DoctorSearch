package com.prashanth.doctorsearch;

import android.app.Application;
import timber.log.Timber;

public class DoctorSearchApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
    }
}
