package com.example.myproject.service;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;

import android.content.res.Configuration;

import android.os.Binder;
import android.os.IBinder;

import android.app.Service;

import androidx.annotation.Nullable;

import com.example.myproject.AppManager;


public class MyService extends Service {


    String currentStr;
    ClipboardManager clipboardManager;
    ClipboardManager.OnPrimaryClipChangedListener clipboardListener;
    String mPreviousText;
    AppManager appManager;
    Context context;
    NotificationForService notificationForService;

    private final IBinder mBinder = new LocalBinder();


    public MyService () {
        super();
    }

    public void initService(Context context,AppManager appManager)
    {
        this.context = context;
        this.appManager = appManager;

        currentStr = null;
        mPreviousText = "";
    }

                    //Работа сервиса
    private void addBufferListener()
    {
        clipboardManager = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        clipboardListener = new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                if(!mPreviousText.equals(clipboardManager.getPrimaryClip().getItemAt(0).getText()))
                {
                    mPreviousText = (String) clipboardManager.getPrimaryClip().getItemAt(0).getText();
                    appManager.addTextFromService(mPreviousText);
                }
            }
        };

        clipboardManager.addPrimaryClipChangedListener(clipboardListener);
    }

    private void removeBufferListener()
    {
        clipboardManager.removePrimaryClipChangedListener(clipboardListener);
    }

    public void startService()
    {
        addBufferListener();
        //Запускаем уведомление
        notificationForService = new NotificationForService(context,this);
        notificationForService.start();
    }

    public void stopService()
    {
        this.stopSelf();
        System.exit(0);
    }


    public class LocalBinder extends Binder {
        public MyService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MyService.this;
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        appManager.configurationChanged(newConfig);
    }


}
