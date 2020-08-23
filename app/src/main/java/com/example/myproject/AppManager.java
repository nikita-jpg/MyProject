package com.example.myproject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.example.myproject.di.components.AppComponent;
import com.example.myproject.di.components.DaggerAppComponent;
import com.example.myproject.di.module.AppManagerModule;
import com.example.myproject.di.module.ContextModule;
import com.example.myproject.service.MyService;;

public class AppManager {
    AppComponent appComponent;
    Context context;
    ServiceConnection serviceConnection;


    AppManager(Context context)
    {
        this.context = context;

        appComponent = DaggerAppComponent.builder()
                .contextModule(new ContextModule(context))
                .appManagerModule(new AppManagerModule(this))
                .build();
        serviceConnection = appComponent.getServiceConnection();
    }

    public void start()
    {
        startService();
    }

    private void startService()
    {
        Intent intent = new Intent(context, MyService.class);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }


    public void addTextFromService(String text)
    {
        int b = 4;
    }
}
