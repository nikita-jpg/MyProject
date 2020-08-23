package com.example.myproject;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.myproject.service.MyService;


public class MainActivity extends AppCompatActivity
{

   private final int REQUEST_OF_PERMISSION = 1;
   private final String STATUS_BAR_HEIGHT_PREFERENCE = "STATUS_BAR_HEIGHT";
   private final String NAVIGATION_BAR_HEIGHT = "NAVIGATION_BAR_HEIGHT";
   private final String PHONE_HEIGHT_PREFERENCE = "PHONE_HEIGHT_PREFERENCE";
   private final String PHONE_WIDTH_PREFERENCE = "PHONE_WIDTH_PREFERENCE";
   private final String PHONE_WIDTH_AND_HEIGHT_PREFERENCE = "PHONE_WIDTH_AND_HEIGHT_PREFERENCE";


   private RelativeLayout relativeLayout;
   private LinearLayout linearLayout;



    private void setPermission()
    {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + this.getPackageName()));
                startActivityForResult(intent, REQUEST_OF_PERMISSION);
            }
            else
                start();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPermission();//Разрешения
    }


    private void start()
    {
        setContentView(R.layout.activity_main);
        relativeLayout = findViewById(R.id.relative);
        linearLayout = findViewById(R.id.main_layout);

        //Наш layout рисуется под bar-ами
        relativeLayout.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        //Для игнорирования чёлки
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            getWindow().getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;

        //Если у нас первый запуск, то нам нужно сохранить ширину и высоту экрана. Они нужны для работы сервиса.
        SharedPreferences sharedPreferences = getSharedPreferences(PHONE_WIDTH_AND_HEIGHT_PREFERENCE,Context.MODE_PRIVATE);
        if(!sharedPreferences.contains(PHONE_HEIGHT_PREFERENCE) || !sharedPreferences.contains(PHONE_WIDTH_PREFERENCE))//Если нет информации о высоте чёлки
        {
            //Нам нужно знать размер экрана вместе с bar-ами. Для этого мы должны отрисовать наш layout под bar-ами.
            //Тогда система пришлёт нам insets - данные о размерах bar-ов, чтобы наш UI мог сделать оступы и ,к примеру, статут бар не наезжал на пункты LinerLayout.
            //Тут то мы и получим размеры bar-ов. И,тогда, будем знать "полный" размер экрана. И сохраняем эти данеые.
            setOnApplyWindowInsetsListenerNoPreference();
            relativeLayout.requestApplyInsets();
        }
        else
        {
            //Тут мы тоже отрисовываем наш layout под bar-ами. Это красиво :)
            setOnApplyWindowInsetsListenerYesPreference();
            relativeLayout.requestApplyInsets();
            startService();
        }
    }

    private void setOnApplyWindowInsetsListenerYesPreference()
    {
        relativeLayout.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {

                getWindow().setStatusBarColor(Color.WHITE);

                    if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                    {
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                        getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                    }
                    else
                        getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(),R.color.black));

                linearLayout.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),0);
                return insets;
            }
        });
    }
    private void setOnApplyWindowInsetsListenerNoPreference()
    {
        relativeLayout.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets)
            {

                int statusBarHeight;
                int navBarHeight;
                int screenHeight;
                int screenWidth;

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

                if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                {
                    //Высота экрана = оступ свурху(чёлка) + оступ снизу(navigationBar) + высота части экрана, не занятого системным UI(то есть барами)
                    statusBarHeight = insets.getSystemWindowInsetTop();
                    navBarHeight = insets.getSystemWindowInsetBottom();
                    screenHeight = navBarHeight + statusBarHeight + displayMetrics.heightPixels;
                    screenWidth = displayMetrics.widthPixels;
                }else
                {
                    //Даже после смены орентации, displayMetrics выдаёт значения для горизонтадьного режима. Но ориентациё всё равно нужно менять
                    //Так как при наличии чёлки в горизонтально режиме ничего нет, а в вертикальном есть статус бар. Его размер = чёлке. И мы можем до него достучаться
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    //Высота экрана = оступ свурху(чёлка) + оступ снизу(navigationBar) + высота части экрана, не занятого системным UI(то есть барами)

                    //Поворот на 90 градусов по часовой стрелке
                    if(getWindowManager().getDefaultDisplay().getRotation() == Surface.ROTATION_90)
                    {
                        statusBarHeight = insets.getSystemWindowInsetRight();
                        navBarHeight = insets.getSystemWindowInsetLeft();
                    }
                    else //Поворот на 270 градусов по часовой стрелке
                    {
                        statusBarHeight = insets.getSystemWindowInsetLeft();
                        navBarHeight = insets.getSystemWindowInsetRight();
                    }

                    screenHeight = navBarHeight+statusBarHeight+displayMetrics.widthPixels;
                    screenWidth = displayMetrics.heightPixels;
                }

                linearLayout.setPadding(0,insets.getSystemWindowInsetBottom(),0,0);

                //Сохраняем данные дисплея
                SharedPreferences sharedPreferences = getSharedPreferences(PHONE_WIDTH_AND_HEIGHT_PREFERENCE, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(STATUS_BAR_HEIGHT_PREFERENCE,statusBarHeight);
                editor.putInt(NAVIGATION_BAR_HEIGHT,navBarHeight);
                editor.putInt(PHONE_HEIGHT_PREFERENCE,screenHeight);
                editor.putInt(PHONE_WIDTH_PREFERENCE,screenWidth);
                editor.apply();


                setOnApplyWindowInsetsListenerYesPreference();
                startService();
                return insets;
            }
        });
    }

    private void startService()
    {
        AppManager appManager = new AppManager(getApplicationContext());
        appManager.start();
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Settings.canDrawOverlays(this))
            start();
        else
            System.exit(0);
    }



}