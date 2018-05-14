package com.hqs.alx.tmdbcinemamovies;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.hqs.alx.tmdbcinemamovies.data.FavoritesContract;
import com.hqs.alx.tmdbcinemamovies.data.FavoritesSqlHelper;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DetailsActivity extends AppCompatActivity {

    TextView movieTitleTV;
    TextView movieBodyTV;
    ImageView movieImage;
    ImageView movieVideoIV;
    ImageView watchedOrNotIV;
    TextView detailsRatingTV;
    TextView detailsTotalVotesTV;
    FavoritesSqlHelper sqlHelper;
    SQLiteDatabase db;
    int watchedOrNotINT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // getting the intent from wherever it comes from
        final Intent intent =getIntent();

        //the "eye" image which describes if the movie was watched by the user or not
        watchedOrNotIV = (ImageView) findViewById(R.id.watchedIV);

        //The movie title, received from the intent at the beginning
        movieTitleTV = (TextView) findViewById(R.id.detailsMovieTitleTV);
        movieTitleTV.setText(intent.getStringExtra("MovieTitle"));

        // movie body description - received from the intent at the beginning
        movieBodyTV = (TextView) findViewById(R.id.detailsMovieBodyTV);
        movieBodyTV.setText(intent.getStringExtra("MovieBody"));

        //the average rating of the movie - received from the intent at the beginning
        detailsRatingTV = (TextView) findViewById(R.id.detailsRatingTV);
        detailsTotalVotesTV = (TextView) findViewById(R.id.detailsTotalVotesTV);

        //the total rating votes of the movie - received from the intent at the beginning
        detailsRatingTV.setText(intent.getStringExtra("MovieAvergae"));
        detailsTotalVotesTV.setText(intent.getStringExtra("MovieTotalVotes"));

        // some movies dont have big sized cover photos or a different cover photo -
        // if there is no big sized image - the default "ic_menu_report_image" will be shown
        movieImage = (ImageView) findViewById(R.id.detailsImageIV);
        if(intent.getStringExtra("MovieBigImage") == null){
            movieImage.setImageResource(android.R.drawable.ic_menu_report_image);
        }else if(!intent.getStringExtra("MovieBigImage").isEmpty())
            Picasso.with(DetailsActivity.this).load(intent.getStringExtra("MovieBigImage")).into(movieImage);
        else
            movieImage.setImageResource(android.R.drawable.ic_menu_report_image);

        movieVideoIV = (ImageView) findViewById(R.id.detailsYouTubethuIV);

        final String id = intent.getStringExtra("MovieId");

        //checking if the item is a movie or series item
        //the image and the video is received via okHttp in the "MyVeryLongTaskHelper" inner class bellow
        if(intent.getIntExtra("isAMovieOrNot", 1) == FavoritesContract.isNotAMovie){
            MyVeryLongTaskHelper tvSeriesVideo = new MyVeryLongTaskHelper();
            String tvSeriesVideoString = "tv/" + id;
            tvSeriesVideo.execute(tvSeriesVideoString);
        }else if(intent.getIntExtra("isAMovieOrNot", 1) == FavoritesContract.isAMovie){
            MyVeryLongTaskHelper movieVideo = new MyVeryLongTaskHelper();
            String movieVideoString = "movie/" + id;
            movieVideo.execute(movieVideoString);
        }

        watchedOrNotINT = intent.getIntExtra("MovieWatchedOrNot", 0);

        // the "eye" icon changes to watched or notWatched deppending on its' saved value and changes on every click
        if(watchedOrNotINT == FavoritesContract.watchedNo){
            watchedOrNotIV.setImageResource(R.drawable.hide);
        }else if(watchedOrNotINT == FavoritesContract.watchedYes){
            watchedOrNotIV.setImageResource(R.drawable.view);
        }

        //what executes when the "eye" icon is clicked
        //on each click it checks if the item exists in users' database
        //if not, it adds the item to favorites and marks it as watched
        //if exists, simply changes its' "watcheOrNot" value.
        watchedOrNotIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(watchedOrNotINT == FavoritesContract.watchedNo){
                    watchedOrNotIV.setImageResource(R.drawable.view);
                    if(CheckIfValueExistsInDataBase(id, intent.getStringExtra("MovieTitle"))){
                        sqlHelper = new FavoritesSqlHelper(DetailsActivity.this);
                        ContentValues values = new ContentValues();
                        values.put(FavoritesContract.COLUMN_WATCHED, FavoritesContract.watchedYes);

                        String [] whereArgs = {id};
                        db = sqlHelper.getWritableDatabase();
                        long checkUpdate = db.update(FavoritesContract.TABLE_NAME, values, FavoritesContract.COLUMN_UNIQ_ID + "=?", whereArgs);
                        if(checkUpdate != 0){
                            Toast.makeText(DetailsActivity.this, getString(R.string.markedAsWatched), Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        //adding a new item to the database
                        sqlHelper = new FavoritesSqlHelper(DetailsActivity.this);
                        ContentValues values = new ContentValues();
                        values.put(FavoritesContract.COLUMN_TITLE, intent.getStringExtra("MovieTitle"));
                        values.put(FavoritesContract.COLUMN_BODY, intent.getStringExtra("MovieBody"));
                        values.put(FavoritesContract.COLUMN_IMAGE, intent.getStringExtra("MovieImage"));
                        values.put(FavoritesContract.COLUMN_RELEASE_DATE, intent.getStringExtra("MovieDateReleased"));
                        values.put(FavoritesContract.COLUMN_UNIQ_ID, intent.getStringExtra("MovieId"));
                        values.put(FavoritesContract.COLUMN_RATING, intent.getStringExtra("MovieAvergae"));
                        values.put(FavoritesContract.COLUMN_TOTAL_VOTES, intent.getStringExtra("MovieTotalVotes"));
                        values.put(FavoritesContract.COLUMN_BIG_IMAGE, intent.getStringExtra("MovieBigImage"));
                        values.put(FavoritesContract.COLUMN_WATCHED, FavoritesContract.watchedYes);
                        if(intent.getIntExtra("isAMovieOrNot", 1) == FavoritesContract.isNotAMovie) {
                            values.put(FavoritesContract.COLUMN_MOVIE_OR_NOT, FavoritesContract.isNotAMovie);
                        }else if(intent.getIntExtra("isAMovieOrNot", 1) == FavoritesContract.isAMovie){
                            values.put(FavoritesContract.COLUMN_MOVIE_OR_NOT, FavoritesContract.isAMovie);
                        }

                        long newRow = sqlHelper.getWritableDatabase().insert(FavoritesContract.TABLE_NAME, null, values);
                        //if there is an error saving the item, the "getWritebleDatabase" returns "-1"
                        if(newRow == -1){
                            Toast.makeText(DetailsActivity.this, getString(R.string.erorSavingItem), Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(DetailsActivity.this, getString(R.string.itemSavedAndMarkedWatched), Toast.LENGTH_SHORT).show();
                        }
                    }
                    watchedOrNotINT = 1;
                }else if(watchedOrNotINT == 1){
                    watchedOrNotIV.setImageResource(R.drawable.hide);
                    sqlHelper = new FavoritesSqlHelper(DetailsActivity.this);
                    ContentValues values = new ContentValues();
                    values.put(FavoritesContract.COLUMN_WATCHED, FavoritesContract.watchedNo);

                    String [] whereArgs = {id};
                    db = sqlHelper.getWritableDatabase();
                    long checkUpdate = db.update(FavoritesContract.TABLE_NAME, values, FavoritesContract.COLUMN_UNIQ_ID + "=?", whereArgs);
                    if(checkUpdate != 0){
                        Toast.makeText(DetailsActivity.this, getString(R.string.markedAsNotWatched), Toast.LENGTH_SHORT).show();
                    }
                    watchedOrNotINT = 0;
                }
            }
        });

        (findViewById(R.id.backBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    //a method to check if the item exists in users' database.
    // the checking executes by the items' ID and its' TITLE
    private boolean CheckIfValueExistsInDataBase(String id, String title) {

        sqlHelper = new FavoritesSqlHelper(this);
        db = sqlHelper.getReadableDatabase();
        String [] columns = {FavoritesContract.COLUMN_UNIQ_ID, FavoritesContract.COLUMN_TITLE};
        String selection = FavoritesContract.COLUMN_UNIQ_ID + "=?" + " AND " + FavoritesContract.COLUMN_TITLE + "=?";
        String [] selectionArgs = {id, title};

        Cursor cursor = db.query(FavoritesContract.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        int result = cursor.getCount();
        if (result > 0){
            cursor.close();
            return true;
        }else{
            cursor.close();
            return false;
        }
    }

    //inner class to execute the api requests using okHttp
    public class MyVeryLongTaskHelper extends AsyncTask<String, String, String> {

        //a pop up progress dialog is shown while receiving information from the api
        ProgressDialog dialog = new ProgressDialog(DetailsActivity.this);

        @Override
        protected void onPreExecute() {
            dialog.setMessage(getString(R.string.loading));
            dialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {

            String allLines = "";

            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://api.themoviedb.org/3/"+ strings[0] + "/videos?api_key=b181bfa3152834d09a533638b87ea4c6&language=en-US")
                        .build();
                Response responses = null;

                try {
                    responses = client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                allLines = responses.body().string();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return allLines;
        }
            //allLines is returned. jsonText receives its' value (json)
            protected void onPostExecute(String jsonText) {

            try {
                //if the array from json is empty - it means there are no trailers
                JSONObject mainObject = new JSONObject(jsonText);
                JSONArray resultArray = mainObject.getJSONArray("results");
                if(resultArray.length() == 0){
                    Toast.makeText(DetailsActivity.this, "" + getString(R.string.noTrailers), Toast.LENGTH_SHORT).show();
                    movieVideoIV.setImageResource(R.drawable.novideo);
                } else{
                    //taking only the firt trailer result
                    JSONObject first = resultArray.getJSONObject(0);
                    //"key" is unique for each item
                    final String key = first.getString("key");
                    //a way to receive the image cover from "youtube" video
                    String imageUrl = "http://img.youtube.com/vi/"+ key +"/0.jpg";
                    Picasso.with(DetailsActivity.this).load(imageUrl).into(movieVideoIV);

                    //what executes when pressing the "youtube" cover image - an intent starts and takes the user to "youtube" app to whatch the specific item video
                    movieVideoIV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + key)));
                        }
                    });
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

                if(dialog.isShowing())
                    dialog.dismiss();
        }
    }
}
