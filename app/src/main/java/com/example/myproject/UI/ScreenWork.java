package com.example.myproject.UI;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;

import com.example.myproject.R;
import com.example.myproject.TextElement;

import java.util.ArrayList;
import java.util.List;

public class ScreenWork
{
    private Context context;
    private RelativeLayout background;
    private MotionLayout blackBoard;//панель с кнопками в выдвигающемся окне


    //Характеристики background
    private float minBackgroundAlpha;
    private float maxBackgroundAlpha;

    //Характеристики bB, выражается в % от SCREEN_WIDTH и SCREEN_HEIGHT
    private float bBHeightСoefPort;
    private float bBWidthСoefPort;
    private float bBHeightСoefLand;
    private float bBWidthСoefLand;


    private int bBgravity;// Сторона, с которой появлется bB

    private boolean finger;//Отвечает за то, касается ли пользователь bB в данный момент
    private float startXLiner;
    private IteamWork iteamWork;



    private UIManager uiManager;
    private ParamsForUI paramsForUI;


    public ScreenWork(Context context, ParamsForUI paramsForUI)
    {
        this.context = context;
        this.paramsForUI = paramsForUI;
    }


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


        if(uiManager.getBtnGravity() == Gravity.LEFT)
            bBgravity = Gravity.LEFT;
        else
            bBgravity = Gravity.RIGHT;


        addBackgroundbBOnScreen();
        addBlackBoardOnScreen();
        hideBackground();
        hidebB();


        startXLiner = 0;
        finger = false;

        addListenersTobB();

