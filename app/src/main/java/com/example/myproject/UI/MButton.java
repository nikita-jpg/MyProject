package com.example.myproject.UI;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.core.content.ContextCompat;

import com.example.myproject.R;

import javax.inject.Inject;


public class MButton
{
    private Button mBtn;
    private int btnGravitySide; //Сторона, с которой будет кнопка. Неободимо для работы bB;
    private int btnWidth;
    private int btnHeight;
    private float btnVerticalMargin;// Отступ от верха экрана в %. Статус Бар в этот экран не входит
    private int btnColor;
    private int btnCornerRadius; //Закругление углов
    private int btnX;
    private int btnY;
    private boolean isHide;
    private UIManager uiManager;



    Context context;
    ParamsForUI paramsForUI;

    public MButton(Context context, ParamsForUI paramsForUI)
    {
        this.context = context;
        this.paramsForUI = paramsForUI;
    }

    public void init()
    {
        //Именя переменных в SharedPreference
        String btnPrefer = "BTN_PREFERENCE";
        String btnWidthCoeffPref  = "btnWidthCoeff";
        String btnHeightCoeffPref = "btnHeightCoeff";
        String btnGravitySidePref = "btnGravitySidePref";
        String btnColorPref = "btnColorPref";
        String btnCornerRadiusPref = "btnCornerRadiusPref";
        String btnVerticalMarginPref = "btnVerticalMarginPref";


        SharedPreferences sharedPreferences = context.getSharedPreferences(btnPrefer, Context.MODE_PRIVATE);
        //Проверяем, есть ли уже записанные значения
        if(     sharedPreferences.contains(btnWidthCoeffPref)  &&
                sharedPreferences.contains(btnHeightCoeffPref) &&
                sharedPreferences.contains(btnGravitySidePref) &&
                sharedPreferences.contains(btnColorPref)       &&
                sharedPreferences.contains(btnCornerRadiusPref)&&
                sharedPreferences.contains(btnVerticalMarginPref))
        {
            btnWidth = (int) (paramsForUI.SCREEN_WIDTH*sharedPreferences.getFloat(btnWidthCoeffPref ,-1));
            btnHeight = (int) (paramsForUI.SCREEN_HEIGHT*sharedPreferences.getFloat(btnHeightCoeffPref,-1));
            btnGravitySide = sharedPreferences.getInt(btnGravitySidePref,-1);
            btnColor = ContextCompat.getColor(context,sharedPreferences.getInt(btnColorPref,-1));
            btnCornerRadius = sharedPreferences.getInt(btnCornerRadiusPref,-1);
            btnVerticalMargin = sharedPreferences.getFloat(btnVerticalMarginPref,-1);
        }               //Если нет, то инициализируем значениями по умолчанию
        else
        {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putFloat(btnWidthCoeffPref ,0.034f);
            editor.putFloat(btnHeightCoeffPref,0.1f);
            editor.putInt(btnGravitySidePref, Gravity.LEFT);
            editor.putInt(btnColorPref, R.color.black);
            editor.putInt(btnCornerRadiusPref,15);
            editor.putFloat(btnVerticalMarginPref,0.8f);
            editor.apply();

            btnWidth = paramsForUI.SCREEN_WIDTH/30;
            btnHeight = paramsForUI.SCREEN_HEIGHT/10;
            btnGravitySide = Gravity.LEFT;
            btnColor = ContextCompat.getColor(context,R.color.black);
            btnCornerRadius = 15;
            btnVerticalMargin = 0.8f;
        }

        mBtn = new Button(context);
        isHide = false;
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

        //Обновляем координаты btnX и btnY
        updateCoord();
        windowParams.x = btnX;
        windowParams.y = btnY;

        //OnClickListener
        mBtn.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                uiManager.dispatchTouchEventToScreenWork(event);

                if(event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    hide();
                    uiManager.showBackground();
                    uiManager.showBb();
                }
                return false;
            }
        });
    }

    //Вычисляем координаты для кнопки
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
                btnX = paramsForUI.SCREEN_WIDTH/2;
            else
                btnX = -paramsForUI.SCREEN_WIDTH/2;

            btnY = (int) ((paramsForUI.SCREEN_HEIGHT- paramsForUI.NAV_BAR_HEIGHT-statusBarHeight)*(btnVerticalMargin-0.5));
        }
        else
        {
            if(btnGravitySide == Gravity.RIGHT)
                btnX = (paramsForUI.SCREEN_HEIGHT- paramsForUI.STATUS_BAR_HEIGHT- paramsForUI.NAV_BAR_HEIGHT)/2;
            else
                btnX = -(paramsForUI.SCREEN_HEIGHT- paramsForUI.STATUS_BAR_HEIGHT- paramsForUI.NAV_BAR_HEIGHT)/2;

            btnY = (int) ((paramsForUI.SCREEN_WIDTH-statusBarHeight)*(btnVerticalMargin-0.5f));
        }
    }

    public void addMbuttonOnScreen()
    {
        WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
        initBtn(windowParams);
        uiManager.addView(mBtn,windowParams);
    }


    public void orientationChangedBtn()
    {
        WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
        initBtn(windowParams);
        if(isHide)
            hide();
        else
            show();
        uiManager.updateView(mBtn,windowParams);
    }

    public void show()
    {
        mBtn.setAlpha(1);
        isHide = false;
    }

    public void hide()
    {
        mBtn.setAlpha(0);
        isHide = true;
    }

    //Геттеры и сеттеры
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

    public UIManager getUiManager() {
        return uiManager;
    }

    public void setUiManager(UIManager uiManager) {
        this.uiManager = uiManager;
    }

}

