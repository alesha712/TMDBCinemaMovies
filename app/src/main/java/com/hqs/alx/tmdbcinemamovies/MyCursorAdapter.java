package com.hqs.alx.tmdbcinemamovies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.hqs.alx.tmdbcinemamovies.data.FavoritesContract;
import com.hqs.alx.tmdbcinemamovies.data.FavoritesSqlHelper;
import com.squareup.picasso.Picasso;

/**
 * Created by Alex on 17/11/2017.
 */

public class MyCursorAdapter extends CursorAdapter {

    public MyCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {

        //inflating the single item view
        View singleView = LayoutInflater.from(context).inflate(R.layout.favorites_single_list_item, null);
        return singleView;
    }

    //binding the information to each view
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView title = (TextView) view.findViewById(R.id.favoritesTitleTV);
        TextView body = (TextView) view.findViewById(R.id.favoritesBodyTV);
        TextView releaseDate = (TextView) view.findViewById(R.id.favoritesReleaseDateTV);
        ImageView image = (ImageView) view.findViewById(R.id.favoritesSingleImage);
        ImageView watchedIV = (ImageView) view.findViewById(R.id.watchedIV);

        final int watchedOrNot = cursor.getInt(cursor.getColumnIndex(FavoritesContract.COLUMN_WATCHED));
        if( watchedOrNot == 0){
            watchedIV.setVisibility(View.GONE);
        }else
            watchedIV.setVisibility(View.VISIBLE);

        title.setText(cursor.getString(cursor.getColumnIndex(FavoritesContract.COLUMN_TITLE)));
        body.setText(cursor.getString(cursor.getColumnIndex(FavoritesContract.COLUMN_BODY)));
        releaseDate.setText(cursor.getString(cursor.getColumnIndex(FavoritesContract.COLUMN_RELEASE_DATE)));
        String imageURL = cursor.getString(cursor.getColumnIndex(FavoritesContract.COLUMN_IMAGE));
        // in case there wasnt received any image url from the api, or if a person added its own movie without an image, the default will be shown
        if (imageURL.isEmpty()){
            image.setImageResource(android.R.drawable.ic_menu_report_image);

        // if there is a valid image url, it will be shown
        }else {
            Picasso.with(context).load(cursor.getString(cursor.getColumnIndex(FavoritesContract.COLUMN_IMAGE))).into(image);
        }
    }
}
