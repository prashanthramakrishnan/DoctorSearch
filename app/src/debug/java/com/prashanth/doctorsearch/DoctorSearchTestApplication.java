package com.prashanth.doctorsearch;

import com.prashanth.doctorsearch.dependencyInjection.DaggerAppDaggerGraph;
import com.prashanth.doctorsearch.dependencyInjection.NetworkDaggerModule;

public class DoctorSearchTestApplication extends DoctorSearchBaseApplication {

    protected DaggerAppDaggerGraph.Builder daggerComponent(DoctorSearchBaseApplication application) {
        return super.daggerComponent(this)
                .networkDaggerModule(new NetworkDaggerModule("http://localhost:8080/", "http://localhost:8080/"));
    }
}