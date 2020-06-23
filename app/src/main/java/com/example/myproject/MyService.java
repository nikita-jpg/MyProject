package com.example.myproject;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.opengl.Visibility;
import android.os.Build;
import android.os.IBinder;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.app.Service;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;


public class MyService extends Service {
    private final String BROADCAST_NAME = "com.example.myproject.unique.code";
    private final int NOTIFICATION_ID = 2;
    private final String CHANNEL_ID = "Chanel_1";

    private WindowManager windowManager;
    private RelativeLayout buttonLayout;
    private DrawerLayout blackBoardLayout;
    private NavigationView navigationView;
    private WindowManager.LayoutParams params;
    private static NotificationManager notificationManager;
    private ClipboardManager clipboardManager;
    private NotificationManagerCompat notificationManagerCompat;
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

    private void init()
    {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        buttonLayout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.activity_button,null);
        final Context contextThemeWrapper = new ContextThemeWrapper(this, R.style.AppTheme_NoActionBar);
        blackBoardLayout = (DrawerLayout) LayoutInflater.from(contextThemeWrapper).inflate(R.layout.activity_black_board,null);
        screenHeight = getScreenHeight();
        screenWidth = getScreenWidth();

        initReciver();//Нужен для работы кнопки на уведомлении
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
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        return display.getHeight();
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
                addBlackBoardomScreen();
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

    private void addBlackBoardomScreen()
    {

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

        /*
        int flag=0
                |WindowManager.LayoutParams.FLAG_FULLSCREEN
                ;
        params.flags=flag;
         */

        params.gravity = Gravity.CENTER;
        //blackBoardLayout.setBackgroundResource(R.drawable.black_board_style);
        //params.horizontalMargin = (float) 0.25;
        //params.verticalMargin = (float) 0.25;

        /*
        Button textView = new Button(this);
        textView.setBackgroundColor(Color.BLUE);
        Button textView2 = new Button(this);

        textView.setBackgroundColor(Color.RED);
        NavigationView.LayoutParams layoutParams = new NavigationView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        NavigationView navigationView = blackBoardLayout.findViewById(R.id.nav_view);
        navigationView.addView(textView);
        navigationView.addView(textView2);
         */
        //blackBoardLayout.addView(textView,layoutParams);


        RelativeLayout relativeLayout = new RelativeLayout(this);
        Button button = new Button(this);
        RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        relativeLayout.addView(button,layoutParams1);
        NavigationView navigationView = blackBoardLayout.findViewById(R.id.nav_view);
        navigationView.addView(relativeLayout);
        navigationView.setSystemUiVisibility(View.GONE);



        /*
        params.softInputMode=WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;
        params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;


        params.flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_FULLSCREEN;

        int flag =  View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

        blackBoardLayout.setSystemUiVisibility(flag);
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
         */
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;

        blackBoardLayout.setSystemUiVisibility(uiOptions);
        /*
        blackBoardLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

         */

        windowManager.addView(blackBoardLayout,params);
    }


                                          //Уведомления
    private void initReciver()
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_NAME);
        registerReceiver(new MyReceiver(),intentFilter);
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
    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopForeground(true);
            stopService();
        }
    }



                                           //Работа сервиса
    private void stopService()
    {
        System.exit(0);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
