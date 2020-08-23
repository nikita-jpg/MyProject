package com.example.myproject.di.module;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.example.myproject.AppManager;
import com.example.myproject.service.MyService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(includes = {ContextModule.class, AppManagerModule.class})
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
            }
        };
        return serviceConnection;
    }
}
