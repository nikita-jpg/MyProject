package com.example.myproject.UI;

import android.content.Context;
import android.content.res.Configuration;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.example.myproject.Cache.TextElement;

import java.util.List;

import javax.inject.Inject;

import static android.content.Context.WINDOW_SERVICE;

public class UIManager
{

    private Context context;
    private WindowManager windowManager;//Для работы с окнами, которые отображаются поверх всех приложений

    private MButton mButton;//Кнопка для вывода blackBoard, mButton от mainButton
    private ScreenWork screenWork;

    private ParamsForUI paramsForUI;


    public UIManager(Context context,MButton mButton,ParamsForUI paramsForUI,ScreenWork screenWork) {
        this.context = context;
        this.paramsForUI = paramsForUI;
        windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);

        //Создаём классы для работы приложения
        this.mButton = mButton;
        this.screenWork = screenWork;
    }
    public void start()
    {
                        //MButton
        mButton.init();
        mButton.addMbuttonOnScreen();
                        //ScreenWork
        screenWork.init();
    }


    //Работа с View
    public void addView(View view,WindowManager.LayoutParams layoutParams)
    {
        windowManager.addView(view,layoutParams);
    }
    public void updateView(View view,WindowManager.LayoutParams layoutParams)
    {
        windowManager.updateViewLayout(view,layoutParams);
    }
    public void removeView(View view)
    {
        windowManager.removeView(view);
    }


    //Показать что-то
    public void showBackground()
    {
        screenWork.showBackground();
    }

    public void showBb()
    {
        screenWork.showbB();
    }

    public void showBtn()
    {
        mButton.show();
    }


    //Геттеры
    public int getBtnGravity()
    {
        return mButton.getBtnGravity();
    }

    public int getBtnWidth()
    {
        return mButton.getBtnWidth();
    }

    public int getBtnHeight()
    {
        return mButton.getBtnHeight();
    }

    public float getBtnVerticalMargin()
    {
        return mButton.getBtnVerticalMargin();
    }


    //Негруппируемые методы
    public void dispatchTouchEventToScreenWork(MotionEvent event)
    {
        screenWork.dispatchTouchEvent(event);
    }
    public void configurationChanged(Configuration newConfig)
    {
        mButton.orientationChangedBtn();
        //screenWork.configurationChanged(newConfig);
    }
    public void addText(String text)
    {
        screenWork.addText(text);
    }
    public void addText(List<TextElement> textElements)
    {
        screenWork.addText(textElements);
    }

}
