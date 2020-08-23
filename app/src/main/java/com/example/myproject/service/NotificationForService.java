package com.example.myproject.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.myproject.R;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationForService
{

    private String BROADCAST_NAME = "com.example.myproject.unique.code";//Для работы кнопки в уведомалнии
    private String CHANNEL_ID = "Chanel_1";//Канал для уведомлений
    Context context;
    MyService myService;


    public NotificationForService(Context context, MyService myService)
    {
        this.context = context;
        this.myService = myService;
    }

    public void start()
    {
        initReceiver();
        createNotificationChanelIfNede();
        startNotify();
    }

    //Нужен для работы кнопки на уведомлении
    private void initReceiver()
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_NAME);
        context.registerReceiver(new MyReceiver(),intentFilter);
    }
    //Принимает широковещательное сообщение от кнопки в уведомлении
    public class MyReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            myService.stopForeground(true);
            myService.stopService();
        }
    }

    private void createNotificationChanelIfNede()
    {
        NotificationManager notificationManager;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,CHANNEL_ID,NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
    private void startNotify()
    {
        int notificationId = 2;
        Intent intent = new Intent();
        intent.setAction(BROADCAST_NAME);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,CHANNEL_ID)
                .setSmallIcon(R.drawable.ar)
                .setContentTitle(context.getString(R.string.notify_title))
                .setContentText(context.getString(R.string.notify_text))
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .addAction(R.mipmap.ic_launcher,context.getString(R.string.notify_btn_text),pendingIntent)
                .setAutoCancel(true);
        android.app.Notification notification = builder.build();
        myService.startForeground(notificationId,notification);
    }
}
