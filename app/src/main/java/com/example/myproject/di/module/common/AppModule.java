package com.example.myproject.di.module.common;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.example.myproject.AppManager;
import com.example.myproject.Cache.CacheManager;
import com.example.myproject.UI.MButton;
import com.example.myproject.UI.ParamsForUI;
import com.example.myproject.UI.ScreenWork;
import com.example.myproject.UI.UIManager;
import com.example.myproject.di.module.ui.MButtonModule;
import com.example.myproject.di.module.ui.ParamsForUIModule;
import com.example.myproject.di.module.ui.ScreenWorkModule;
import com.example.myproject.service.MyService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(includes = {ContextModule.class, ParamsForUIModule.class, AppManagerModule.class, MButtonModule.class, ScreenWorkModule.class})
public class AppModule {
    MyService myService;

    @Provides
    @Singleton
    ServiceConnection providerServiceConnection(final Context context, final AppManager appManager)
    {
        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MyService.LocalBinder localService = (MyService.LocalBinder) service;
                myService = localService.getService();
                myService.initService(context, appManager);
                myService.startService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                myService = null;
                appManager.stopApp();
            }
        };
        return serviceConnection;
    }

    @Provides
    @Singleton
    UIManager provideUIManager(Context context, MButton mButton, ParamsForUI paramsForUI, ScreenWork screenWork)
    {
        UIManager uiManager = new UIManager(context,mButton,paramsForUI,screenWork);
        mButton.setUiManager(uiManager);
        screenWork.setUiManager(uiManager);
        return uiManager;
    }

    @Provides
    @Singleton
    CacheManager provideCacheManager(Context context)
    {
        CacheManager cacheManager = new CacheManager(context);
        return cacheManager;
    }

}
