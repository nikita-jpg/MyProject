package com.example.myproject.di.module.ui;

import android.content.Context;

import com.example.myproject.UI.ParamsForUI;
import com.example.myproject.di.module.common.ContextModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(includes = ContextModule.class)
public class ParamsForUIModule {
    @Provides
    @Singleton
    ParamsForUI provideParamsForUi(Context context)
    {
        ParamsForUI paramsForUI = new ParamsForUI(context);
        return paramsForUI;
    }
}
