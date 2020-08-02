package com.example.myproject;

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
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.Context.WINDOW_SERVICE;

public class UIManager
{

    private Context context;
    private WindowManager windowManager;//Для работы с окнами, которые отображаются поверх всех приложений


    private int SCREEN_HEIGHT;//Высота экрана
    private int SCREEN_WIDTH;//Ширина экрана
    private int STATUS_BAR_HEIGHT;
    private int NAV_BAR_HEIGHT;

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
        String STATUS_BAR_HEIGHT_PREFERENCE = "STATUS_BAR_HEIGHT";
        String NAVIGATION_BAR_HEIGHT = "NAVIGATION_BAR_HEIGHT";
        String PHONE_WIDTH_AND_HEIGHT_PREFERENCE = "PHONE_WIDTH_AND_HEIGHT_PREFERENCE";

        SharedPreferences sharedPreferences = context.getSharedPreferences(PHONE_WIDTH_AND_HEIGHT_PREFERENCE,Context.MODE_PRIVATE);
        SCREEN_HEIGHT = sharedPreferences.getInt(PHONE_HEIGHT_PREFERENCE,0);
        SCREEN_WIDTH = sharedPreferences.getInt(PHONE_WIDTH_PREFERENCE,0);
        STATUS_BAR_HEIGHT = sharedPreferences.getInt(STATUS_BAR_HEIGHT_PREFERENCE,0);
        NAV_BAR_HEIGHT = sharedPreferences.getInt(NAVIGATION_BAR_HEIGHT,0);

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

    public void configurationChanged(Configuration newConfig)
    {
        mButton.orientationChangedBtn();
        screenWork.configurationChanged(newConfig);
    }


    private class MButtonWork
    {
        private Button mBtn;
        private int btnGravitySide;
        private int btnWidth;
        private int btnHeight;
        private float btnVerticalMargin;
        private int btnColor;
        private int btnCornerRadius;
        private int btnX;
        private int btnY;


        private void init()
        {
            mBtn = new Button(context);
            btnWidth = SCREEN_WIDTH/30;
            btnHeight = SCREEN_HEIGHT/10;
            btnGravitySide = Gravity.LEFT;
            btnColor = ContextCompat.getColor(context,R.color.black);
            btnCornerRadius = 15;
            btnVerticalMargin = 0.8f;
            btnY = 0;
            btnX = 0;
        }

