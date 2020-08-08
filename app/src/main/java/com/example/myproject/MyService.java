package com.example.myproject;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;

import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;

import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.app.Service;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.drawerlayout.widget.DrawerLayout;


public class MyService extends Service {

    UIManager uiManager;
    String currentStr;

    public void onCreate()
    {
        currentStr = null;

        uiManager = new UIManager();
        uiManager.init(this);
        uiManager.start(this);

        startScanBuffer();
    }



                    //Работа сервиса
    private void startScanBuffer()
    {
        final ClipboardManager clipboardManager;
        clipboardManager = (ClipboardManager) this.getSystemService(CLIPBOARD_SERVICE);

        clipboardManager.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                uiManager.addText("" + clipboardManager.getPrimaryClip().getItemAt(0).getText());
            }
        });

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
