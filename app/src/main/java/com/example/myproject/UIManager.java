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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.Context.WINDOW_SERVICE;

public class UIManager
{

    private Context context;


    //Экран
    private WindowManager windowManager;//Для работы с окнами, которые отображаются поверх всех приложений
    //blackBoard - основное окно приложения


    private int SCREEN_HEIGHT;//Высота экрана
    private int SCREEN_WIDTH;//Ширина экрана
    private float defaultMbuttonAlpha = 1;


    //для закрытия всплывающего окна
    private float MAX_DISTANCE;
    private float startX, startY;

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


        //Женя, напиши тут что-нибудь :)
        MAX_DISTANCE = ((float) Math.pow(SCREEN_WIDTH, 2) + (float) Math.pow(SCREEN_HEIGHT, 2)) / 10;


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
            windowParams.verticalMargin = 0.35f;


            windowManager.addView(mBtn,windowParams);
        }

        private void addOnTouchListenerMbutton()
        {
            mBtn.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    float x = event.getX();
                    switch (event.getAction()){

                        //создаю основное окно при нажатие на левую часть экрана
                        case MotionEvent.ACTION_DOWN:
                            mBtn.setAlpha(0.1f);//Делаем кнопку прозрачной
                            screenWork.addBackgroundBlackBoardOnScreen();
                            screenWork.addBlackBoardOnScreen(4);
                            break;

                        //меняю координаты при перемещение
                        case MotionEvent.ACTION_MOVE:
                            screenWork.dispatchTouchEvent(event);
                            //blackBoardDrawerLayout.setAlpha((float) x / (float) SCREEN_WIDTH);
                            //instruments.setX(x - SCREEN_WIDTH * 0.9f);
                            break;

                        //при отпускание если видно больше 40%, то показываю во весь экран, иначе удаляю
                        case MotionEvent.ACTION_UP:
                            screenWork.dispatchTouchEvent(event);
                            /*
                            if (instruments.getX() > -0.5f * SCREEN_WIDTH)
                            {
                               blackBoardDrawerLayout.setAlpha(1);
                                instruments.setX(0);
                            }
                            else
                            {
                                windowManager.removeView(blackBoardDrawerLayout);
                                mBtn.setAlpha(defaultMbuttonAlpha);//Возвращаем кнопке прежний цвет
                            }

                             */
                            break;
                        default:
                            break;
                    }
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
        LinearLayout blackBoard;//панель с кнопками в выдвигающемся окне

        float defaultBackgroundAlpha;
        float maxBackgroundAlpha;


        public void init()
        {
            background = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.background_black_board,null);
            blackBoard = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.activity_black_board,null);

            defaultBackgroundAlpha = 0.1f;
            maxBackgroundAlpha = 0.8f;
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


        public void addBackgroundBlackBoardOnScreen()
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

            WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
            windowParams.type = type;
            windowParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
            windowParams.format = PixelFormat.RGBA_8888;
            windowParams.width = screenWidth;
            windowParams.height = screenHeight;


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                windowParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            }

            background.setAlpha(defaultBackgroundAlpha);

            windowManager.addView(background,windowParams);

        }


        @SuppressLint("ClickableViewAccessibility")
        public void addBlackBoardOnScreen(int x)
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
            blackBoard.setAlpha((float) x / (float) screenWidth);

            blackBoard.setX(-screenWidth * 0.9f + x);

            //Для отображения в полный экран
            blackBoard.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);

            windowManager.addView(blackBoard,windowParams);

        }


        //работа с движением blackBoard при нажатии на mButton
        @SuppressLint("ClickableViewAccessibility")
        private void addOnTouchListenerBlackBoard()
        {
            blackBoard.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    float distance;
                    float screenWidth;

                    if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                        screenWidth = SCREEN_WIDTH;
                    else
                        screenWidth = SCREEN_HEIGHT;


                    switch (event.getAction())
                    {
                        case MotionEvent.ACTION_MOVE:
                            float alpha = (defaultBackgroundAlpha  + (event.getX()/screenWidth)*(maxBackgroundAlpha - defaultBackgroundAlpha));

                            background.setAlpha(alpha);
                            break;

                        case MotionEvent.ACTION_UP:
                            distance = (float) Math.pow(event.getX() - startX, 2) + (float)Math.pow(event.getY()  - startY, 2);

                            if(event.getX() > 0.5f * screenWidth)
                            {
                                background.setAlpha(maxBackgroundAlpha);
                            }
                            else
                            {
                                blackBoard.setSystemUiVisibility(~View.SYSTEM_UI_FLAG_FULLSCREEN);
                                windowManager.removeView(background);
                                mButton.setAlphaBtn(defaultMbuttonAlpha);//Делаем кнопку видимой
                            }

                            break;
                        default:
                            break;
                    }
                    return false;
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


                    //Поворачиваем экран
                    if (blackBoard.isAttachedToWindow())
                        windowManager.updateViewLayout(blackBoard,windowParams);
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
