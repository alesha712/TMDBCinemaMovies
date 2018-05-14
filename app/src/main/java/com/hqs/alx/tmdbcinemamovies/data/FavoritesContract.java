package com.hqs.alx.tmdbcinemamovies.data;

/**
 * Created by Alex on 16/11/2017.
 */

public class FavoritesContract {

    //Empty private constractor to prevent from initializing
    private FavoritesContract (){
    }

    /*
        Constant column names of the favorites database table
     */
    public final static String TABLE_NAME = "favorites";

    public final static String COLUMN_ID = "_id";
    public final static String COLUMN_TITLE = "title";
    public final static String COLUMN_BODY = "body";
    public final static String COLUMN_IMAGE = "image";
    public final static String COLUMN_RELEASE_DATE = "release_date";
    public final static String COLUMN_UNIQ_ID = "uniq_id";
    public final static String COLUMN_BIG_IMAGE = "big_image";
    public final static String COLUMN_TOTAL_VOTES = "total_votes";
    public final static String COLUMN_RATING = "rating";
    public final static String COLUMN_WATCHED = "watched";
    public final static String COLUMN_MOVIE_OR_NOT = "movie_or_not";

    //SQL doesnt receive boolean so we use integer instead
    public final static int watchedYes = 1;
    public final static int watchedNo = 0;

    public final static int isAMovie = 1;
    public final static int isNotAMovie = 0;







}
