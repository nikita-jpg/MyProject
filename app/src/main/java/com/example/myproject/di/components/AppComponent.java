package com.example.myproject.di.components;

import android.content.ServiceConnection;

import com.example.myproject.di.module.AppModule;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = AppModule.class)
@Singleton
public interface AppComponent {
    ServiceConnection getServiceConnection();
}
