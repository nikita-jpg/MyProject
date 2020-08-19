package com.example.myproject;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "myTable")
public class TextElement
{
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String text;
}