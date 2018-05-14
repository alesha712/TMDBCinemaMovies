package com.hqs.alx.tmdbcinemamovies;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hqs.alx.tmdbcinemamovies.data.FavoritesContract;
import com.hqs.alx.tmdbcinemamovies.data.FavoritesSqlHelper;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Alex on 16/11/2017.
 */

public class MovieListAdapter extends ArrayAdapter<MoviesFromApiList> {

    FavoritesSqlHelper sqlHelper;
    SQLiteDatabase db;

    public MovieListAdapter(Activity context, ArrayList<MoviesFromApiList> newList) {
        super(context, 0, newList);
    }

    //declaring how a single item view in the list will be shown
    @NonNull
    @Override
    public View getView(final int position, @Nullable final View convertView, @NonNull ViewGroup parent) {

        View listItemView = LayoutInflater.from(getContext()).inflate(
                R.layout.single_list_item, parent, false);

       final MoviesFromApiList  currentList = getItem(position);

            // Find the TextView in the single_list_item.xml layout with the ID and set the text to movie title
            TextView movieTitle = (TextView) listItemView.findViewById(R.id.singleItemTitleTV);
            movieTitle.setText(currentList.getTitle());

            // Find the TextView in the single_list_item.xml layout with the ID and set the text to release date
            TextView releaseDate = (TextView) listItemView.findViewById(R.id.singleItemReleaseDateTV);
            releaseDate.setText(currentList.getDateReleased());

            ImageView movieImage = (ImageView) listItemView.findViewById(R.id.singleItemIV);
            if(currentList.getImage().isEmpty()){
                movieImage.setImageResource(android.R.drawable.ic_menu_report_image);
            }else{
                Picasso.with(getContext()).load(currentList.getImage()).into(movieImage);
            }

            //  a checkBox to see if an item exists in database or to add an item to database
            final CheckBox checkBox = (CheckBox) listItemView.findViewById(R.id.addToFavoritesCHB);
            if(currentList.isChecked()){
                checkBox.setChecked(true);
            }else if(!currentList.isChecked()){
                checkBox.setChecked(false);
            }

            //if the checkbox was clicked - the item will be added to database
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    MoviesFromApiList  myMovie = currentList;
                    if(checkBox.isChecked()){
                        myMovie.setChecked(true);
                        myMovie = getItem(position);
                        sqlHelper = new FavoritesSqlHelper(getContext());

                            ContentValues values = new ContentValues();
                                values.put(FavoritesContract.COLUMN_TITLE, currentList.getTitle());
                                values.put(FavoritesContract.COLUMN_BODY, currentList.getDescription());
                                values.put(FavoritesContract.COLUMN_IMAGE, currentList.getImage());
                                values.put(FavoritesContract.COLUMN_RELEASE_DATE, currentList.getDateReleased());
                                values.put(FavoritesContract.COLUMN_UNIQ_ID, currentList.getId());
                                values.put(FavoritesContract.COLUMN_RATING, currentList.getVotesAverage());
                                values.put(FavoritesContract.COLUMN_TOTAL_VOTES, currentList.getVotesTotal());
                                values.put(FavoritesContract.COLUMN_BIG_IMAGE, currentList.getBigImage());

                                long newRow = sqlHelper.getWritableDatabase().insert(FavoritesContract.TABLE_NAME, null, values);
                                if(newRow == -1){
                                    Toast.makeText(getContext(), getContext().getString(R.string.erorSavingItem), Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(getContext(), getContext().getString(R.string.itemSavedSuccessfuly), Toast.LENGTH_SHORT).show();
                                }
                    }else{
                        myMovie.setChecked(false);
                        String idToDelete = myMovie.getId();
                        String [] whereArgs = {idToDelete};
                        sqlHelper = new FavoritesSqlHelper(getContext());
                        db = sqlHelper.getReadableDatabase();
                        int deleteSuccesfulOrNot = db.delete(FavoritesContract.TABLE_NAME, FavoritesContract.COLUMN_UNIQ_ID + "=?", whereArgs);
                        if(deleteSuccesfulOrNot != 0)
                            Toast.makeText(getContext(), getContext().getString(R.string.deleteSuccessfull), Toast.LENGTH_SHORT).show();

                        db.close();
                    }
                }
            });

        return listItemView;
        }
}
