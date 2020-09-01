package com.example.myproject.di.module.common;

import com.example.myproject.AppManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppManagerModule {
    AppManager appManager;

    public AppManagerModule(AppManager appManager)
    {
        this.appManager = appManager;
    }

    @Provides
    @Singleton
    AppManager provideAppManager()
    {
        return appManager;
    }
}
