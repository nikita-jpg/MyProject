package com.example.myproject;

import android.content.Context;
import android.os.AsyncTask;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
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

    public void addTextToCach(String text)
    {
        final TextElement textElement = new TextElement();
        textElement.text = text;
        textElementDao.insert(textElement);
        textDb.close();
    }

    public int getTextQuantity()
    {
        return 0;
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
        void insert(TextElement textElement);
        @Update
        void update(TextElement textElement);
        @Delete
        void delete(TextElement textElement);
    }
}
