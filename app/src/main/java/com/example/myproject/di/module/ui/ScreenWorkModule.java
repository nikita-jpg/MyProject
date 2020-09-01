package com.example.myproject.di.module.ui;

import android.content.Context;

import com.example.myproject.UI.ParamsForUI;
import com.example.myproject.UI.ScreenWork;
import com.example.myproject.di.module.common.ContextModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(includes = {ContextModule.class, ParamsForUIModule.class})
public class ScreenWorkModule {

    @Provides
    @Singleton
    ScreenWork provideScreenWork(Context context, ParamsForUI paramsForUI)
    {
        ScreenWork screenWork = new ScreenWork(context,paramsForUI);
        return screenWork;
    }
}
