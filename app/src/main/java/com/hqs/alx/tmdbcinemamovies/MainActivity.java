package com.hqs.alx.tmdbcinemamovies;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hqs.alx.tmdbcinemamovies.data.FavoritesContract;
import com.hqs.alx.tmdbcinemamovies.data.FavoritesSqlHelper;
import com.squareup.picasso.Picasso;
import com.startapp.android.publish.adsCommon.StartAppAd;
import com.startapp.android.publish.adsCommon.StartAppSDK;

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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    AlertDialog.Builder builder;
    ArrayList<MoviesFromApiList> moviesList;
    FavoritesSqlHelper sqlHelper;
    RecyclerView mostPopularHorizontalRecycler;
    final String mostPopularQuery = "movie/popular";
    RecyclerView nowPlayingHorizontalRecycler;
    final String nowPlayingQuery = "movie/now_playing";
    RecyclerView upComingMovieRecyclerView;
    final String upComingMovieQuery = "movie/upcoming";
    RecyclerView tvSeriesRecyclerView;
    final String tvSeriesQuery = "tv/popular";
    HorizontalAdapter recyclerHorizontalAdapter;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StartAppSDK.init(this, "211505544", false);
        setContentView(R.layout.activity_main);

        mostPopularHorizontalRecycler = (RecyclerView) findViewById(R.id.horizontal_most_popular_recycler_view);
        nowPlayingHorizontalRecycler = (RecyclerView) findViewById(R.id.horizontal_coming_soon_recycler_view);
        upComingMovieRecyclerView = (RecyclerView) findViewById(R.id.up_coming_recycler_view);
        tvSeriesRecyclerView = (RecyclerView) findViewById(R.id.tv_series_recycler_view);
         /*
         check if there is internet connection through "NetworkHelper" class
         */
        if(NetworkHelper.isInternetAvailable(MainActivity.this))
        {
            //AsyncTask to get the most popular movies
            MyLongTaskHelper mostPopularTask = new MyLongTaskHelper();
            mostPopularTask.execute(mostPopularQuery);
            //AsyncTask to get the now in cinemas movies
            MyLongTaskHelper nowPlayingTask = new MyLongTaskHelper();
            nowPlayingTask.execute(nowPlayingQuery);
            //AsyncTask to get the upcoming movies
            MyLongTaskHelper upComingTask = new MyLongTaskHelper();
            upComingTask.execute(upComingMovieQuery);
            //AsyncTask to get the most popular tv series
            MyLongTaskHelper tvSeriesTask = new MyLongTaskHelper();
            tvSeriesTask.execute(tvSeriesQuery);
        }else
        {
            Toast.makeText(MainActivity.this,getString(R.string.noInternetConnection),Toast.LENGTH_LONG).show();
        }

        //creating and setting the AlertDialog which provides 3 choises: "search on line" , "add your own movie" or "cancell"
        builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.chooseFromTheAboveDialogHeader)).setMessage(getString(R.string.dialogBuilderMessage));
        builder.setPositiveButton(getString(R.string.search), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(MainActivity.this, SearchOnLineActivity.class);
                startActivity(intent);
            }
        });
        builder.setNegativeButton(getString(R.string.addDialogBtn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(MainActivity.this, NewMovieActivity.class);
                intent.putExtra("incomingFrom", "MainActivity");
                startActivity(intent);
            }
        });
        builder.setNeutralButton(getString(R.string.cancell), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

    }

    //My class to work on the api (getting information via JASON)
    public class MyLongTaskHelper extends AsyncTask<String, String, String> {

        ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        String queryToCheck = "";

        @Override
        protected void onPreExecute() {
            dialog.setMessage(getString(R.string.loading));
            dialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {

            String allLines = "";
            String query = strings[0];
            queryToCheck = query;

            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://api.themoviedb.org/3/" + query + "?api_key=b181bfa3152834d09a533638b87ea4c6&language=en-US&page=1")
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

        protected void onPostExecute(String jsonText) {
            //a list for all the movies (most popular, now in theaters etc...)
            // the list is overided every time "MylongTaskHelper" is called
            moviesList = new ArrayList<MoviesFromApiList>();

            recyclerHorizontalAdapter = new HorizontalAdapter(moviesList);
            LinearLayoutManager horizontalLayoutManagaer = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);

            try {
                JSONObject mainObject = new JSONObject(jsonText);
                JSONArray resultArray = mainObject.getJSONArray("results");

                //getting the required information from json
                if(queryToCheck.equals(tvSeriesQuery)){
                    for(int i = 0 ; i < resultArray.length() ; i ++){
                        JSONObject singleResult = resultArray.getJSONObject(i);
                        String title = singleResult.getString("name");
                        String description = singleResult.getString("overview");
                        String voteAverage = singleResult.getString("vote_average");
                        String voteCount = singleResult.getString("vote_count");
                        String imagePath = "";
                        String bigImage = "";
                        String backdrop_path = singleResult.getString("backdrop_path");
                        String poster_path = singleResult.getString("poster_path");
                        if(poster_path.contentEquals("null")){
                            imagePath = "";
                        }else{
                            imagePath = "https://image.tmdb.org/t/p/w150" + singleResult.getString("poster_path");
                        }
                        if(backdrop_path.contentEquals("null")){
                            bigImage = imagePath;
                        }else{
                            bigImage = "https://image.tmdb.org/t/p/w500" + singleResult.getString("backdrop_path");
                        }
                        String releaseDate = singleResult.getString("first_air_date");
                        String id = singleResult.getString("id");
                        boolean isChecked = false;

                        if(CheckIfValueExistsInDataBase(id)){
                            isChecked = true;
                        }

                        boolean isAMovie = false;
                        moviesList.add(new MoviesFromApiList(title, description, imagePath, releaseDate, id, isChecked, bigImage, voteAverage, voteCount, isAMovie));
                    }
                    tvSeriesRecyclerView.setLayoutManager(horizontalLayoutManagaer);
                    tvSeriesRecyclerView.setAdapter(recyclerHorizontalAdapter);
                }else{
                    for(int i = 0; i < resultArray.length() ; i ++){
                        JSONObject singleResult = resultArray.getJSONObject(i);
                        String title = singleResult.getString("title");
                        String description = singleResult.getString("overview");
                        String voteAverage = singleResult.getString("vote_average");
                        String voteCount = singleResult.getString("vote_count");
                        String imagePath = "";
                        String bigImage = "";
                        String backdrop_path = singleResult.getString("backdrop_path");
                        String poster_path = singleResult.getString("poster_path");
                        if(poster_path.contentEquals("null")){
                            imagePath = "";
                        }else{
                            imagePath = "https://image.tmdb.org/t/p/w150" + singleResult.getString("poster_path");
                        }
                        if(backdrop_path.contentEquals("null")){
                            bigImage = imagePath;
                        }else{
                            bigImage = "https://image.tmdb.org/t/p/w500" + singleResult.getString("backdrop_path");
                        }
                        String releaseDate = singleResult.getString("release_date");
                        String id = singleResult.getString("id");
                        boolean isChecked = false;

                        if(CheckIfValueExistsInDataBase(id)){
                            isChecked = true;
                        }
                        boolean isAMovie = true;
                        moviesList.add(new MoviesFromApiList(title, description, imagePath, releaseDate, id, isChecked, bigImage, voteAverage, voteCount, isAMovie));
                    }
                }

                //checking what is the query in order to set it to the right recyclerView (most popular, now playing or upcoming)
                if(queryToCheck.equals(mostPopularQuery)){
                    mostPopularHorizontalRecycler.setLayoutManager(horizontalLayoutManagaer);
                    mostPopularHorizontalRecycler.setAdapter(recyclerHorizontalAdapter);
                }else if(queryToCheck.equals(nowPlayingQuery)){
                    nowPlayingHorizontalRecycler.setLayoutManager(horizontalLayoutManagaer);
                    nowPlayingHorizontalRecycler.setAdapter(recyclerHorizontalAdapter);
                }else if(queryToCheck.equals(upComingMovieQuery)){
                    upComingMovieRecyclerView.setLayoutManager(horizontalLayoutManagaer);
                    upComingMovieRecyclerView.setAdapter(recyclerHorizontalAdapter);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            //closing the aler dialog so it wont continue showing after the task is done
            if(dialog.isShowing())
                dialog.dismiss();
        }
    }

    //a method to check if a single item already exists in users' database or not
    //the method returns true if there is an item with the specific "id" - else - false
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

    //creating menu and inflating it with the "main menu" xml file
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //a different intent starts for each menu item
        //for the "addNewMovieItem" (the + symbole UI) the alertDialog will be shown.
        if(item.getItemId() == R.id.favoritesItem){
            Intent intent = new Intent(this, FavoritesActivity.class);
            startActivity(intent);
        }else if(item.getItemId() == R.id.addANewMovieItem){
            builder.show();
        }else if(item.getItemId() == R.id.searchOnLineItem){
            Intent intent = new Intent(this, SearchOnLineActivity.class);
            startActivity(intent);
        }
        return true;
    }

    // an adapter for the RecyclerView
    public class HorizontalAdapter extends RecyclerView.Adapter<HorizontalAdapter.MyViewHolder> {

        // items list (will be recieved when get called
        private ArrayList<MoviesFromApiList> horizontalList;

        //MyViewHolder describes the item view and its place within the RecyclerView
        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView movieTitleTV;
            public ImageView movieImageIV;
            public ImageView likeIV;
            public ImageView shareIV;
            public View currentView;

            public MyViewHolder(View view) {
                super(view);
                movieTitleTV = (TextView) view.findViewById(R.id.movieTitleRecyclerTV);
                movieImageIV = (ImageView) view.findViewById(R.id.imageRecyclerIV);
                likeIV = (ImageView) view.findViewById(R.id.likeImageView);
                shareIV = (ImageView) view.findViewById(R.id.shareImageView);
                currentView = (View) view.findViewById(R.id.linearLayoutItemView);
            }
        }

        public HorizontalAdapter(ArrayList<MoviesFromApiList> horizontalList) {
            this.horizontalList = horizontalList;
        }

        //inflating the single item view for recyclerView
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.horizontal_recycler_single_view, parent, false);

            MyViewHolder holder = new MyViewHolder(itemView);
            return holder;
        }

        //binding the right information for each item view
        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            final int id = holder.getItemViewType();
            final MoviesFromApiList  singleItemList = horizontalList.get(id);
            holder.movieTitleTV.setText(singleItemList.getTitle());

            if(singleItemList.isChecked()){
                holder.likeIV.setImageResource(R.drawable.like_red);
                holder.likeIV.setTag(R.drawable.like_red);
            }
            else{
                holder.likeIV.setImageResource(R.drawable.like);
                holder.likeIV.setTag(R.drawable.like);
            }

            sqlHelper = new FavoritesSqlHelper(MainActivity.this);
            db = sqlHelper.getReadableDatabase();

            holder.likeIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int likeIVID = (int)holder.likeIV.getTag();
                    if(likeIVID == R.drawable.like){
                        holder.likeIV.setTag(R.drawable.like_red);
                        holder.likeIV.setImageResource(R.drawable.like_red);
                        singleItemList.setChecked(true);

                        if(!CheckIfValueExistsInDataBase(singleItemList.getId())){
                            ContentValues values = new ContentValues();
                            values.put(FavoritesContract.COLUMN_TITLE, singleItemList.getTitle());
                            values.put(FavoritesContract.COLUMN_BODY, singleItemList.getDescription());
                            values.put(FavoritesContract.COLUMN_IMAGE, singleItemList.getImage());
                            values.put(FavoritesContract.COLUMN_RELEASE_DATE, singleItemList.getDateReleased());
                            values.put(FavoritesContract.COLUMN_UNIQ_ID, singleItemList.getId());
                            values.put(FavoritesContract.COLUMN_RATING, singleItemList.getVotesAverage());
                            values.put(FavoritesContract.COLUMN_TOTAL_VOTES, singleItemList.getVotesTotal());
                            values.put(FavoritesContract.COLUMN_BIG_IMAGE, singleItemList.getBigImage());
                            if(singleItemList.isAmovieOrSeries()){
                                values.put(FavoritesContract.COLUMN_MOVIE_OR_NOT, FavoritesContract.isAMovie);
                            }else if(!singleItemList.isAmovieOrSeries()){
                                values.put(FavoritesContract.COLUMN_MOVIE_OR_NOT, FavoritesContract.isNotAMovie);
                            }

                            long newRow = sqlHelper.getWritableDatabase().insert(FavoritesContract.TABLE_NAME, null, values);
                            if(newRow == -1){
                                Toast.makeText(MainActivity.this, getString(R.string.erorSavingItem), Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(MainActivity.this, getString(R.string.itemSavedSuccessfuly), Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(MainActivity.this, getString(R.string.itemAlreadyExists), Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        holder.likeIV.setImageResource(R.drawable.like);
                        holder.likeIV.setTag(R.drawable.like);
                        singleItemList.setChecked(false);
                        String idToDelete = singleItemList.getId();
                        String [] whereArgs = {idToDelete};
                        int deleteSuccessfulOrNot = db.delete(FavoritesContract.TABLE_NAME, FavoritesContract.COLUMN_UNIQ_ID + "=?", whereArgs);
                        if(deleteSuccessfulOrNot !=0 )
                            Toast.makeText(MainActivity.this, getString(R.string.deleteSuccessfull), Toast.LENGTH_SHORT).show();
                    }

                    recyclerHorizontalAdapter.notifyDataSetChanged();
                    nowPlayingHorizontalRecycler.getAdapter().notifyDataSetChanged();
                }
            });

            if(horizontalList.get(position).getImage().isEmpty()){
                holder.movieImageIV.setImageResource(android.R.drawable.ic_menu_report_image);
            }else{
                Picasso.with(MainActivity.this).load(horizontalList.get(position).getImage()).into(holder.movieImageIV);
            }

            holder.shareIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, getText(R.string.shareMessagePartOne) + " '" + singleItemList.getTitle() + "' " + getText(R.string.shareMessagePartTwo)
                            + "\n" + singleItemList.getImage());
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.shareTitle)));

                }
            });

            //what will be exacuted when pressing a single item:
            //all the items' information will be passed by intent to the "Details" activity
            holder.currentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                    //checking if it a movie item or series item
                    if(horizontalList.get(position).isAmovieOrSeries()){
                        intent.putExtra("isAMovieOrNot", FavoritesContract.isAMovie);
                    }else
                        intent.putExtra("isAMovieOrNot", FavoritesContract.isNotAMovie);

                    intent.putExtra("MovieTitle", horizontalList.get(position).getTitle());
                    intent.putExtra("MovieBody", horizontalList.get(position).getDescription());
                    intent.putExtra("MovieBigImage", horizontalList.get(position).getBigImage());
                    intent.putExtra("MovieDateReleased", horizontalList.get(position).getDateReleased());
                    intent.putExtra("MovieId", horizontalList.get(position).getId());
                    intent.putExtra("MovieAvergae", horizontalList.get(position).getVotesAverage());
                    intent.putExtra("MovieTotalVotes", horizontalList.get(position).getVotesTotal());
                    intent.putExtra("MovieImage" , horizontalList.get(position).getImage());
                    startActivity(intent);
                }
            });
        }

        // returns the item list size
        @Override
        public int getItemCount() {
            return horizontalList.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }
    }

    @Override
    public void onBackPressed() {
        //Move the task containing this activity to the back of the activity stack
        moveTaskToBack(true);
    }
}
