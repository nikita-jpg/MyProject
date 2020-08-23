package com.example.myproject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.example.myproject.service.MyService;;

public class AppManager {
    Context context;
    MyService myService;
    ServiceConnection serviceConnection;


    AppManager(Context context)
    {
        this.context = context;

        //Работа сервиса
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MyService.LocalBinder localService = (MyService.LocalBinder) service;
                myService = localService.getService();
                myService.initService(AppManager.this.context,AppManager.this);
                myService.startService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                myService = null;
            }
        };
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
    }
}
