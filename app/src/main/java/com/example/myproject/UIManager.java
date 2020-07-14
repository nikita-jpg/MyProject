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
    private RelativeLayout blackBoardDrawerLayout;//Основной лэйаут, именно он выдвигается пользователем

    private int SCREEN_HEIGHT;//Высота экрана
    private int SCREEN_WIDTH;//Ширина экрана
    private float defaultMbuttonAlpha = 1;

    private LinearLayout instruments;//панель с кнопками в выдвигающемся окне

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



        //Получаем blackBoard layout
        final Context contextThemeWrapper = new ContextThemeWrapper(context, R.style.AppTheme_NoActionBar);
        blackBoardDrawerLayout = (RelativeLayout) LayoutInflater.from(contextThemeWrapper).inflate(R.layout.activity_black_board,null);


        //Получаем данные экрана устройства из памяти устройства
        String PHONE_HEIGHT_PREFERENCE = "PHONE_HEIGHT_PREFERENCE";
        String PHONE_WIDTH_PREFERENCE = "PHONE_WIDTH_PREFERENCE";
        String PHONE_WIDTH_AND_HEIGHT_PREFERENCE = "PHONE_WIDTH_AND_HEIGHT_PREFERENCE";
        SharedPreferences sharedPreferences = context.getSharedPreferences(PHONE_WIDTH_AND_HEIGHT_PREFERENCE,Context.MODE_PRIVATE);
        SCREEN_HEIGHT = sharedPreferences.getInt(PHONE_HEIGHT_PREFERENCE,0);
        SCREEN_WIDTH = sharedPreferences.getInt(PHONE_WIDTH_PREFERENCE,0);


        //Женя, напиши тут что-нибудь :)
        MAX_DISTANCE = ((float) Math.pow(SCREEN_WIDTH, 2) + (float) Math.pow(SCREEN_HEIGHT, 2)) / 10;

        //И тут тоже :))
        instruments = blackBoardDrawerLayout.findViewById(R.id.instruments);

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
                            screenWork.addBlackBoardOnScreen(Math.round(x));
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
                                mBtn.setAlpha(defaultMbuttonAlpha);//Возвращаем кнопке прежний цвет
                            }
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


    private class ScreenWork
    {

        private void start()
        {
            addOnTouchListenerBlackBoard();
            addOrientationEventListener();
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
            blackBoardDrawerLayout.setAlpha((float) x / (float) screenWidth);

            instruments.setX(-screenWidth * 0.9f + x);

            //Для отображения в полный экран
            blackBoardDrawerLayout.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);

            windowManager.addView(blackBoardDrawerLayout,windowParams);

        }


        //работа с движением blackBoard при нажатии на mButton
        private void addOnTouchListenerBlackBoard()
        {
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
                                blackBoardDrawerLayout.setSystemUiVisibility(~View.SYSTEM_UI_FLAG_FULLSCREEN);
                                windowManager.removeView(blackBoardDrawerLayout);
                                mButton.setAlphaBtn(defaultMbuttonAlpha);//Делаем кнопку видимой
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
                    if (blackBoardDrawerLayout.isAttachedToWindow())
                        windowManager.updateViewLayout(blackBoardDrawerLayout,windowParams);
                }
            };

            if(orientationEventListener.canDetectOrientation())
                orientationEventListener.enable();
        }

    }



}
