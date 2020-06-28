package com.example.myproject;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;

import android.os.Build;
import android.os.IBinder;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.app.Service;

import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;



public class MyService extends Service {
    private final String BROADCAST_NAME = "com.example.myproject.unique.code";//Для работы кнопки в уведомалнии
    private final int NOTIFICATION_ID = 2;
    private final String CHANNEL_ID = "Chanel_1";//Канал для уведомлений

    private WindowManager windowManager;//Для работы с окнами, которые отображаются поверх всех приложений
    private WindowManager.LayoutParams params;
    private RelativeLayout buttonLayout;
    //blackBoard - основное окно приложения
    private DrawerLayout blackBoardDrawerLayout;
    private RelativeLayout blackBoardLayout;
    private static NotificationManager notificationManager;
    private int screenHeight;
    private int screenWidth;


    public void onCreate()
    {
        init();
        //Перед запуском сервиса нужно вывести уведомление, это запретит андроиду самому выключить сервис
        createNotificationChanelIfNede();
        startNotify();
        addButtonOnScreen();
    }

    //Инициализация переменных
    private void init()
    {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        buttonLayout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.activity_button,null);
        final Context contextThemeWrapper = new ContextThemeWrapper(this, R.style.AppTheme_NoActionBar);
        blackBoardDrawerLayout = (DrawerLayout) LayoutInflater.from(contextThemeWrapper).inflate(R.layout.activity_black_board,null);
        blackBoardLayout = (RelativeLayout) LayoutInflater.from(contextThemeWrapper).inflate(R.layout.test,null);
        screenHeight = getScreenHeight();
        screenWidth = getScreenWidth();

        initReceiver();//Нужен для работы кнопки на уведомлении
    }
    private int getScreenWidth()
    {
        final Display display = windowManager.getDefaultDisplay();
        return display.getWidth();
    }
    private int getScreenHeight()
    {
        final Display display = windowManager.getDefaultDisplay();
        int statusBarHeight = 0;
        int navigBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            navigBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        return display.getHeight()+statusBarHeight+2*navigBarHeight;
    }

                                          //Экран
    private void addButtonOnScreen()
    {
        Button mButton = buttonLayout.findViewById(R.id.button);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(getResources().getColor(R.color.colorAccent));
        drawable.setCornerRadius(15);
        mButton.setBackground(drawable);
        mButton.setText("C");
        mButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 45);//надо фиксить
        mButton.setPadding(0,-17,0,0);
        Toast.makeText(getApplicationContext(),"65468",Toast.LENGTH_SHORT).show();
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = "No text";
                //if(clipboardManager.hasPrimaryClip())
                //text = ""+clipboardManager.getPrimaryClip().getItemAt(0).getText();
                //Toast.makeText(getApplicationContext(),"65468",Toast.LENGTH_SHORT).show();
                addBlackBoardOnScreen();
            }
        });


        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        params = new WindowManager.LayoutParams(
                screenWidth/10,
                screenHeight/2,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.RGBA_8888
        );
        params.gravity = Gravity.RIGHT | Gravity.TOP;
        params.horizontalMargin = (float) 0.05;
        params.verticalMargin = (float) 0.25;
        windowManager.addView(buttonLayout,params);
    }

    private void addBlackBoardOnScreen()
    {
        //Для добавления окна на экран
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }
        params = new WindowManager.LayoutParams(
                screenWidth,
                screenHeight,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.RGBA_8888
        );

        // Лэйаут, нак отором поисходят основные действие (пока добавляется кнопка)
        NavigationView navigationView = blackBoardDrawerLayout.findViewById(R.id.nav_view);
        navigationView.addView(blackBoardLayout);


        //Для отображения в полный экран
        blackBoardDrawerLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
        windowManager.addView(blackBoardDrawerLayout,params);
    }


                                          //Уведомления
    private void initReceiver()
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_NAME);
        registerReceiver(new MyReceiver(),intentFilter);
    }
    //Принимает широковещательное сообщение от кнопки в уведомлении
    public class MyReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopForeground(true);
            stopService();
        }
    }

    private void createNotificationChanelIfNede()
    {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,CHANNEL_ID,NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
    private void startNotify()
    {
        Intent intent = new Intent();
        intent.setAction(BROADCAST_NAME);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.notify_title))
                .setContentText(getString(R.string.notify_text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .addAction(R.mipmap.ic_launcher,getString(R.string.notify_btn_text),pendingIntent)
                .setAutoCancel(true);
        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID,notification);

    }


    
                                           //Работа сервиса
    private void stopService()
    {
        //Дописать
        System.exit(0);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
