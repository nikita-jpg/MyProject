package com.example.myproject.di.module.ui;

import android.content.Context;

import com.example.myproject.UI.MButton;
import com.example.myproject.UI.ParamsForUI;
import com.example.myproject.di.module.common.ContextModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(includes = {ContextModule.class, ParamsForUIModule.class})
public class MButtonModule {

    @Provides
    @Singleton
    MButton provideMButton(Context context,ParamsForUI paramsForUI)
    {
        MButton mButton = new MButton(context,paramsForUI);
        return mButton;
    }
}
