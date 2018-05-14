package com.hqs.alx.tmdbcinemamovies;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.hqs.alx.tmdbcinemamovies.data.FavoritesContract;
import com.hqs.alx.tmdbcinemamovies.data.FavoritesSqlHelper;

public class FavoritesActivity extends AppCompatActivity {

    FavoritesSqlHelper sqlHelper;
    Cursor favoritesMovieCursor;
    MyCursorAdapter adapter;
    SQLiteDatabase db;
    ListView favoritesListView;
    // a number for startActivityForResult
    int RESULT_NUM_FOR_EDITING_FAVORITE_ITEM = 356;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        sqlHelper = new FavoritesSqlHelper(FavoritesActivity.this);
        db = sqlHelper.getReadableDatabase();

        // creating a new cursor to show the saved items
        favoritesMovieCursor = sqlHelper.getReadableDatabase().query(FavoritesContract.TABLE_NAME, null, null, null, null, null, null);
        adapter = new MyCursorAdapter(this, favoritesMovieCursor);
        favoritesListView = (ListView) findViewById(R.id.userFavoritesLV);
        favoritesListView.setAdapter(adapter);

        //when a single item on the list is clicked, it takes the user to "Details" Activity with all the items' information
        favoritesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                favoritesMovieCursor.moveToPosition(i);
                Intent intent = new Intent(FavoritesActivity.this, DetailsActivity.class);
                intent.putExtra("MovieId", favoritesMovieCursor.getString(favoritesMovieCursor.getColumnIndex(FavoritesContract.COLUMN_UNIQ_ID)));
                intent.putExtra("MovieTitle", favoritesMovieCursor.getString(favoritesMovieCursor.getColumnIndex(FavoritesContract.COLUMN_TITLE)));
                intent.putExtra("MovieBody", favoritesMovieCursor.getString(favoritesMovieCursor.getColumnIndex(FavoritesContract.COLUMN_BODY)));
                intent.putExtra("MovieAvergae", favoritesMovieCursor.getString(favoritesMovieCursor.getColumnIndex(FavoritesContract.COLUMN_RATING)));
                intent.putExtra("MovieTotalVotes", favoritesMovieCursor.getString(favoritesMovieCursor.getColumnIndex(FavoritesContract.COLUMN_TOTAL_VOTES)));
                intent.putExtra("MovieBigImage", favoritesMovieCursor.getString(favoritesMovieCursor.getColumnIndex(FavoritesContract.COLUMN_BIG_IMAGE)));
                intent.putExtra("MovieWatchedOrNot", favoritesMovieCursor.getInt(favoritesMovieCursor.getColumnIndex(FavoritesContract.COLUMN_WATCHED)));
                intent.putExtra("isAMovieOrNot", favoritesMovieCursor.getInt(favoritesMovieCursor.getColumnIndex(FavoritesContract.COLUMN_MOVIE_OR_NOT)));
                startActivity(intent);
            }
        });

        // add context menu to item on the list view
        registerForContextMenu(favoritesListView);
    }

    // inflating a menu XML file
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.favorites_single_item_context_menu, menu);
    }

    //what happens when an item from the menu is pressed
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        long id = info.id;

        switch (item.getItemId()){
            // case item "delete" is pressed
            case R.id.favoritesContextItemDelete:
                String [] whereArgs = {String.valueOf(id)};
                db.delete(FavoritesContract.TABLE_NAME, FavoritesContract.COLUMN_ID + "=?", whereArgs);
                //update the cursor after an item was deleted
                Cursor updatedCursor = sqlHelper.getReadableDatabase().query(FavoritesContract.TABLE_NAME, null, null, null, null,null, null);
                updatedCursor.moveToFirst();
                adapter.swapCursor(updatedCursor);
                break;

            //case item "edit" is pressed
            case R.id.favoritesContextItemEdit:
                Cursor cursor = sqlHelper.getReadableDatabase().query(FavoritesContract.TABLE_NAME, null, FavoritesContract.COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
                cursor.moveToFirst();
                String itemTitle = (cursor.getString(cursor.getColumnIndex(FavoritesContract.COLUMN_TITLE)));
                String itemBody = (cursor.getString(cursor.getColumnIndex(FavoritesContract.COLUMN_BODY)));
                String itemImageUrl = (cursor.getString(cursor.getColumnIndex(FavoritesContract.COLUMN_IMAGE)));
                String itemReleaseDate = (cursor.getString(cursor.getColumnIndex(FavoritesContract.COLUMN_RELEASE_DATE)));
                int itemUniqId = (cursor.getInt(cursor.getColumnIndex(FavoritesContract.COLUMN_UNIQ_ID)));

                Intent intent = new Intent(FavoritesActivity.this, NewMovieActivity.class);
                intent.putExtra("incomingFrom", "FavoritesActivity");
                intent.putExtra("movieTitle", itemTitle);
                intent.putExtra("movieBody", itemBody);
                intent.putExtra("movieImageURL", itemImageUrl);
                intent.putExtra("movieReleaseDate", itemReleaseDate);
                intent.putExtra("movieUniqId", itemUniqId);
                intent.putExtra("movieDataId", id);
                startActivityForResult(intent, RESULT_NUM_FOR_EDITING_FAVORITE_ITEM);
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.favorites_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.deleteAllItem:
                db = sqlHelper.getReadableDatabase();
                int numOfDeletedItems = db.delete(FavoritesContract.TABLE_NAME, null, null);
                Toast.makeText(this, getText(R.string.deleteAllToast), Toast.LENGTH_SHORT).show();
                //updating the cursor after all item were deleted - no items will be visible
                Cursor updatedCursor = sqlHelper.getReadableDatabase().query(FavoritesContract.TABLE_NAME, null, null, null, null,null, null);
                adapter.swapCursor(updatedCursor);
                break;

            case R.id.backItem:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // checking if got back from new movie activity after pressing the edit button from context menu
        if(requestCode == RESULT_NUM_FOR_EDITING_FAVORITE_ITEM){
            // checking if the result from NewMovieActivity is "OK"
            if(resultCode == RESULT_OK){
                Cursor updatedCursor = sqlHelper.getReadableDatabase().query(FavoritesContract.TABLE_NAME, null, null, null, null,null, null);
                adapter.swapCursor(updatedCursor);
                Toast.makeText(this, R.string.FavoritesItemUpdated, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        //updating the cursor when returning to this activity (needed to show if an item was marked watched or not watched)
        Cursor updatedCursor = sqlHelper.getReadableDatabase().query(FavoritesContract.TABLE_NAME, null, null, null, null,null, null);
        adapter.swapCursor(updatedCursor);
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(FavoritesActivity.this, MainActivity.class);
        startActivity(intent);

    }
}
