package com.hqs.alx.tmdbcinemamovies;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hqs.alx.tmdbcinemamovies.data.FavoritesContract;
import com.hqs.alx.tmdbcinemamovies.data.FavoritesSqlHelper;

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

public class SearchOnLineActivity extends AppCompatActivity {

    EditText titleFromUserET;
    ListView foundMoviesLV;
    ArrayList<MoviesFromApiList> moviesList;
    MovieListAdapter adapter;
    FavoritesSqlHelper sqlHelper;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_on_line);

        titleFromUserET = (EditText) findViewById(R.id.movieToSearchET);
        // setting the text on the keyboard to "search" + deciding what the button performs
        titleFromUserET.setImeActionLabel("Search", EditorInfo.IME_ACTION_DONE);
        // adding a listener to the editText and performing a search through the "search" button on the keyboard
        titleFromUserET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean action = false;
                if(i == EditorInfo.IME_ACTION_DONE){
                    String inputFromUser = titleFromUserET.getText().toString().trim();
                    MyLongTaskHelper longTaskHelper = new MyLongTaskHelper();
                    longTaskHelper.execute(inputFromUser);
                    action = true;
                    InputMethodManager imm = (InputMethodManager) getSystemService(SearchOnLineActivity.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                }
                return action;
            }
        });
        foundMoviesLV = (ListView) findViewById(R.id.listFromApiLV);

        // creating a context menu for list items (declared below)
        registerForContextMenu(foundMoviesLV);

        //pressing the search image will take the input from the user and send it to the asyncTask - searching for the query in the TMDB database
        (findViewById(R.id.searchIV)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inputFromUser = titleFromUserET.getText().toString().trim();
                MyLongTaskHelper longTaskHelper = new MyLongTaskHelper();
                longTaskHelper.execute(inputFromUser);
            }
        });

        //short click will take the user to "details" activity using all the item information
        foundMoviesLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(SearchOnLineActivity.this, DetailsActivity.class);
                intent.putExtra("MovieTitle", moviesList.get(i).getTitle());
                intent.putExtra("MovieBody", moviesList.get(i).getDescription());
                intent.putExtra("MovieBigImage", moviesList.get(i).getBigImage());
                intent.putExtra("MovieDateReleased", moviesList.get(i).getDateReleased());
                intent.putExtra("MovieId", moviesList.get(i).getId());
                intent.putExtra("MovieAvergae", moviesList.get(i).getVotesAverage());
                intent.putExtra("MovieTotalVotes", moviesList.get(i).getVotesTotal());
                intent.putExtra("MovieImage" , moviesList.get(i).getImage());
                startActivity(intent);
            }
        });
    }

    //inner class to execute the search query using the api from TMDB
    public class MyLongTaskHelper extends AsyncTask <String, String, String>{

        // a dialog progress bar is shown while the task is in prosses
        ProgressDialog dialog = new ProgressDialog(SearchOnLineActivity.this);

        @Override
        protected void onPreExecute() {
            dialog.setMessage(getString(R.string.searcingDialog));
            dialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {

            String allLines = "";
            String movieToSearch = strings[0];
            String query = "";
            try {
                //in case there is spacing in the users' input:
                query = URLEncoder.encode(movieToSearch, "utf-8");
            } catch (UnsupportedEncodingException e) {
                Toast.makeText(SearchOnLineActivity.this, "Bad Input", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            URL url;
            InputStream is = null;
            BufferedReader br;
            String line;
            try {
                url = new URL("https://api.themoviedb.org/3/search/movie?api_key=b181bfa3152834d09a533638b87ea4c6&query=" + query);
                is = url.openStream();  // throws an IOException
                br = new BufferedReader(new InputStreamReader(is));

                while ((line = br.readLine()) != null) {
                    allLines = allLines + line;
                }
            } catch (MalformedURLException mue) {
                mue.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                try {
                    if (is != null) is.close();
                } catch (IOException ioe) {
                    // nothing to see here
                }
            }
            return allLines;
        }

        // allLines is the whole json text
        @Override
        protected void onPostExecute(String jsonText) {

            moviesList = new ArrayList<MoviesFromApiList>();

            try {
                JSONObject mainObject = new JSONObject(jsonText);
                JSONArray resultArray = mainObject.getJSONArray("results");

                //for each result that is found, an item will be added to the list
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
                    //adding each movie that was found to the movieList
                    moviesList.add(new MoviesFromApiList(title, description, imagePath, releaseDate, id, isChecked, bigImage, voteAverage, voteCount, isAMovie));
                }
                //showing the movieList using an adapter (java class "MovieListAdapter")
                adapter = new MovieListAdapter(SearchOnLineActivity.this, moviesList);
                foundMoviesLV.setAdapter(adapter);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            //closing the aler dialog so it wont continue showing after the task is done
            if(dialog.isShowing()){
                dialog.dismiss();
            }
            try {
                adapter.notifyDataSetChanged();
            }catch (NullPointerException ex) {
                Toast.makeText(SearchOnLineActivity.this, getString(R.string.nothingFound), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_item_from_api_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch(item.getItemId()){
            case R.id.editItem:
                String id = moviesList.get(info.position).getId();
                Intent intent = new Intent(SearchOnLineActivity.this, NewMovieActivity.class);
                intent.putExtra("movieTitle", moviesList.get(info.position).getTitle());
                intent.putExtra("movieBody", moviesList.get(info.position).getDescription());
                intent.putExtra("movieImageURL", moviesList.get(info.position).getImage());
                intent.putExtra("movieReleaseDate", moviesList.get(info.position).getDateReleased());
                intent.putExtra("movieUniqId", id);
                intent.putExtra("incomingFrom", "searchOnLineActivity");
                startActivity(intent);
                break;
        }

        return super.onContextItemSelected(item);
    }

    //a method to check if a single item with a specific id exists in users' database
    private boolean CheckIfValueExistsInDataBase(String id) {

        sqlHelper = new FavoritesSqlHelper(SearchOnLineActivity.this);
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

    @Override
    protected void onResume() {
        super.onResume();
        try{
            adapter.notifyDataSetChanged();
        }catch (NullPointerException ex){

        }
    }
}

