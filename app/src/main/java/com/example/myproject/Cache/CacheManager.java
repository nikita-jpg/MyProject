package com.example.myproject.Cache;

import android.content.Context;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.Update;

import java.util.List;

public class CacheManager
{
    private TextDb textDb;
    private TextElementDao textElementDao;
    private Context context;

    public CacheManager(Context context)
    {
        this.context = context;
        textDb = Room.databaseBuilder(context, TextDb.class,"database")
                .allowMainThreadQueries()
                .build();
        textElementDao = textDb.textElementDao();
    }

    public List<TextElement> getAllText()
    {
        return textElementDao.getAll();
    }

    public long addTextToCache(String text)
    {
        final TextElement textElement = new TextElement();
        textElement.text = text;
        long id = textElementDao.insert(textElement);
        return id;
    }

    public TextElement getTextById(long id)
    {
        TextElement textElement = textElementDao.getElementById(id);
        return textElement;
    }


                    //Текстовая Бд
    @Database(entities =  {TextElement.class}, version = 1,exportSchema = false)
    public abstract static class TextDb extends RoomDatabase
    {
        public abstract TextElementDao textElementDao();
    }


    @Dao
    public interface TextElementDao
    {
        @Query("SELECT * FROM myTable")
        List<TextElement> getAll();

        @Query("SELECT * FROM myTable WHERE id = :id")
        TextElement getElementById(long id);

        @Insert
        long insert(TextElement textElement);
        @Update
        void update(TextElement textElement);
        @Delete
        void delete(TextElement textElement);
    }
}
