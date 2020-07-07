package com.example.myproject;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;

import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.app.Service;

import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;


public class MyService extends Service {
    private final String BROADCAST_NAME = "com.example.myproject.unique.code";//Для работы кнопки в уведомалнии
    private final int NOTIFICATION_ID = 2;
    private final String CHANNEL_ID = "Chanel_1";//Канал для уведомлений
    private final String PHONE_HEIGHT_PREFERENCE = "PHONE_HEIGHT_PREFERENCE";
    private final String PHONE_WIDTH_PREFERENCE = "PHONE_WIDTH_PREFERENCE";
    private final String PHONE_WIDTH_AND_HEIGHT_PREFERENCE = "PHONE_WIDTH_AND_HEIGHT_PREFERENCE";

    private WindowManager windowManager;//Для работы с окнами, которые отображаются поверх всех приложений
    private WindowManager.LayoutParams params;
    private RelativeLayout buttonLayout;
    //blackBoard - основное окно приложения
    private DrawerLayout blackBoardDrawerLayout;
    private static NotificationManager notificationManager;
    private Button mButton; //Кнопка для вывода blackBoard
    private GradientDrawable drawable; // раскраска кнопки
    private int SCREEN_HEIGHT;
    private int SCREEN_WIDTH;
    private float defaultButtonAlpha = 1;

    //панель с кнопками в выдвигающемся окне
    private LinearLayout instruments;

    //для закрытия всплывающего окна
    private float MAX_DISTANCE;
    private float startX, startY;

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



        SharedPreferences sharedPreferences = getSharedPreferences(PHONE_WIDTH_AND_HEIGHT_PREFERENCE,Context.MODE_PRIVATE);
        SCREEN_HEIGHT = sharedPreferences.getInt(PHONE_HEIGHT_PREFERENCE,0);
        SCREEN_WIDTH = sharedPreferences.getInt(PHONE_WIDTH_PREFERENCE,0);

        MAX_DISTANCE = ((float) Math.pow(SCREEN_WIDTH, 2) + (float) Math.pow(SCREEN_HEIGHT, 2)) / 10;

        instruments = blackBoardDrawerLayout.findViewById(R.id.instruments);

        initReceiver();//Нужен для работы кнопки на уведомлении
    }


                                          //Экран
    @SuppressLint("ClickableViewAccessibility")
    private void addButtonOnScreen()
    {
        mButton = buttonLayout.findViewById(R.id.button);
        drawable = new GradientDrawable();
        drawable.setColor(getResources().getColor(R.color.colorAccent));
        drawable.setCornerRadius(15);
        mButton.setBackground(drawable);
        Toast.makeText(getApplicationContext(),"65468",Toast.LENGTH_SHORT).show();

        mButton.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX();
                Toast.makeText(getApplicationContext(),String.valueOf(SCREEN_WIDTH+":"+SCREEN_HEIGHT),Toast.LENGTH_SHORT).show();
                switch (event.getAction()){

                    //создаю основное окно при нажатие на левую часть экрана
                    case MotionEvent.ACTION_DOWN:
                        mButton.setAlpha(0.1f);//Делаем кнопку прозрачной
                        Toast.makeText(getApplicationContext(), "down", Toast.LENGTH_SHORT).show();
                        addBlackBoardOnScreen(Math.round(x));
                        break;

                    //меняю координаты при перемещение
                    case MotionEvent.ACTION_MOVE:
                        blackBoardDrawerLayout.setAlpha((float) x / (float) SCREEN_WIDTH);
                        instruments.setX(x - SCREEN_WIDTH * 0.9f);
                        break;

                    //при отпускание если видно больше 40%, то показываю во весь экран, иначе удаляю
                    case MotionEvent.ACTION_UP:
                        if (instruments.getX() > -0.5f * SCREEN_WIDTH)
                        {
                            blackBoardDrawerLayout.setAlpha(1);
                            instruments.setX(0);
                        }
                        else
                        {
                            windowManager.removeView(blackBoardDrawerLayout);
                            mButton.setAlpha(defaultButtonAlpha);//Возвращаем кнопке прежний цвет
                        }
                        break;
                    default:
                        break;
                }

                return false;
            }
        });


        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        params = new WindowManager.LayoutParams(
                SCREEN_WIDTH/70,
                SCREEN_HEIGHT/12,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.RGBA_8888
        );
        params.gravity = Gravity.LEFT;
        //Дефолтное положение нопки. Потом сделаем настройки и позволим пользователю самому выбирать положение
        params.verticalMargin = 0.35f;
        windowManager.addView(buttonLayout,params);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addBlackBoardOnScreen(int x)
    {
        //Для добавления окна на экран
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        int screenHeight = 0;
        int screenWidth = 0;

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            screenWidth = SCREEN_WIDTH;
            screenHeight = SCREEN_HEIGHT;
        }else
        {
            screenWidth = SCREEN_HEIGHT;
            screenHeight = SCREEN_WIDTH;
        }

        params = new WindowManager.LayoutParams(
                screenWidth,
                screenHeight,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.RGBA_8888
        );
        blackBoardDrawerLayout.setAlpha((float) x / (float) screenWidth);

        instruments.setX(-screenWidth * 0.9f + x);

        //Для отображения в полный экран
        blackBoardDrawerLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        windowManager.addView(blackBoardDrawerLayout,params);

        //закрываю всплывшее окно при движение пальцем по нему
        blackBoardDrawerLayout.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float distance;

                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        distance = (float) Math.pow(event.getX() - startX, 2) + (float)Math.pow(event.getY() - startY, 2);
                        float diagonalLength = (float) Math.pow(SCREEN_HEIGHT, 2) + (float)Math.pow(SCREEN_WIDTH, 2);

                        float alpha = 1 - (float) distance / (float) diagonalLength * 2;
                        alpha = Math.max(alpha, 0.1f);


                        blackBoardDrawerLayout.setAlpha(alpha);
                        break;

                    case MotionEvent.ACTION_UP:
                        distance = (float) Math.pow(event.getX() - startX, 2) + (float)Math.pow(event.getY()  - startY, 2);

                        if(MAX_DISTANCE < distance)
                        {
                            windowManager.removeView(blackBoardDrawerLayout);
                            mButton.setAlpha(defaultButtonAlpha);//Делаем кнопку видимой
                        }
                        else
                            blackBoardDrawerLayout.setAlpha(1);
                        break;

                    default:
                        break;
                }

                return false;
            }
        });
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
        this.stopSelf();
        System.exit(0);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
