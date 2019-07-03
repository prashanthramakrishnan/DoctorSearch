package com.prashanth.doctorsearch.dependencyInjection;

import com.prashanth.doctorsearch.MainActivity;
import com.prashanth.doctorsearch.adapter.DoctorSearchRecyclerViewAdapter;
import com.prashanth.doctorsearch.ui.LoginActivity;
import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(modules = {NetworkDaggerModule.class, UtilsModule.class, ApplicationModule.class})
public interface AppDaggerGraph {

    void inject(LoginActivity loginActivity);

    void inject(MainActivity mainActivity);

    void inject(DoctorSearchRecyclerViewAdapter doctorSearchRecyclerViewAdapter);

}