package com.prashanth.doctorsearch;

import com.prashanth.doctorsearch.dependencyInjection.DaggerAppDaggerGraph;
import com.prashanth.doctorsearch.dependencyInjection.NetworkDaggerModule;

public class DoctorSearchTestApplication extends DoctorSearchApplication {

    protected DaggerAppDaggerGraph.Builder daggerComponent(DoctorSearchApplication application) {
        return super.daggerComponent(this)
                .networkDaggerModule(new NetworkDaggerModule("http://localhost:8080/", "http://localhost:8080/"));
    }
}