package com.hqs.alx.tmdbcinemamovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hqs.alx.tmdbcinemamovies.data.FavoritesContract;
import com.hqs.alx.tmdbcinemamovies.data.FavoritesSqlHelper;
import com.squareup.picasso.Picasso;

import java.util.Random;

public class NewMovieActivity extends AppCompatActivity {

    EditText titleET;
    EditText bodyET;
    EditText imageUrlET;
    EditText releaseDateET;
    ImageView movieIV;
    TextView activityHeader;
    FavoritesSqlHelper sqlHelper;
    SQLiteDatabase db;


    /*
    *
    * this activity is for adding a new movie and for edditing one
    *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_movie);

        sqlHelper = new FavoritesSqlHelper(this);
        movieIV = (ImageView) findViewById(R.id.movieIV);
        titleET = (EditText) findViewById(R.id.movieTitleET);
        bodyET = (EditText) findViewById(R.id.movieBodyET);
        imageUrlET = (EditText) findViewById(R.id.movieImageUrlET);
        releaseDateET = (EditText) findViewById(R.id.movieReleaseDateET);

        final Intent intent = getIntent();

        //if the activity started from the mainactivity, meaning there is no intent extras (from main activity is is possible only to creat a new movie - no edditing)
        if(!intent.getStringExtra("incomingFrom").equals("MainActivity")) {
            activityHeader = (TextView)findViewById(R.id.newMovieHeader);
            activityHeader.setText(getText(R.string.edit));
            titleET.setText(intent.getStringExtra("movieTitle"));
            bodyET.setText(intent.getStringExtra("movieBody"));
            imageUrlET.setText(intent.getStringExtra("movieImageURL"));
            releaseDateET.setText(intent.getStringExtra("movieReleaseDate"));
        }

        // if the user came from "searchOnLine" activity, it will receive the information of the item the user pressed
        if(intent.getStringExtra("incomingFrom").equals("searchOnLineActivity")){

            //each movie from api has a unique id
            final long num = intent.getLongExtra("movieUniqId", 0);

            (findViewById(R.id.saveBtn)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (titleET.getText().toString().isEmpty()) {
                        Toast.makeText(NewMovieActivity.this, getString(R.string.movieTitleIsRequired), Toast.LENGTH_SHORT).show();
                    } else {
                        ContentValues values = new ContentValues();
                        values.put(FavoritesContract.COLUMN_TITLE, titleET.getText().toString());
                        values.put(FavoritesContract.COLUMN_BODY, bodyET.getText().toString());
                        values.put(FavoritesContract.COLUMN_IMAGE, imageUrlET.getText().toString());
                        values.put(FavoritesContract.COLUMN_RELEASE_DATE, releaseDateET.getText().toString());
                        values.put(FavoritesContract.COLUMN_UNIQ_ID, intent.getLongExtra("movieUniqId", 0));

                        if (num != 0) {
                            String id = String.valueOf(num);
                            //checking if the item with the unique id exists in database, if does, it will be updated
                            //if not, it will add the item to database
                            if (!CheckIfValueExistsInDataBase(id)) {
                                long newItem = sqlHelper.getWritableDatabase().insert(FavoritesContract.TABLE_NAME, null, values);
                                if (newItem == -1) {
                                    Toast.makeText(NewMovieActivity.this, getString(R.string.erorSavingItem), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(NewMovieActivity.this, getString(R.string.itemSavedSuccessfuly), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                String [] whereArgs = {String.valueOf(num)};
                                db = sqlHelper.getReadableDatabase();
                                long checkIfUpdated = db.update(FavoritesContract.TABLE_NAME, values, FavoritesContract.COLUMN_ID + " =?" , whereArgs);
                                if(checkIfUpdated != 0){
                                    Toast.makeText(NewMovieActivity.this, R.string.FavoritesItemUpdated, Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(NewMovieActivity.this, R.string.erorSavingItem, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        finish();
                    }
                }
            });

            // coming from main activity will create a new item in the database
        }else if(intent.getStringExtra("incomingFrom").equals("MainActivity")){

            (findViewById(R.id.saveBtn)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(titleET.getText().toString().isEmpty()){
                        Toast.makeText(NewMovieActivity.this, R.string.movieTitleIsRequired, Toast.LENGTH_SHORT).show();
                    }else{
                        ContentValues values = new ContentValues();
                        values.put(FavoritesContract.COLUMN_TITLE, titleET.getText().toString());
                        values.put(FavoritesContract.COLUMN_BODY, bodyET.getText().toString());
                        values.put(FavoritesContract.COLUMN_IMAGE, imageUrlET.getText().toString());
                        values.put(FavoritesContract.COLUMN_RELEASE_DATE, releaseDateET.getText().toString());

                        long newItem = sqlHelper.getWritableDatabase().insert(FavoritesContract.TABLE_NAME, null, values);
                        if(newItem == -1){
                            Toast.makeText(NewMovieActivity.this, getString(R.string.erorSavingItem), Toast.LENGTH_SHORT).show();
                        }else
                            Toast.makeText(NewMovieActivity.this, getString(R.string.itemSavedSuccessfuly), Toast.LENGTH_SHORT).show();
                    }
                    //pressing the save button will add the movie to database and take the user to "favorites" activity using intent:
                    Intent intent = new Intent(NewMovieActivity.this, FavoritesActivity.class);
                    startActivity(intent);
                }
            });
            //coming from "favorites" activity will update the item with the new content provided
        }else if(intent.getStringExtra("incomingFrom").equals("FavoritesActivity")){
            final long dataBaseId = intent.getLongExtra("movieDataId", 0);

            (findViewById(R.id.saveBtn)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (titleET.getText().toString().isEmpty()) {
                        Toast.makeText(NewMovieActivity.this, getString(R.string.movieTitleIsRequired), Toast.LENGTH_SHORT).show();
                    } else {
                        ContentValues values = new ContentValues();
                        values.put(FavoritesContract.COLUMN_TITLE, titleET.getText().toString());
                        values.put(FavoritesContract.COLUMN_BODY, bodyET.getText().toString());
                        values.put(FavoritesContract.COLUMN_IMAGE, imageUrlET.getText().toString());
                        values.put(FavoritesContract.COLUMN_RELEASE_DATE, releaseDateET.getText().toString());
                        values.put(FavoritesContract.COLUMN_UNIQ_ID, intent.getIntExtra("movieUniqId", 0));

                        String [] whereArgs = {String.valueOf(dataBaseId)};
                        db = sqlHelper.getReadableDatabase();
                        db.update(FavoritesContract.TABLE_NAME, values, FavoritesContract.COLUMN_ID + "=?", whereArgs);

                        Intent returnIntent = new Intent();
                        setResult(RESULT_OK, returnIntent);
                        finish();
                    }
                }
            });
        }

        //will try to show an image using its URL, if there is no url, a toast will be poped up
        (findViewById(R.id.showImageIV)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Picasso.with(NewMovieActivity.this).load(imageUrlET.getText().toString().trim()).into(movieIV);
                }catch (IllegalArgumentException ex){
                    Toast.makeText(NewMovieActivity.this, "Bad URL", Toast.LENGTH_SHORT).show();
                }
            }
        });


        (findViewById(R.id.cancelBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    //a method to check if an item with the specific id exists in database
    private boolean CheckIfValueExistsInDataBase(String id) {

        sqlHelper = new FavoritesSqlHelper(this);
        db = sqlHelper.getReadableDatabase();
        String [] columns = {FavoritesContract.COLUMN_UNIQ_ID};
        String selection = FavoritesContract.COLUMN_UNIQ_ID + "=?";
        String [] selectionArgs = {id};

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
}
