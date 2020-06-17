package com.example.myproject;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Intent service;
    private static final String CHANNEL_ID = "Chanel_1";

    private WindowManager windowManager;
    private NotificationManagerCompat notificationManagerCompat;
    private final int REQUEST_OF_PERMISSION = 1;
    private final int NOTIFICATION_ID = 2;
    private void setPermission()
    {
        int permissAlertDialog = ContextCompat.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + this.getPackageName()));
                startActivityForResult(intent, REQUEST_OF_PERMISSION);
            } else startService();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final NotificationManagerCompat[] notificationManagerCompat = new NotificationManagerCompat[1];

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setPermission();//Разрешения
        //createNotificationChanelIfNede();

        //Button button = findViewById(R.id.button);
        //button.setOnClickListener(this);
        //this.notificationManagerCompat = NotificationManagerCompat.from(this);



    }

    private void createNotificationChanelIfNede()
    {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,CHANNEL_ID,NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private void startService()
    {
        Intent intent = new Intent(this,MyService.class);
        //intent.setAction()
        startService(intent);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Settings.canDrawOverlays(this))
            startService();
        else
            System.exit(0);
    }

    @Override
    public void onClick(View v) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentTitle("Text")
                .setContentText("text")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);
        Notification notification = builder.build();
        int notifyId = 1;
        this.notificationManagerCompat.notify(notifyId,notification);
    }
}