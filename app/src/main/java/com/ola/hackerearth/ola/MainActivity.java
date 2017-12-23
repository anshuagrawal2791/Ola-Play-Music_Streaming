package com.ola.hackerearth.ola;

import android.app.ActionBar;
import android.app.SearchManager;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;

import static java.security.AccessController.getContext;


public class MainActivity extends AppCompatActivity {

    VolleySingleton volleySingleton;
    RequestQueue requestQueue;
    RecyclerView recycler;
    PaginationAdapter myAdapter;
    ProgressBar progressBar;
    TextView info;
    private static final int PAGE_START = 0;
    private static final int SONGS_PER_PAGE = 4;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int TOTAL_PAGES;
    private int currentPage = PAGE_START;
    boolean isSearching=false;
    ImageView retry;
    SearchView searchView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("");
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setStackedBackgroundDrawable(getDrawable(R.mipmap.ic_launcher));
        volleySingleton = VolleySingleton.getinstance();
        requestQueue = volleySingleton.getrequestqueue();

        recycler = (RecyclerView)findViewById(R.id.recycler);
        myAdapter =new PaginationAdapter(this);
        progressBar=(ProgressBar)findViewById(R.id.progressbar);
        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this,LinearLayoutManager.VERTICAL,false);

        recycler.setLayoutManager(layoutManager);
        recycler.setItemAnimator(new DefaultItemAnimator());
        recycler.setAdapter(myAdapter);
        info = (TextView)findViewById(R.id.info);
        retry = (ImageView)findViewById(R.id.retry);
        try {
            fetchSongs(0,SONGS_PER_PAGE-1);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView toolbartitle = (TextView)findViewById(R.id.toolbar_title);
        TextView toolbartitle2 = (TextView)findViewById(R.id.toolbar_title2);
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/ola.otf");
        toolbartitle.setTypeface(custom_font);

        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retry.setVisibility(View.GONE);
                isLoading = true;
                currentPage += 1; //Increment page index to load the next one
                try {
//                    myAdapter.removeLoadingFooter();  // 2
                    isLoading = false;

                    fetchSongs(currentPage*SONGS_PER_PAGE,currentPage*SONGS_PER_PAGE+SONGS_PER_PAGE-1);
                    if (currentPage <= TOTAL_PAGES){
//                        myAdapter.addLoadingFooter();
                    }
                    else {
                        Log.e("r","reached else");
                        isLastPage = true;
                        info.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        recycler.addOnScrollListener(new PaginationScrollListener(layoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage += 1; //Increment page index to load the next one
                try {
//                    myAdapter.removeLoadingFooter();  // 2
                    isLoading = false;

                    fetchSongs(currentPage*SONGS_PER_PAGE,currentPage*SONGS_PER_PAGE+SONGS_PER_PAGE-1);
                    if (currentPage <= TOTAL_PAGES){
//                        myAdapter.addLoadingFooter();
                    }
                    else {
                        Log.e("r","reached else");
                        isLastPage = true;
                        info.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public int getTotalPageCount() {
                return TOTAL_PAGES;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });






    }

    private void fetchSongs(final int start, final int end) throws JSONException{
        Log.e("start and end",""+start+" "+end);
        progressBar.setVisibility(View.VISIBLE);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Constants.GET_SONGS_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String apiResp) {
                        JSONArray response;
                        progressBar.setVisibility(View.GONE);
                        info.setVisibility(View.VISIBLE);
                        try {
                            response = new JSONArray(apiResp);


//                            myAdapter.clearAll();

                            ArrayList<Song> songs = new ArrayList<>();
                            if (response.length()>0) {
                                TOTAL_PAGES = (response.length()+1)/SONGS_PER_PAGE;
                                Log.e("TOTal pages",TOTAL_PAGES+"");
                                for (int i = start; i <=Math.min(response.length()-1,end); i++) {
                                    JSONObject rec = response.getJSONObject(i);

                                    Song song = new Song(rec.getString("song"),rec.getString("url"),rec.getString("artists"),rec.getString("cover_image"));
//                                    myAdapter.addSong(song);
                                    songs.add(song);
                                }
                                Log.e("adding songs",songs.toString());
                                myAdapter.addAll(songs);
                            }
                            else {

                                Toast.makeText(MainActivity.this, "No Songs available", Toast.LENGTH_SHORT).show();
                            }

                            Log.e("resp of songs",response.toString());
                        } catch (JSONException e) {
                            Log.e("error",e.toString());
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                retry.setVisibility(View.VISIBLE);
                currentPage-=1;
                progressBar.setVisibility(View.GONE);

                Toast.makeText(MainActivity.this,"Network Error",Toast.LENGTH_LONG).show();
                Log.e("error",error.toString());
            }
        }
        );
        requestQueue.add(stringRequest);
    }

    abstract class PaginationScrollListener extends RecyclerView.OnScrollListener {
        LinearLayoutManager layoutManager;

        public PaginationScrollListener(LinearLayoutManager layoutManager) {
            this.layoutManager = layoutManager;
        }


@Override
public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
    super.onScrollStateChanged(recyclerView, newState);

    int lastvisibleitemposition = layoutManager.findLastVisibleItemPosition();

    if (lastvisibleitemposition == myAdapter.getItemCount() - 1) {

        if (!isLoading() && !isLastPage&&!isSearching) {
            info.setVisibility(View.GONE);
            isLoading = true;
            loadMoreItems();
        }


    }
}


        protected abstract void loadMoreItems();

        public abstract int getTotalPageCount();

        public abstract boolean isLastPage();

        public abstract boolean isLoading();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                myAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                isSearching=true;
                if(query.matches(""))
                    isSearching=false;
                if(isSearching)
                    info.setVisibility(View.GONE);
                else info.setVisibility(View.VISIBLE);
                myAdapter.getFilter().filter(query);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        // close search view on back button pressed
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            return;
        }
        super.onBackPressed();
    }

}
