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
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.Context.WINDOW_SERVICE;

public class UIManager
{

    private Context context;
    private WindowManager windowManager;//Для работы с окнами, которые отображаются поверх всех приложений


    private int SCREEN_HEIGHT;//Высота экрана
    private int SCREEN_WIDTH;//Ширина экрана
    private float defaultMbuttonAlpha = 1;


    private NotificatinWork notificatinWork;
    private MButtonWork mButton;//Кнопка для вывода blackBoard, mButton от mainButton
    private ScreenWork screenWork;




    //Инициализация переменных
    public void init(Context context)
    {
        this.context = context;
        windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);


        //Получаем данные экрана устройства из памяти устройства
        String PHONE_HEIGHT_PREFERENCE = "PHONE_HEIGHT_PREFERENCE";
        String PHONE_WIDTH_PREFERENCE = "PHONE_WIDTH_PREFERENCE";
        String PHONE_WIDTH_AND_HEIGHT_PREFERENCE = "PHONE_WIDTH_AND_HEIGHT_PREFERENCE";
        SharedPreferences sharedPreferences = context.getSharedPreferences(PHONE_WIDTH_AND_HEIGHT_PREFERENCE,Context.MODE_PRIVATE);
        SCREEN_HEIGHT = sharedPreferences.getInt(PHONE_HEIGHT_PREFERENCE,0);
        SCREEN_WIDTH = sharedPreferences.getInt(PHONE_WIDTH_PREFERENCE,0);


