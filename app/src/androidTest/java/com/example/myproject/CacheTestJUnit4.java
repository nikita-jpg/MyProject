package com.example.myproject;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.example.myproject.Cache.CacheManager;
import com.example.myproject.Cache.TextElement;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;


@RunWith(AndroidJUnit4.class)
public class CacheTestJUnit4 extends Assert {
    private String firstText;
    private String secondText;
    static Context context;
    static CacheManager cacheManager;

    @BeforeClass
    public static void setUpBeforeClass()
    {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        cacheManager = new CacheManager(context);
    }

    @Before
    public void setUpSimpleText()
    {
        firstText = "First text";
        secondText = "Second txt";
    }

    @After
    public void tearDownSimpleText()
    {
        firstText = null;
        secondText = null;
    }

    @Test
    public void testSaveReadTest()
    {
        long firstId = cacheManager.addTextToCache(firstText);
        long secondId = cacheManager.addTextToCache(secondText);
        String firstTextRead = cacheManager.getTextById(firstId).text;
        String secondTextRead = cacheManager.getTextById(secondId).text;
        List<TextElement> textRead = cacheManager.getAllText();

        assertEquals("Ошибка сохранения или чтения",firstText,firstTextRead);
        assertEquals("Ошибка сохранения или чтения",secondText,secondTextRead);
        assertEquals("Ошибка в CacheManager в getAll",firstTextRead+secondTextRead,textRead.get(textRead.size()-2).text+textRead.get(textRead.size()-1).text);
    }
}
