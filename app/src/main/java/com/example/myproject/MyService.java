package com.example.myproject;

import android.content.ClipboardManager;
import android.content.Intent;

import android.content.res.Configuration;

import android.os.IBinder;

import android.app.Service;

import androidx.annotation.Nullable;


public class MyService extends Service {

    UIManager uiManager;
    String currentStr;
    ClipboardManager clipboardManager;
    ClipboardManager.OnPrimaryClipChangedListener clipboardLisener;
    String mPreviousText;

    
    public void onCreate()
    {
        currentStr = null;

        uiManager = new UIManager();
        uiManager.init(this);
        uiManager.start(this);
        mPreviousText = "";
        clipboardManager = (ClipboardManager) this.getSystemService(CLIPBOARD_SERVICE);
        clipboardLisener = new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                if(!mPreviousText.equals(clipboardManager.getPrimaryClip().getItemAt(0).getText()))
                {
                    mPreviousText = (String) clipboardManager.getPrimaryClip().getItemAt(0).getText();
                    uiManager.addText("" + clipboardManager.getPrimaryClip().getItemAt(0).getText());
                }
            }
        };
        addBufferListener();
    }



                    //Работа сервиса
    private void addBufferListener()
    {
        clipboardManager.addPrimaryClipChangedListener(clipboardLisener);
    }
    private void remBufferListener()
    {
        clipboardManager.removePrimaryClipChangedListener(clipboardLisener);
    }


    public void stopService()
    {
        this.stopSelf();
        System.exit(0);
    }

    @Nullable

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        uiManager.configurationChanged(newConfig);
    }


}