        notificatinWork = new NotificatinWork();
        mButton = new MButtonWork();
        screenWork = new ScreenWork();
    }

    public void start(MyService myService)
    {

                        //Уведомление
        //Перед запуском сервиса нужно вывести уведомление, это запретит андроиду самому выключить сервис
        notificatinWork.init(myService);
        notificatinWork.start();

                        //MButton
        mButton.init();
        mButton.start();

        screenWork.init();
        screenWork.start();

    }




    private class MButtonWork
    {
        private Button mBtn;
        private void init()
        {
            mBtn = new Button(context);
        }

        private void start()
        {
            addMbuttonOnScreen();
            addOnTouchListenerMbutton();
        }

        @SuppressLint("ClickableViewAccessibility")
        private void addMbuttonOnScreen()
        {
            // раскраска кнопки
            GradientDrawable drawable;
            drawable = new GradientDrawable();
            drawable.setColor(ContextCompat.getColor(context,R.color.colorAccent));
            drawable.setCornerRadius(15);
            mBtn.setBackground(drawable);


            //Создаём WindowManager.LayoutParams
            int type;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                type = WindowManager.LayoutParams.TYPE_PHONE;
            }

            WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
            windowParams.type = type;
            windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            windowParams.format = PixelFormat.RGBA_8888;
            windowParams.width = SCREEN_WIDTH/70;
            windowParams.height = SCREEN_HEIGHT/12;
            windowParams.gravity = Gravity.LEFT;
            //Дефолтное положение нопки. Потом сделаем настройки и позволим пользователю самому выбирать положение
            windowParams.verticalMargin = -0.2f;


            windowManager.addView(mBtn,windowParams);
        }

        @SuppressLint("ClickableViewAccessibility")
        private void addOnTouchListenerMbutton()
        {
            mBtn.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    if (event.getAction() == MotionEvent.ACTION_DOWN)
                    {
                        mBtn.setAlpha(0.1f);//Делаем кнопку прозрачной
                        screenWork.addBackgroundBlackBoardOnScreen();
                        screenWork.addBlackBoardOnScreen();
                        screenWork.dispatchTouchEvent(event);
                    }
                    else
                        screenWork.dispatchTouchEvent(event);

                    return false;
                }
            });
        }

        public void setAlphaBtn(float alpha)
        {
            mBtn.setAlpha(alpha);
        }
    }



    private class ScreenWork
    {
        RelativeLayout background;
        DrawerLayout blackBoard;//панель с кнопками в выдвигающемся окне
        NavigationView navigationView;



        //Характеристики bB, выражается в %
        int blackBoardHeightСoef;
        int blackBoardWidthСoef;

        int bBTopMarginPortCoef;
        int bBBottomMarginPortCoef;
        int bBLeftMarginPortCoef;
        int bBRightMarginPortCoef;

        int bBTopMarginLandCoef;
        int bBBottomMarginLandCoef;
        int bBLeftMarginLandCoef;
        int bBRightMarginLandCoef;

        public void init()
        {
            background = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.background_black_board,null);
            final Context contextThemeWrapper = new ContextThemeWrapper(context, R.style.AppTheme_NoActionBar);
            blackBoard = (DrawerLayout) LayoutInflater.from(contextThemeWrapper).inflate(R.layout.activity_black_board,null);
            navigationView = blackBoard.findViewById(R.id.nav);


            //Характеристики bB
            blackBoardHeightСoef = 8/10;
            blackBoardWidthСoef = 8/10;

            //Portrait Margin
            bBTopMarginPortCoef = 1/10;
            bBBottomMarginPortCoef = 1/10;
            bBLeftMarginPortCoef = 1/10;
            bBRightMarginPortCoef = 1/10;

            //Portrait Landscape
            bBTopMarginLandCoef = 1/10;
            bBBottomMarginLandCoef = 1/10;
            bBLeftMarginLandCoef = 1/10;
            bBRightMarginLandCoef = 1/10;
        }

        private void start()
        {
            addOnTouchListenerBlackBoard();
            addOrientationEventListener();
        }

        public void dispatchTouchEvent(MotionEvent event)
        {
            blackBoard.dispatchTouchEvent(event);
        }


        private void initBackground(WindowManager.LayoutParams windowParamsBackground)
        {

            //Инициализируем windowParamsBackground
            int type;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            else
                type = WindowManager.LayoutParams.TYPE_PHONE;

            int screenHeight;
            final int screenWidth;

            if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            {
                screenWidth = SCREEN_WIDTH;
                screenHeight = SCREEN_HEIGHT;
            }
            else
            {
                screenWidth = SCREEN_HEIGHT;
                screenHeight = SCREEN_WIDTH;
            }

            windowParamsBackground.type = type;
            windowParamsBackground.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
            windowParamsBackground.format = PixelFormat.RGBA_8888;
            windowParamsBackground.width = screenWidth;
            windowParamsBackground.height = screenHeight;


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                windowParamsBackground.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;


            //Инициализируем background
            final float defaultBackgroundAlpha;
            final float maxBackgroundAlpha;

            defaultBackgroundAlpha = 0.1f;
            maxBackgroundAlpha = 0.8f;

            background.setAlpha(defaultBackgroundAlpha);

            //Для отображения в полный экран
            background.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);


            //Описываем реакцию на касание
            background.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction())
                    {
                        case MotionEvent.ACTION_MOVE:
                            float alpha = (defaultBackgroundAlpha  + (event.getX()/screenWidth)*(maxBackgroundAlpha - defaultBackgroundAlpha));
                            background.setAlpha(alpha);
                            break;
                    }

                    return false;
                }
            });


            //Описываем реакцию на изменение ориентации
            OrientationEventListener orientationEventListener = new OrientationEventListener(context) {
                @Override
                public void onOrientationChanged(int orientation) {

                    //Создаём WindowManager.LayoutParams
                    WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
                    int type;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                    } else {
                        type = WindowManager.LayoutParams.TYPE_PHONE;
                    }
                    windowParams.type = type;
                    windowParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    windowParams.format = PixelFormat.RGBA_8888;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                        windowParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;



                    //Задаём размеры в соответствии с ориентацией экрана
                    int screenHeight;
                    int screenWidth;
                    if(orientation == 90 || orientation == 270)
                    {
                        windowParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                        screenWidth = SCREEN_HEIGHT;
                        screenHeight = SCREEN_WIDTH;

                    }else
                    {
                        windowParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                        screenWidth = SCREEN_WIDTH;
                        screenHeight = SCREEN_HEIGHT;
                    }
                    windowParams.width = screenWidth;
                    windowParams.height = screenHeight;

                    //Поворачиваем экран
                    if (background.isAttachedToWindow())
                        windowManager.updateViewLayout(background,windowParams);
                }
            };
            if(orientationEventListener.canDetectOrientation())
                orientationEventListener.enable();

        }

        public void addBackgroundBlackBoardOnScreen()
        {
            WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
            initBackground(windowParams);
            windowManager.addView(background,windowParams);
        }
        public void removeBackgroundBlackBoardOnScreen()
        {
            windowManager.removeView(background);
        }






        @SuppressLint("ClickableViewAccessibility")
        public void addBlackBoardOnScreen()
        {

            //Создаём WindowManager.LayoutParams
            int type;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                type = WindowManager.LayoutParams.TYPE_PHONE;
            }

            int screenHeight;
            int screenWidth;
            LinearLayout linearLayout = blackBoard.findViewById(R.id.liner);
            FrameLayout.LayoutParams layoutParams;

            if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            {
                screenWidth = SCREEN_WIDTH;
                screenHeight = SCREEN_HEIGHT;
                layoutParams = new FrameLayout.LayoutParams(screenWidth*8/10,screenHeight*8/10);
                layoutParams.topMargin = screenHeight*bBTopMarginPortCoef;
                layoutParams.leftMargin=screenWidth*bBLeftMarginPortCoef;
                layoutParams.rightMargin=screenWidth*bBRightMarginPortCoef;
            }
            else
            {
                screenWidth = SCREEN_HEIGHT;
                screenHeight = SCREEN_WIDTH;
                layoutParams = new FrameLayout.LayoutParams(screenWidth*8/10,screenHeight*8/10);
                layoutParams.topMargin = screenHeight*bBTopMarginLandCoef;
                layoutParams.leftMargin=screenWidth*bBLeftMarginLandCoef;
                layoutParams.rightMargin=screenWidth* bBRightMarginLandCoef;
            }


            WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
            windowParams.type = type;
            windowParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
            windowParams.format = PixelFormat.RGBA_8888;
            windowParams.width = screenWidth;
            windowParams.height = screenHeight;


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                windowParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            }


            //Настраиваем blackBoardLayout
            blackBoard.setAlpha(1);

            //Для отображения в полный экран
            blackBoard.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);

            linearLayout.setLayoutParams(layoutParams);
            windowManager.addView(blackBoard,windowParams);

        }


        //работа с движением blackBoard при нажатии на mButton
        @SuppressLint("ClickableViewAccessibility")
        private void addOnTouchListenerBlackBoard()
        {
            blackBoard.addDrawerListener(new DrawerLayout.DrawerListener() {
                @Override
                public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                    if(slideOffset == 0)
                    {
                        removeBackgroundBlackBoardOnScreen();
                        windowManager.removeView(blackBoard);
                        mButton.setAlphaBtn(1);
                    }
                }

                @Override
                public void onDrawerOpened(@NonNull View drawerView) {
                }

                @Override
                public void onDrawerClosed(@NonNull View drawerView) {
                }

                @Override
                public void onDrawerStateChanged(int newState) {
                }
            });
        }

        //Отвечает за смену ориентации
        private void addOrientationEventListener()
        {
            OrientationEventListener orientationEventListener = new OrientationEventListener(context) {
                @Override
                public void onOrientationChanged(int orientation) {

                    //Создаём WindowManager.LayoutParams
                    WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
                    int type;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                    } else {
                        type = WindowManager.LayoutParams.TYPE_PHONE;
                    }
                    windowParams.type = type;
                    windowParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    windowParams.format = PixelFormat.RGBA_8888;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        windowParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                    }


                    //Задаём размеры в соответствии с ориентацией экрана
                    int screenHeight;
                    int screenWidth;
                    if(orientation == 90 || orientation == 270)
                    {
                        windowParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                        screenWidth = SCREEN_HEIGHT;
                        screenHeight = SCREEN_WIDTH;

                    }else
                    {
                        windowParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                        screenWidth = SCREEN_WIDTH;
                        screenHeight = SCREEN_HEIGHT;
                    }
                    windowParams.width = screenWidth;
                    windowParams.height = screenHeight;

                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(screenWidth*8/10,screenHeight*8/10);
                    LinearLayout linearLayout = navigationView.findViewById(R.id.liner);
                    layoutParams.topMargin = screenHeight/10;
                    layoutParams.leftMargin=screenWidth/10;
                    layoutParams.rightMargin=screenWidth/10;
                    linearLayout.setLayoutParams(layoutParams);

                    //Поворачиваем экран
                    if (blackBoard.isAttachedToWindow())
                    {
                        windowManager.updateViewLayout(blackBoard, windowParams);
                    }
                }
            };

            if(orientationEventListener.canDetectOrientation())
                orientationEventListener.enable();
        }

    }




    private class NotificatinWork
    {
        private String BROADCAST_NAME = "com.example.myproject.unique.code";//Для работы кнопки в уведомалнии
        private String CHANNEL_ID = "Chanel_1";//Канал для уведомлений
        private MyService myService;

        public void init(MyService myService)
        {
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
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .addAction(R.mipmap.ic_launcher,context.getString(R.string.notify_btn_text),pendingIntent)
                    .setAutoCancel(true);
            Notification notification = builder.build();
            myService.startForeground(notificationId,notification);
        }
    }


}
