package com.example.myproject;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity
{

   private final int REQUEST_OF_PERMISSION = 1;
   private final String PHONE_HEIGHT_PREFERENCE = "PHONE_HEIGHT_PREFERENCE";
   private final String PHONE_WIDTH_PREFERENCE = "PHONE_WIDTH_PREFERENCE";
   private final String PHONE_WIDTH_AND_HEIGHT_PREFERENCE = "PHONE_WIDTH_AND_HEIGHT_PREFERENCE";
    private void setPermission()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + this.getPackageName()));
                startActivityForResult(intent, REQUEST_OF_PERMISSION);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setPermission();//Разрешения
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final RelativeLayout relativeLayout = findViewById(R.id.relative);
        final LinearLayout linearLayout = findViewById(R.id.main_layout);

        relativeLayout.setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        SharedPreferences sharedPreferences = getSharedPreferences(PHONE_WIDTH_AND_HEIGHT_PREFERENCE,Context.MODE_PRIVATE);
        if(!sharedPreferences.contains(PHONE_HEIGHT_PREFERENCE) || !sharedPreferences.contains(PHONE_WIDTH_PREFERENCE))//Если нет информации о высоте чёлки
        {
            relativeLayout.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View v, WindowInsets insets)
                {
                    //Сохраняем текущую ориентацию, чтобы потом понять, нужно ли её менять после сохранения данных
                    Boolean landscapeOrientation;

                    if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                        landscapeOrientation = true;
                    else
                        landscapeOrientation = false;

                    int screenHeight = 0;
                    int screenWidth = 0;

                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

                    if(!landscapeOrientation)
                    {
                        //Высота экрана = оступ свурху(чёлка) + оступ снизу(navigationBar) + высота части экрана, не занятого системным UI(то есть барами)
                        screenHeight = insets.getSystemWindowInsetBottom()+insets.getSystemWindowInsetTop()+displayMetrics.heightPixels;
                        screenWidth = displayMetrics.widthPixels;
                    }else
                    {
                        //Даже после смены орентации, displayMetrics выдаёт значения для горизонтадьного режима. Но ориентациё всё равно нужно менять
                        //Так как при наличии чёлки в горизонтально режиме ничего нет, а в вертикальном есть статус бар. Его размер = чёлке. И мы можем до него достучаться
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        //Высота экрана = оступ свурху(чёлка) + оступ снизу(navigationBar) + высота части экрана, не занятого системным UI(то есть барами)
                        screenHeight = insets.getSystemWindowInsetLeft()+insets.getSystemWindowInsetRight()+displayMetrics.widthPixels;
                        screenWidth = displayMetrics.heightPixels;
                    }

                    linearLayout.setPadding(0,insets.getSystemWindowInsetBottom(),0,0);


                    SharedPreferences sharedPreferences = getSharedPreferences(PHONE_WIDTH_AND_HEIGHT_PREFERENCE, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt(PHONE_HEIGHT_PREFERENCE,screenHeight);
                    editor.apply();

                    SharedPreferences sharedPreferences2 = getSharedPreferences(PHONE_WIDTH_AND_HEIGHT_PREFERENCE, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor2 = sharedPreferences2.edit();
                    editor2.putInt(PHONE_WIDTH_PREFERENCE,screenWidth);
                    editor2.apply();


                    startService();
                    relativeLayout.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                        @Override
                        public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {

                            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                                getWindow().setNavigationBarColor(Color.BLACK);
                            else {
                                getWindow().setStatusBarColor(Color.BLACK);
                                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                            }

                            linearLayout.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),0);
                            return insets;
                        }
                    });
                    relativeLayout.requestApplyInsets();
                    return insets;
                }
            });
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            }
            relativeLayout.requestApplyInsets();
        }else
        {
            relativeLayout.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {

                    if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                        getWindow().setNavigationBarColor(Color.BLACK);
                    else {
                        getWindow().setStatusBarColor(Color.BLACK);
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    }

                    linearLayout.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),0);
                    return insets;
                }
            });
            relativeLayout.requestApplyInsets();
            startService();
        }

    }

    private void startService()
    {
        Intent intent = new Intent(this,MyService.class);
        startService(intent);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Settings.canDrawOverlays(this))
            startService();
        else
            System.exit(0);
    }



}