        private void initBtn(WindowManager.LayoutParams windowParams)
        {

                            //Работа с самой кнопкой

            // раскраска кнопки
            GradientDrawable drawable;
            drawable = new GradientDrawable();
            drawable.setColor(btnColor);
            drawable.setCornerRadius(btnCornerRadius);
            mBtn.setBackground(drawable);


            mBtn.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    screenWork.dispatchTouchEvent(event);

                    if(event.getAction() == MotionEvent.ACTION_DOWN)
                    {
                        mBtn.setAlpha(1);//Делаем кнопку прозрачной
                        screenWork.showBackground();
                        screenWork.showbB();
                    }
                    return false;
                }
            });

                            //Работа с WindowManager.LayoutParams
            int type;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            else
                type = WindowManager.LayoutParams.TYPE_PHONE;


            windowParams.type = type;
            windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            windowParams.format = PixelFormat.RGBA_8888;
            windowParams.width = btnWidth;
            windowParams.height = btnHeight;

            updateCoord();
            windowParams.x = btnX;
            windowParams.y = btnY;
        }

        private void updateCoord()
        {
            int statusBarHeight = 0;
            int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
            }

            if(context.getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            {
                if(btnGravitySide == Gravity.RIGHT)
                    btnX = SCREEN_WIDTH/2;
                else
                    btnX = -SCREEN_WIDTH/2;

                btnY = (int) ((SCREEN_HEIGHT-NAV_BAR_HEIGHT-statusBarHeight)*(btnVerticalMargin-0.5));
            }
            else
            {
                if(btnGravitySide == Gravity.RIGHT)
                    btnX = (SCREEN_HEIGHT-STATUS_BAR_HEIGHT-NAV_BAR_HEIGHT)/2;
                else
                    btnX = -(SCREEN_HEIGHT-STATUS_BAR_HEIGHT-NAV_BAR_HEIGHT)/2;

                btnY = (int) ((SCREEN_WIDTH-statusBarHeight)*(btnVerticalMargin-0.5f));
            }
        }

        private void addMbuttonOnScreen()
        {
            WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
            initBtn(windowParams);
            windowManager.addView(mBtn,windowParams);
        }

        //не нужно
        private void orientationChangedBtn()
        {
            WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
            initBtn(windowParams);
            windowManager.updateViewLayout(mBtn,windowParams);

        }



        public void show()
        {
            mBtn.setAlpha(1);
        }

        public int getBtnGravity()
        {
           return btnGravitySide;
        }

        public int getBtnWidth() {
            return btnWidth;
        }

        public int getBtnHeight() {
            return btnHeight;
        }

        public int getBtnX() {
            return btnX;
        }

        public float getBtnVerticalMargin() {
            return btnVerticalMargin;
        }

        public int getBtnY() {

            return btnY;
        }


        public int getBtnColor() {
            return btnColor;
        }

        public int getBtnCornerRadius() {
            return btnCornerRadius;
        }

    }



    private class ScreenWork
    {
        RelativeLayout background;
        MotionLayout blackBoard;//панель с кнопками в выдвигающемся окне


        //Характеристики background
        float minBackgroundAlpha;
        float maxBackgroundAlpha;

        //Характеристики bB, выражается в %
        float bBHeightСoefPort;
        float bBWidthСoefPort;
        float bBHeightСoefLand;
        float bBWidthСoefLand;

        float peepbB;

        float bBTopMarginPortCoef;
        float bBBottomMarginPortCoef;
        float bBLeftMarginPortCoef;
        float bBRightMarginPortCoef;

        float bBTopMarginLandCoef;
        float bBBottomMarginLandCoef;
        float bBLeftMarginLandCoef;
        float bBRightMarginLandCoef;

        int bBgravity;


        boolean finger;
        float startXLiner;

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
            bBHeightСoefPort = 0.8f;
            bBWidthСoefPort = 0.8f;

            bBHeightСoefLand = 0.8f;
            bBWidthСoefLand = 0.8f;

            peepbB = 0.1f;

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
                bBgravity = Gravity.LEFT;
            else
                bBgravity = Gravity.RIGHT;


            screenWork.addBackgroundbBOnScreen();
            screenWork.addBlackBoardOnScreen();
            screenWork.hideBackground();
            screenWork.hidebB();


            startXLiner = 0;
            finger = false;

        }

        public void configurationChanged(Configuration newConfig)
        {
            if(newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            {
                orientationChangedBackground(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }else
            {
                orientationChangedBackground(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
            orientationChangedbB();

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

        private void orientationChangedBackground(int orientation)
        {

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
                if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    //windowParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    screenWidth = SCREEN_WIDTH;
                    screenHeight = SCREEN_HEIGHT;

                } else {
                    //windowParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    screenWidth = SCREEN_HEIGHT;
                    screenHeight = SCREEN_WIDTH;
                }
                windowParams.width = screenWidth;
                windowParams.height = screenHeight;

                //Поворачиваем экран
                if (background.isAttachedToWindow())
                    windowManager.updateViewLayout(background, windowParams);
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
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
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
            blackBoard.dispatchTouchEvent(event);
        }

        private void initBb(WindowManager.LayoutParams windowParams)
        {
            LinearLayout linearLayout = blackBoard.findViewById(R.id.liner);
            Button button = linearLayout.findViewById(R.id.button5);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateStartCoord();
                }
            });

            if (context.getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            {
                windowParams.width = SCREEN_WIDTH;
                windowParams.height = SCREEN_HEIGHT;
            } else {
                windowParams.width = SCREEN_HEIGHT;
                windowParams.height = SCREEN_WIDTH;
            }

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



            int bBHeight;
            int bBWidth;
            int marginForMoving;
            int marginTop;
            int side;
            if(bBgravity == Gravity.RIGHT)
            {
                blackBoard.loadLayoutDescription(R.xml.right_to_left);
                side = ConstraintSet.START;
            }
            else {
                blackBoard.loadLayoutDescription(R.xml.left_to_right);
                side = ConstraintSet.END;
            }

            ConstraintSet constraintSet = blackBoard.getConstraintSet(R.id.start);
            if(context.getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            {
                bBWidth = (int) (SCREEN_WIDTH * bBWidthСoefPort)/100;
                bBHeight = (int) (SCREEN_HEIGHT * bBHeightСoefPort)/100;

                marginForMoving = (int) (SCREEN_WIDTH + bBWidth*(1-peepbB));

            }
            else
            {
                bBWidth = (int) (SCREEN_HEIGHT * bBWidthСoefLand)/100;
                bBHeight = (int) (SCREEN_WIDTH * bBHeightСoefLand)/100;

                marginForMoving = (int) (SCREEN_HEIGHT + bBWidth*(1-peepbB));

            }


            constraintSet.constrainWidth(R.id.liner, mButton.getBtnWidth());
            constraintSet.constrainHeight(R.id.liner, mButton.getBtnHeight());
            updateStartCoord();


            constraintSet = blackBoard.getConstraintSet(R.id.end);
            constraintSet.constrainWidth(R.id.liner, bBWidth*100);
            constraintSet.constrainHeight(R.id.liner, bBHeight*100);




                            //Настраиваем сам DrawerLayout
            blackBoard.setAlpha(1);
            //blackBoard.setBackgroundColor(Color.RED);

            //Для отображения в полный экран
            blackBoard.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }

        public void updateStartCoord()
        {
            int statusBarHeight = 0;
            int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
            }

            int navBarHeight = 0;
            resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                navBarHeight = context.getResources().getDimensionPixelSize(resourceId);
            }



            ConstraintSet constraintSet = blackBoard.getConstraintSet(R.id.start);


            constraintSet.clear(R.id.liner,ConstraintSet.TOP);
            constraintSet.clear(R.id.liner,ConstraintSet.START);
            constraintSet.clear(R.id.liner,ConstraintSet.END);

            float z = mButton.getBtnVerticalMargin();
            int x = SCREEN_WIDTH;
            int y;

            if(context.getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            {
                y = (int) ((SCREEN_HEIGHT-statusBarHeight-NAV_BAR_HEIGHT)*z-NAV_BAR_HEIGHT+statusBarHeight);

                if(bBgravity == Gravity.RIGHT)
                    constraintSet.connect(R.id.liner,ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END,0);
                else
                    constraintSet.connect(R.id.liner,ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START,0);

                constraintSet.connect(R.id.liner,ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP,y);

            }
            else
            {
                y = (int) ((SCREEN_WIDTH-statusBarHeight)*z-statusBarHeight);

                if(bBgravity == Gravity.RIGHT)
                    constraintSet.connect(R.id.liner,ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START,SCREEN_HEIGHT-NAV_BAR_HEIGHT-STATUS_BAR_HEIGHT-mButton.getBtnWidth());
                else
                    constraintSet.connect(R.id.liner,ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START,0);

                constraintSet.connect(R.id.liner,ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP,y);
            }



            int b;
            if(context.getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
               b = 4;
            else
                b = 5;

        }

        public void addBlackBoardOnScreen()
        {
            WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
            initBb(windowParams);
            windowManager.addView(blackBoard,windowParams);
        }

        public void orientationChangedbB()
        {
            //Создаём WindowManager.LayoutParams
            WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
            initBb(windowParams);

            //Поворачиваем экран
            if (blackBoard.isAttachedToWindow())
                windowManager.updateViewLayout(blackBoard, windowParams);
        }

        public void hidebB()
        {
            blackBoard.setVisibility(View.GONE);
        }
        public void showbB()
        {
            updateStartCoord();
            startXLiner = blackBoard.findViewById(R.id.liner).getX();
            blackBoard.setVisibility(View.VISIBLE);
            //Для отображения в полный экран
            blackBoard.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            test();
        }

        private void test()
        {
            blackBoard.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    float a  = blackBoard.findViewById(R.id.liner).getX();
                    switch (event.getAction())
                    {

                        case MotionEvent.ACTION_UP:
                            if(event.getEventTime()-event.getDownTime()<400)
                                screenWork.forcedOpenbB();
                            else if(a == startXLiner)
                            {
                                hidebB();
                                hideBackground();
                                mButton.show();
                            }
                    }
                    return false;
                }
            });


            blackBoard.addTransitionListener(new MotionLayout.TransitionListener() {
                @Override
                public void onTransitionStarted(MotionLayout motionLayout, int i, int i1) {
                }

                @Override
                public void onTransitionChange(MotionLayout motionLayout, int i, int i1, float v) {

                    slideBackground(v);
                    if(v == 0 && !finger)
                    {
                        hidebB();
                        hideBackground();
                        mButton.show();
                    }

                }

                @Override
                public void onTransitionCompleted(MotionLayout motionLayout, int i) {
                }

                @Override
                public void onTransitionTrigger(MotionLayout motionLayout, int i, boolean b, float v) {

                }
            });
        }



        public void forcedOpenbB()
        {
            //MotionScene.Transition transition =  blackBoard.getTransition(R.id.bBTransition);
            blackBoard.transitionToEnd();
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
