package com.example.myproject;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import com.example.myproject.UI.UIManager;
import com.example.myproject.di.components.AppComponent;
import com.example.myproject.di.components.DaggerAppComponent;
import com.example.myproject.di.module.common.AppManagerModule;
import com.example.myproject.di.module.common.ContextModule;
import com.example.myproject.service.MyService;;

public class AppManager {
    AppComponent appComponent;
    Context context;
    private ServiceConnection serviceConnection;
    private UIManager uiManager;


    AppManager(Context context)
    {
        this.context = context;

        appComponent = DaggerAppComponent.builder()
                .contextModule(new ContextModule(context))
                .appManagerModule(new AppManagerModule(this))
                .build();
        serviceConnection = appComponent.getServiceConnection();
        uiManager = appComponent.getUIManager();
    }

    public void start()
    {
        startService();
        uiManager.start();
    }

    private void startService()
    {
        Intent intent = new Intent(context, MyService.class);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void stopApp()
    {
        //Так же сервис выключается через задницу
    }

    public void addTextFromService(String text)
    {
        uiManager.addText(text);
    }
}
