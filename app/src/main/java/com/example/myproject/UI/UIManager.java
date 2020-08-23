package com.example.myproject.UI;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
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

import javax.inject.Inject;

import static android.content.Context.WINDOW_SERVICE;

public class UIManager
{

    private Context context;
    private WindowManager windowManager;//Для работы с окнами, которые отображаются поверх всех приложений

    private MButtonWork mButton;//Кнопка для вывода blackBoard, mButton от mainButton
    private ScreenWork screenWork;

    @Inject
    ParamsForUI paramsForUI;


    public UIManager(Context context) {
        this.context = context;
        windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);

        //paramsForUIComponent.inject(this);


        //Создаём классы для работы приложения
        mButton = new MButtonWork();
        screenWork = new ScreenWork();
    }


    public void start()
    {

                        //MButton
        mButton.init();
        mButton.addMbuttonOnScreen();
                        //ScreenWork
        screenWork.init();

    }

    public void configurationChanged(Configuration newConfig)
    {
        mButton.orientationChangedBtn();
        screenWork.configurationChanged(newConfig);
    }

    public void addText(String text)
    {
        screenWork.addText(text);
    }

    private class MButtonWork
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


        private void init()
        {
                            //Именя переменных в SharedPreference
            String btnPrefer = "BTN_PREFERENCE";
            String btnWidthCoeffPref  = "btnWidthCoeff";
            String btnHeightCoeffPref = "btnHeightCoeff";
            String btnGravitySidePref = "btnGravitySidePref";
            String btnColorPref = "btnColorPref";
            String btnCornerRadiusPref = "btnCornerRadiusPref";
            String btnVerticalMarginPref = "btnVerticalMarginPref";


            SharedPreferences sharedPreferences = context.getSharedPreferences(btnPrefer,Context.MODE_PRIVATE);
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
                editor.putInt(btnGravitySidePref,Gravity.LEFT);
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
                    screenWork.dispatchTouchEvent(event);

                    if(event.getAction() == MotionEvent.ACTION_DOWN)
                    {
                        hide();
                        screenWork.showBackground();
                        screenWork.showbB();
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

        private void addMbuttonOnScreen()
        {
            WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
            initBtn(windowParams);
            windowManager.addView(mBtn,windowParams);
        }


        private void orientationChangedBtn()
        {
            WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
            initBtn(windowParams);
            windowManager.updateViewLayout(mBtn,windowParams);

        }

        public void show()
        {
            mBtn.setAlpha(1);
        }

        public void hide()
        {
            mBtn.setAlpha(0);
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

    }


    private class ScreenWork
    {
        private RelativeLayout background;
        MotionLayout blackBoard;//панель с кнопками в выдвигающемся окне


        //Характеристики background
        float minBackgroundAlpha;
        float maxBackgroundAlpha;

        //Характеристики bB, выражается в % от SCREEN_WIDTH и SCREEN_HEIGHT
        float bBHeightСoefPort;
        float bBWidthСoefPort;
        float bBHeightСoefLand;
        float bBWidthСoefLand;


        int bBgravity;// Сторона, с которой появлется bB

        boolean finger;//Отвечает за то, касается ли пользователь bB в данный момент
        float startXLiner;

        private IteamWork iteamWork;

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


            if(mButton.getBtnGravity() == Gravity.LEFT)
                bBgravity = Gravity.LEFT;
            else
                bBgravity = Gravity.RIGHT;


            screenWork.addBackgroundbBOnScreen();
            screenWork.addBlackBoardOnScreen();
            screenWork.hideBackground();
            screenWork.hidebB();


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
            windowManager.addView(background,windowParams);
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
                windowManager.updateViewLayout(background, windowParams);
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
            windowManager.removeView(background);
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


            constraintSet.constrainWidth(R.id.liner, mButton.getBtnWidth());
            constraintSet.constrainHeight(R.id.liner, mButton.getBtnHeight());
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

            float z = mButton.getBtnVerticalMargin();
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
                    constraintSet.connect(R.id.liner,ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START, paramsForUI.SCREEN_HEIGHT- paramsForUI.NAV_BAR_HEIGHT- paramsForUI.STATUS_BAR_HEIGHT-mButton.getBtnWidth());
                else
                    constraintSet.connect(R.id.liner,ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START,0);
            }
            constraintSet.connect(R.id.liner,ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP,y);

        }

        public void addBlackBoardOnScreen()
        {
            WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
            initBb(windowParams);
            windowManager.addView(blackBoard,windowParams);
        }

        public void orientationChangedbB()
        {
            //Создаём WindowManager.LayoutParams
            WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
            initBb(windowParams);

            //Поворачиваем экран
            if (blackBoard.isAttachedToWindow())
                windowManager.updateViewLayout(blackBoard, windowParams);
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
                                screenWork.forcedOpenbB();
                            else if(a == startXLiner)
                            {
                                hidebB();
                                hideBackground();
                                mButton.show();
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
                        mButton.show();
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
            screenWork.hideBackground();
            windowManager.removeView(blackBoard);
        }

        public void addText(String text)
        {
            TextElement textElement = new TextElement();
            textElement.text = text;
            iteamWork.addText(textElement);
        }



        private class IteamWork
        {
            ArrayList<String> arrayList = new ArrayList<>();
            public void init()
            {
                // получаем элемент ListView
                ListView countriesList = (ListView) blackBoard.findViewById(R.id.list1);

                // создаем адаптер
                myAdapter adapter = new myAdapter(arrayList);

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

}
