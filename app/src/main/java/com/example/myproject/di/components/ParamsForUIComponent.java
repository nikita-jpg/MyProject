package com.example.myproject.di.components;

import com.example.myproject.UI.ParamsForUI;
import com.example.myproject.UI.UIManager;
import com.example.myproject.di.module.ParamsForUIModule;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = ParamsForUIModule.class)
@Singleton
public interface ParamsForUIComponent {
    void inject(UIManager uiManager);
}
