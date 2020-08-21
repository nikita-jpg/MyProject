package com.example.myproject.UI;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;

public class ParamsForUI {

    Context context;
    private String PHONE_HEIGHT_PREFERENCE = "PHONE_HEIGHT_PREFERENCE";
    private String PHONE_WIDTH_PREFERENCE = "PHONE_WIDTH_PREFERENCE";
    private String STATUS_BAR_HEIGHT_PREFERENCE = "STATUS_BAR_HEIGHT";
    private String NAVIGATION_BAR_HEIGHT = "NAVIGATION_BAR_HEIGHT";
    private String PHONE_WIDTH_AND_HEIGHT_PREFERENCE = "PHONE_WIDTH_AND_HEIGHT_PREFERENCE";

    public final int SCREEN_HEIGHT;//Высота экрана
    public final int SCREEN_WIDTH;//Ширина экрана
    public final int STATUS_BAR_HEIGHT;
    public final int NAV_BAR_HEIGHT;

    public ParamsForUI(Context context)
    {
        this.context = context;
        SharedPreferences sharedPreferences = context.getSharedPreferences(PHONE_WIDTH_AND_HEIGHT_PREFERENCE,Context.MODE_PRIVATE);
        SCREEN_HEIGHT = sharedPreferences.getInt(PHONE_HEIGHT_PREFERENCE,0);
        SCREEN_WIDTH = sharedPreferences.getInt(PHONE_WIDTH_PREFERENCE,0);
        STATUS_BAR_HEIGHT = sharedPreferences.getInt(STATUS_BAR_HEIGHT_PREFERENCE,0);
        NAV_BAR_HEIGHT = sharedPreferences.getInt(NAVIGATION_BAR_HEIGHT,0);
    }
}
