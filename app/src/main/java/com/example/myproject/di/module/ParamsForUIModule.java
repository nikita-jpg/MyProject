package com.example.myproject.di.module;

import android.content.Context;

import com.example.myproject.UI.ParamsForUI;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(includes = ContextModule.class)
public class ParamsForUIModule {

    @Provides
    @Singleton
    ParamsForUI provideParamsForUI(Context context)
    {
        return new ParamsForUI(context);
    }
}