        iteamWork = new IteamWork();
        iteamWork.init();
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
            screenWidth = paramsForUI.SCREEN_WIDTH;
            screenHeight = paramsForUI.SCREEN_HEIGHT;
        }
        else
        {
            screenWidth = paramsForUI.SCREEN_HEIGHT;
            screenHeight = paramsForUI.SCREEN_WIDTH;
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
        uiManager.addView(background,windowParams);
    }

    private void orientationChangedBackground(int orientation)
    {
        //Создаём WindowManager.LayoutParams
        WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
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


        //Задаём размеры в соответствии с ориентацией экрана
        int screenHeight;
        int screenWidth;
        if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            screenWidth = paramsForUI.SCREEN_WIDTH;
            screenHeight = paramsForUI.SCREEN_HEIGHT;

        } else {
            screenWidth = paramsForUI.SCREEN_HEIGHT;
            screenHeight = paramsForUI.SCREEN_WIDTH;
        }
        windowParams.width = screenWidth;
        windowParams.height = screenHeight;

        //Поворачиваем экран
        if (background.isAttachedToWindow())
            uiManager.updateView(background, windowParams);
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
        //windowManager.removeView(background);
    }


    //Работа с bB
    public void dispatchTouchEvent(MotionEvent event)
    {
        blackBoard.dispatchTouchEvent(event);
    }

    private void initBb(WindowManager.LayoutParams windowParams)
    {

        if (context.getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        {
            windowParams.width = paramsForUI.SCREEN_WIDTH;
            windowParams.height = paramsForUI.SCREEN_HEIGHT;
        } else {
            windowParams.width = paramsForUI.SCREEN_HEIGHT;
            windowParams.height = paramsForUI.SCREEN_WIDTH;
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
        if(bBgravity == Gravity.RIGHT)
            blackBoard.loadLayoutDescription(R.xml.right_to_left);
        else
            blackBoard.loadLayoutDescription(R.xml.left_to_right);


        ConstraintSet constraintSet = blackBoard.getConstraintSet(R.id.start);
        if(context.getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        {
            bBWidth = (int) (paramsForUI.SCREEN_WIDTH * bBWidthСoefPort)/100;
            bBHeight = (int) (paramsForUI.SCREEN_HEIGHT * bBHeightСoefPort)/100;
        }
        else
        {
            bBWidth = (int) (paramsForUI.SCREEN_HEIGHT * bBWidthСoefLand)/100;
            bBHeight = (int) (paramsForUI.SCREEN_WIDTH * bBHeightСoefLand)/100;
        }


        constraintSet.constrainWidth(R.id.liner, uiManager.getBtnWidth());
        constraintSet.constrainHeight(R.id.liner, uiManager.getBtnHeight());
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

    //Работа с координатами bB в начальном положениее (в виде кнопки)
    public void updateStartCoord()
    {
        int statusBarHeight = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }

        ConstraintSet constraintSet = blackBoard.getConstraintSet(R.id.start);


        constraintSet.clear(R.id.liner,ConstraintSet.TOP);
        constraintSet.clear(R.id.liner,ConstraintSet.START);
        constraintSet.clear(R.id.liner,ConstraintSet.END);

        float z = uiManager.getBtnVerticalMargin();
        int y;

        if(context.getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        {
            y = (int) ((paramsForUI.SCREEN_HEIGHT-statusBarHeight- paramsForUI.NAV_BAR_HEIGHT)*z- paramsForUI.NAV_BAR_HEIGHT+statusBarHeight);

            if(bBgravity == Gravity.RIGHT)
                constraintSet.connect(R.id.liner,ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END,0);
            else
                constraintSet.connect(R.id.liner,ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START,0);

        }
        else
        {
            y = (int) ((paramsForUI.SCREEN_WIDTH-statusBarHeight)*z-statusBarHeight);

            if(bBgravity == Gravity.RIGHT)
                constraintSet.connect(R.id.liner,ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START, paramsForUI.SCREEN_HEIGHT- paramsForUI.NAV_BAR_HEIGHT- paramsForUI.STATUS_BAR_HEIGHT-uiManager.getBtnWidth());
            else
                constraintSet.connect(R.id.liner,ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START,0);
        }
        constraintSet.connect(R.id.liner,ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP,y);

    }

    public void addBlackBoardOnScreen()
    {
        WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
        initBb(windowParams);
        uiManager.addView(blackBoard,windowParams);
    }

    public void orientationChangedbB()
    {
        //Создаём WindowManager.LayoutParams
        WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
        initBb(windowParams);

        //Поворачиваем экран
        if (blackBoard.isAttachedToWindow())
            uiManager.updateView(blackBoard, windowParams);
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
    }

    private void addListenersTobB()
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
                            forcedOpenbB();
                        else if(a == startXLiner)
                        {
                            hidebB();
                            hideBackground();
                            uiManager.showBtn();
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
                    uiManager.showBtn();
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
        hideBackground();
        uiManager.removeView(blackBoard);
    }

    public void addText(String text)
    {
        TextElement textElement = new TextElement();
        textElement.text = text;
        iteamWork.addText(textElement);
    }

    public UIManager getUiManager() {
        return uiManager;
    }

    public void setUiManager(UIManager uiManager) {
        this.uiManager = uiManager;
    }


    private class IteamWork
    {
        ArrayList<String> arrayList = new ArrayList<>();
        public void init()
        {
            // получаем элемент ListView
            ListView countriesList = (ListView) blackBoard.findViewById(R.id.list1);

            // создаем адаптер
            IteamWork.myAdapter adapter = new IteamWork.myAdapter(arrayList);

            // устанавливаем для списка адаптер
            countriesList.setAdapter(adapter);


        }

        public void addText(List<TextElement> textElements)
        {
            for(int i = 0;i<textElements.size();i++)
                addText(textElements.get(i));
        }

        public void addText(TextElement textElement)
        {
            arrayList.add(textElement.text);
        }

        private class myAdapter extends BaseAdapter
        {
            ArrayList<String> arrayList;
            LayoutInflater layoutInflater;

            myAdapter(ArrayList<String> arrayList)
            {
                this.arrayList = arrayList;
                layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }

            @Override
            public int getCount() {
                return arrayList.size();
            }

            @Override
            public Object getItem(int position) {
                return arrayList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View view = convertView;
                if (view == null) {
                    view = layoutInflater.inflate(R.layout.list_item, parent, false);
                }
                String text = (String) getItem(position);
                TextView textView = view.findViewById(R.id.item_text_view);
                textView.setLines(text.length()/40+1);
                textView.setText(text);
                return view;
            }
        }

        private void initCache()
        {
            List<TextElement> textElements;
        }


    }

}