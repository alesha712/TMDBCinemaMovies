package com.hqs.alx.tmdbcinemamovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Alex on 16/11/2017.
 */

public class FavoritesSqlHelper extends SQLiteOpenHelper {

    //the name of the database file
    public static final String DATABASE_NAME = "favorites.db";
    //database version - should be incremneted on every change to the database
    public static final int DATABSE_VERSION = 1;

    public FavoritesSqlHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABSE_VERSION);
    }

    //creating the database table
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String SQL_CREATE_FAV_TABLE = "CREATE TABLE " + FavoritesContract.TABLE_NAME + " ("
                + FavoritesContract.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + FavoritesContract.COLUMN_TITLE + " TEXT NOT NULL, "
                + FavoritesContract.COLUMN_BODY + " TEXT, "
                + FavoritesContract.COLUMN_RELEASE_DATE + " TEXT, "
                + FavoritesContract.COLUMN_UNIQ_ID + " INTEGER DEFAULT 0, "
                + FavoritesContract.COLUMN_RATING + " TEXT, "
                + FavoritesContract.COLUMN_TOTAL_VOTES + " TEXT, "
                + FavoritesContract.COLUMN_WATCHED + " INTEGER DEFAULT 0, "
                + FavoritesContract.COLUMN_BIG_IMAGE + " TEXT, "
                + FavoritesContract.COLUMN_IMAGE + " TEXT, "
                + FavoritesContract.COLUMN_MOVIE_OR_NOT + " INTEGER DEFAULT 1);";

        sqLiteDatabase.execSQL(SQL_CREATE_FAV_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
