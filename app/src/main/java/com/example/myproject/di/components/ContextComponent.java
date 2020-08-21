package com.example.myproject.di.components;

import com.example.myproject.MainActivity;
import com.example.myproject.di.module.ContextModule;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = ContextModule.class)
@Singleton
public interface ContextComponent {
    void inject(MainActivity mainActivity);
}
