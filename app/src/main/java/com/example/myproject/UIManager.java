package com.example.myproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import static android.content.Context.WINDOW_SERVICE;

public class UIManager
{

    private Context context;


    //Уведомления
    String BROADCAST_NAME = "com.example.myproject.unique.code";//Для работы кнопки в уведомалнии
    String CHANNEL_ID = "Chanel_1";//Канал для уведомлений

    //Экран
    private WindowManager windowManager;//Для работы с окнами, которые отображаются поверх всех приложений
    //blackBoard - основное окно приложения
    private RelativeLayout blackBoardDrawerLayout;//Основной лэйаут, именно он выдвигается пользователем
    private Button mButton; //Кнопка для вывода blackBoard, mButton от mainButton

    private int SCREEN_HEIGHT;//Высота экрана
    private int SCREEN_WIDTH;//Ширина экрана
    private float defaultMbuttonAlpha = 1;

    private LinearLayout instruments;//панель с кнопками в выдвигающемся окне

    //для закрытия всплывающего окна
    private float MAX_DISTANCE;
    private float startX, startY;


    //Инициализация переменных
    public void init(Context context)
    {
        this.context = context;
        windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        mButton = new Button(context);


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


        //Нужен для работы кнопки на уведомлении
    }



}
