package com.example.myproject;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.app.Service;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


public class MyService extends Service {
    private static final String BROADCAST_NAME = "com.example.myproject.unique.code";
    private WindowManager windowManager;
    private RelativeLayout relativeLayout;
    private WindowManager.LayoutParams params;
    private static NotificationManager notificationManager;
    private ClipboardManager clipboardManager;
    public static Context context;
    private NotificationManagerCompat notificationManagerCompat;
    private final int REQUEST_OF_PERMISSION = 1;
    private final static int NOTIFICATION_ID = 2;
    private static final String CHANNEL_ID = "Chanel_1";

    public MyService()
    {
        windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        relativeLayout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.activity_main,null);
        initScreenUtils();
        createNotificationChanelIfNede();
        initReciver();
    }

    public int onStartCommand(Intent intent, int flags, int startId)
    {
        makeButton();
        startNotify();
        return START_STICKY;
    }

    public void onCreate() {
        //Looper.prepare();
        /*
        Handler handler = new Handler(Looper.getMainLooper());
        windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        clipboardManager = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        relativeLayout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.activity_main,null);
        initScreenUtils();

        //Оформляем кнопку
        Button mButton = relativeLayout.findViewById(R.id.button);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(context.getResources().getColor(R.color.colorAccent));
        drawable.setCornerRadius(15);
        mButton.setBackground(drawable);
        mButton.setText("C");
        mButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 45);//надо фиксить
        mButton.setPadding(0,-17,0,0);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = "No text";
                if(clipboardManager.hasPrimaryClip())
                    //text = ""+clipboardManager.getPrimaryClip().getItemAt(0).getText();
                Toast.makeText(getApplicationContext(),"65468",Toast.LENGTH_SHORT).show();
            }
        });


        params = new WindowManager.LayoutParams(
                ScreenUtils.width/10,
                ScreenUtils.height/2,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.RGBA_8888
        );
        params.gravity = Gravity.RIGHT | Gravity.TOP;
        params.horizontalMargin = (float) 0.05;
        params.verticalMargin = (float) 0.25;
        windowManager.addView(relativeLayout,params);
         */
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private void initScreenUtils() {
        final Display display = windowManager.getDefaultDisplay();
        int statusBarHeight = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        ScreenUtils.width = display.getWidth();
        ScreenUtils.height = display.getHeight() - statusBarHeight;
    }

    private void makeButton()
    {
        Button mButton = relativeLayout.findViewById(R.id.button);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(context.getResources().getColor(R.color.colorAccent));
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
                Toast.makeText(getApplicationContext(),"65468",Toast.LENGTH_SHORT).show();
            }
        });

        params = new WindowManager.LayoutParams(
                ScreenUtils.width/10,
                ScreenUtils.height/2,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.RGBA_8888
        );
        params.gravity = Gravity.RIGHT | Gravity.TOP;
        params.horizontalMargin = (float) 0.05;
        params.verticalMargin = (float) 0.25;
        windowManager.addView(relativeLayout,params);
    }
    private void stopService()
    {
        System.exit(0);
    }

                                          //Уведомления
    private void initReciver()
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_NAME);
        context.registerReceiver(new MyReceiver(),intentFilter);
    }
    private void createNotificationChanelIfNede()
    {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,CHANNEL_ID,NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
    private void startNotify()
    {
        Intent intent = new Intent();
        intent.setAction(BROADCAST_NAME);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,CHANNEL_ID)
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
}
