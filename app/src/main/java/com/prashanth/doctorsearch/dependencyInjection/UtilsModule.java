package com.prashanth.doctorsearch.dependencyInjection;

import android.content.Context;
import com.prashanth.doctorsearch.storage.LoginSharedPreferences;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module
public class UtilsModule {

    private Context context;

    public UtilsModule(Context context) {
        this.context = context;
    }

    @Provides
    @Singleton
    LoginSharedPreferences provideLoginSharedPreferences() {
        return new LoginSharedPreferences(context);
    }

}