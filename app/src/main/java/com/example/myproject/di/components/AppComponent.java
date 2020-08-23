package com.example.myproject.di.components;

import com.example.myproject.di.module.AppModule;
import com.example.myproject.service.MyService;
import com.example.myproject.service.NotificationForService;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = AppModule.class)
@Singleton
public interface AppComponent {
}
