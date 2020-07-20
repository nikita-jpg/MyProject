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
import android.graphics.Color;
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
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.constraintlayout.solver.state.State;
import androidx.constraintlayout.widget.ConstraintSet;
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
    private int STATUS_BAR_HEIGHT;


    private NotificatinWork notificatinWork;
    private MButtonWork mButton;//Кнопка для вывода blackBoard, mButton от mainButton
    private ScreenWork screenWork;

    private boolean flag = false;

    //Инициализация переменных
    public void init(Context context)
    {
        this.context = context;
        windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);


        //Получаем данные экрана устройства из памяти устройства
        String PHONE_HEIGHT_PREFERENCE = "PHONE_HEIGHT_PREFERENCE";
        String PHONE_WIDTH_PREFERENCE = "PHONE_WIDTH_PREFERENCE";
        String STATUS_BAR_HEIGHT_PREFERENCE = "STATUS_BAR_HEIGHT";
        String PHONE_WIDTH_AND_HEIGHT_PREFERENCE = "PHONE_WIDTH_AND_HEIGHT_PREFERENCE";

        SharedPreferences sharedPreferences = context.getSharedPreferences(PHONE_WIDTH_AND_HEIGHT_PREFERENCE,Context.MODE_PRIVATE);
        SCREEN_HEIGHT = sharedPreferences.getInt(PHONE_HEIGHT_PREFERENCE,0);
        SCREEN_WIDTH = sharedPreferences.getInt(PHONE_WIDTH_PREFERENCE,0);
        STATUS_BAR_HEIGHT = sharedPreferences.getInt(STATUS_BAR_HEIGHT_PREFERENCE,0);

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
        mButton.addMbuttonOnScreen();

        screenWork.init();

    }




    private class MButtonWork
    {
        private Button mBtn;
        private int btnGravity;
        private void init()
        {
            mBtn = new Button(context);
            btnGravity = Gravity.LEFT;
        }

        private void initBtn(WindowManager.LayoutParams windowParams)
        {

                            //Работа с самой кнопкой

            // раскраска кнопки
            GradientDrawable drawable;
            drawable = new GradientDrawable();
            drawable.setColor(ContextCompat.getColor(context,R.color.colorAccent));
            drawable.setCornerRadius(15);
            mBtn.setBackground(drawable);


            mBtn.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    screenWork.dispatchTouchEvent(event);
                    switch (event.getAction())
                    {
                        case MotionEvent.ACTION_DOWN:
                            mBtn.setAlpha(0.1f);//Делаем кнопку прозрачной
                            screenWork.showBackground();
                            screenWork.showbB();
                            break;

                        case MotionEvent.ACTION_MOVE:
                            flag = true;
                            break;

                        case MotionEvent.ACTION_UP:
                            if(!flag)
                                if(event.getEventTime()-event.getDownTime()<400);
                            screenWork.forcedOpenbB();
                    }
                    return false;
                }
            });


                            //Работа с WindowManager.LayoutParams


            int type;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                type = WindowManager.LayoutParams.TYPE_PHONE;
            }

            windowParams.type = type;
            windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            windowParams.format = PixelFormat.RGBA_8888;
            windowParams.width = SCREEN_WIDTH/30;
            windowParams.height = SCREEN_HEIGHT/10;
            windowParams.gravity = btnGravity;
            //Дефолтное положение нопки. Потом сделаем настройки и позволим пользователю самому выбирать положение
            windowParams.verticalMargin = -0.2f;


        }


        private void addMbuttonOnScreen()
        {
            WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
            initBtn(windowParams);
            windowManager.addView(mBtn,windowParams);
        }


        public void showBtnonScreen()
        {
            mBtn.setAlpha(1);
        }

        public int getBtnGravity()
        {
           return btnGravity;
        }
    }



    private class ScreenWork
    {
        RelativeLayout background;
        MotionLayout blackBoard;//панель с кнопками в выдвигающемся окне
        NavigationView navigationView;

        int tekOrieantationBackground = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        int tekOrieantationbB = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        //Характеристики background
        float minBackgroundAlpha;
        float maxBackgroundAlpha;

        //Характеристики bB, выражается в %
        float blackBoardHeightСoef;
        float blackBoardWidthСoef;

        float bBTopMarginPortCoef;
        float bBBottomMarginPortCoef;
        float bBLeftMarginPortCoef;
        float bBRightMarginPortCoef;

        float bBTopMarginLandCoef;
        float bBBottomMarginLandCoef;
        float bBLeftMarginLandCoef;
        float bBRightMarginLandCoef;

        int bBgravity;


        public void init()
        {
            background = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.background_black_board,null);
            final Context contextThemeWrapper = new ContextThemeWrapper(context, R.style.AppTheme_NoActionBar);
            blackBoard = (MotionLayout) LayoutInflater.from(contextThemeWrapper).inflate(R.layout.activity_black_board,null);
            //navigationView = blackBoard.findViewById(R.id.nav);

            //Прозрачность background
            minBackgroundAlpha = 0.1f;
            maxBackgroundAlpha = 0.8f;


            //Характеристики bB
            blackBoardHeightСoef = 0.8f;
            blackBoardWidthСoef = 0.8f;

            //Portrait Margin
            bBTopMarginPortCoef = 0.1f;
            bBBottomMarginPortCoef = 0.1f;
            bBLeftMarginPortCoef = 0.1f;
            bBRightMarginPortCoef = 0.1f;

            //Portrait Landscape
            bBTopMarginLandCoef = 0.1f;
            bBBottomMarginLandCoef = 0.1f;
            bBLeftMarginLandCoef = 0.1f;
            bBRightMarginLandCoef = 0.1f;

            if(mButton.getBtnGravity() == Gravity.LEFT)
                bBgravity = Gravity.START;
            else
                bBgravity = Gravity.END;

            screenWork.addBackgroundbBOnScreen();
            screenWork.addBlackBoardOnScreen();
            screenWork.hideBackground();
            screenWork.hidebB();

        }


        //Рабоа с фоном
        private void initBackground(WindowManager.LayoutParams windowParamsBackground)
        {

            //Инициализируем windowParams для background
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
            background.setBackgroundColor(Color.BLACK);
            background.setAlpha(minBackgroundAlpha);

            //Для отображения в полный экран
            background.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);



            //Описываем реакцию на изменение ориентации
            OrientationEventListener orientationEventListener = new OrientationEventListener(context) {
                @Override
                public void onOrientationChanged(int orientation) {
                    if ( (orientation == 0 || orientation == 90 || orientation == 180 || orientation == 270) && orientation!= tekOrieantationBackground ) {

                        tekOrieantationBackground = orientation;

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
                        if (orientation == 90 || orientation == 270) {
                            windowParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                            screenWidth = SCREEN_HEIGHT;
                            screenHeight = SCREEN_WIDTH;

                        } else {
                            windowParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                            screenWidth = SCREEN_WIDTH;
                            screenHeight = SCREEN_HEIGHT;
                        }
                        windowParams.width = screenWidth;
                        windowParams.height = screenHeight;

                        //Поворачиваем экран
                        if (background.isAttachedToWindow())
                            windowManager.updateViewLayout(background, windowParams);
                    }
                }
            };
            if(orientationEventListener.canDetectOrientation())
                orientationEventListener.enable();

        }

        private void slideBackground(float slideOffset)
        {
            background.setAlpha(minBackgroundAlpha + (maxBackgroundAlpha-minBackgroundAlpha)*slideOffset);
        }

        public void addBackgroundbBOnScreen()
        {
            WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
            initBackground(windowParams);
            windowManager.addView(background,windowParams);
        }

        private void forceMaxAlphaBackground()
        {
            background.setAlpha(maxBackgroundAlpha);
        }


        public void hideBackground()
        {
            background.setVisibility(View.GONE);
        }
        public void showBackground()
        {
            background.setAlpha(minBackgroundAlpha);
            background.setVisibility(View.VISIBLE);
            //Для отображения в полный экран
            background.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }


        //Если пользователь остановил приложение. Дописать
        public void removeBackgroundbBOnScreen()
        {
            background.setBackgroundColor(ContextCompat.getColor(context,R.color.transparent));
            windowManager.removeView(background);
        }




        //Работа с bB
        public void dispatchTouchEvent(MotionEvent event)
        {
            /*
            if(flag)
                blackBoard.dispatchTouchEvent(event);
            else
            {
                flag = true;
                event.offsetLocation(50,50);
            }

             */

            blackBoard.dispatchTouchEvent(event);
        }

        private void initBb(WindowManager.LayoutParams windowParams)
        {

                            //bB = DrawerLayout + LinerLayout


                            //Работа с DrawerLayout
            //Инициализируем WindowManager.LayoutParams для DrawerLayout
            int screenHeight;
            int screenWidth;


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

            int type;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            else
                type = WindowManager.LayoutParams.TYPE_PHONE;


            windowParams.type = type;
            windowParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
            windowParams.format = PixelFormat.RGBA_8888;
            windowParams.width = screenWidth;
            windowParams.height = screenHeight;


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                windowParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;




                            //Настраиваем сам LinerLayout
            LinearLayout linearLayout = blackBoard.findViewById(R.id.liner);
            MotionLayout.LayoutParams layoutParams;


            if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            {
                layoutParams = new MotionLayout.LayoutParams(screenWidth*8/10,screenHeight*8/10);
                layoutParams.topMargin = (int)(screenHeight*bBTopMarginPortCoef);
                layoutParams.leftMargin = (int)(screenWidth*bBLeftMarginPortCoef);
                layoutParams.rightMargin = (int)(screenWidth*bBRightMarginPortCoef);
            }
            else
            {
                layoutParams = new MotionLayout.LayoutParams(screenWidth*8/10,screenHeight*8/10);
                layoutParams.topMargin = (int)(screenHeight*bBTopMarginLandCoef);
                layoutParams.leftMargin = (int)(screenWidth*bBLeftMarginLandCoef);
                layoutParams.rightMargin = (int)(screenWidth*bBRightMarginLandCoef);
            }
            linearLayout.setLayoutParams(layoutParams);

            ConstraintSet constraintSet = blackBoard.getConstraintSet(R.id.start);

            constraintSet.constrainWidth(R.id.liner,screenWidth/3);
            constraintSet.constrainHeight(R.id.liner,screenHeight/3);

            constraintSet = blackBoard.getConstraintSet(R.id.end);
            constraintSet.constrainWidth(R.id.liner,screenWidth/3);
            constraintSet.constrainHeight(R.id.liner,screenHeight/3);





                            //Настраиваем сам DrawerLayout
            blackBoard.setAlpha(1);

            //Для отображения в полный экран
            blackBoard.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);


            /*
            blackBoard.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction())
                    {
                        case MotionEvent.ACTION_DOWN:
                            showbB();
                            break;
                    }

                    return false;
                }
            });

             */


            OrientationEventListener orientationEventListener = new OrientationEventListener(context) {
                @Override
                public void onOrientationChanged(int orientation) {

                    //Поворачиваем DrawerLayout

                    if ( (orientation == 0 || orientation == 90 || orientation == 180 || orientation == 270) && orientation!= tekOrieantationbB ) {
                        tekOrieantationbB = orientation;
                        //Создаём WindowManager.LayoutParams
                        WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
                        int type;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                        else
                            type = WindowManager.LayoutParams.TYPE_PHONE;

                        windowParams.type = type;
                        windowParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
                        windowParams.format = PixelFormat.RGBA_8888;

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                            windowParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;


                        //Задаём размеры в соответствии с ориентацией экрана
                        int screenHeight;
                        int screenWidth;
                        if (orientation == 90 || orientation == 270) {
                            windowParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                            screenWidth = SCREEN_HEIGHT;
                            screenHeight = SCREEN_WIDTH;

                        } else {
                            windowParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                            screenWidth = SCREEN_WIDTH;
                            screenHeight = SCREEN_HEIGHT;
                        }
                        windowParams.width = screenWidth;
                        windowParams.height = screenHeight;

                        //Поворачиваем экран
                        if (blackBoard.isAttachedToWindow())
                            windowManager.updateViewLayout(blackBoard, windowParams);


                        ConstraintSet constraintSet = blackBoard.getConstraintSet(R.id.start);

                        constraintSet.constrainWidth(R.id.liner, screenWidth / 3);
                        constraintSet.constrainHeight(R.id.liner, screenHeight / 3);

                        constraintSet = blackBoard.getConstraintSet(R.id.end);
                        constraintSet.constrainWidth(R.id.liner, screenWidth / 3);
                        constraintSet.constrainHeight(R.id.liner, screenHeight / 3);


                    /*
                    //Поворачиваем LinerLayout
                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(screenWidth*8/10,screenHeight*8/10);
                    LinearLayout linearLayout = navigationView.findViewById(R.id.liner);
                    layoutParams.topMargin = screenHeight/10;
                    layoutParams.leftMargin=screenWidth/10;
                    layoutParams.rightMargin=screenWidth/10;
                    linearLayout.setLayoutParams(layoutParams);

                     */
                    }
                }
            };

            if(orientationEventListener.canDetectOrientation())
                orientationEventListener.enable();




                            //Настраиваем NavigatiomView в DrawerLayout

            //Сторона, с которой выезжает bB совпадает со стороной кнопки
            //DrawerLayout.LayoutParams layoutParamsNav = new DrawerLayout.LayoutParams(screenWidth,screenHeight);
           // layoutParamsNav.gravity = bBgravity;
            //navigationView.setLayoutParams(layoutParamsNav);



        }

        public void addBlackBoardOnScreen()
        {
            WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
            initBb(windowParams);
            windowManager.addView(blackBoard,windowParams);
        }

        public void forcedOpenbB()
        {
            forceMaxAlphaBackground();
        }
        public void hidebB()
        {

            blackBoard.setVisibility(View.GONE);
        }
        public void showbB()
        {
            blackBoard.setVisibility(View.VISIBLE);
            //Для отображения в полный экран
            blackBoard.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }

        //Если пользователь остановил приложение. Дописать
        public void removebBOnScreen()
        {
            blackBoard.setSystemUiVisibility(~View.SYSTEM_UI_FLAG_FULLSCREEN);
            screenWork.hideBackground();
            windowManager.removeView(blackBoard);
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
