package com.techmind.tubeless;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.techmind.tubeless.Sqlite.PostsDatabaseHelper;
import com.techmind.tubeless.adapters.FavouriteGridViewAdapter;
import com.techmind.tubeless.adapters.GenresAlbumsGridAdapter;
import com.techmind.tubeless.adapters.MultiViewAdapter;
import com.techmind.tubeless.config.AppController;
import com.techmind.tubeless.config.ConstURL;
import com.techmind.tubeless.interfaces.OnItemClickListener;
import com.techmind.tubeless.models.YoutubeDataModel;
import com.techmind.tubeless.pojo.VideosGridCategory;
import com.techmind.tubeless.util.ConnectionDetector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.techmind.tubeless.config.ConstURL.CHANNEL_TYPE;
import static com.techmind.tubeless.config.ConstURL.PLAYLIST_TYPE;
import static com.techmind.tubeless.config.ConstURL.VIDEOS_TYPE;
import static com.techmind.tubeless.util.AnimationUtils.animateView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final boolean DEBUG = !BuildConfig.BUILD_TYPE.equals("release");
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    FrameLayout mContentFrame;
    //    private RecyclerView mList_videos = null;
    private MultiViewAdapter adapter = null;
    private static final String PREFERENCES_FILE = "mymaterialapp_settings";
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    private boolean mUserLearnedDrawer;
    private boolean mFromSavedInstanceState;
    private int mCurrentSelectedPosition;
    private TextView empty_view;
    private ProgressBar loadingProgressBar;
    private View errorPanelRoot;
    private Button errorButtonRetry;
    private TextView errorTextView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private List<YoutubeDataModel> youtubeDataModelsList;
    private List<VideosGridCategory> albumList;
    private GenresAlbumsGridAdapter genresAlbumsGridAdapter;


    private String pageToken;

    JSONObject jsonObjUserDetail = new JSONObject();
    private String kind;
    private String channelId = "";
    private String playListID = "";
    ArrayList<String> videosIdArrayList = new ArrayList<String>();
    ArrayList<String> channelIdArrayList = new ArrayList<String>();
    ArrayList<String> playListIdArrayList = new ArrayList<String>();
    private HashMap<String, YoutubeDataModel> hmTempListData = new HashMap<>();
    private View trending_video_categories;
    private View musics_category_offline;
    private View tvShow_video_categories;
    private View movies_video_categories;
    private View news_video_categories;
    private View comedy_video_categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_drawer);
//        FloatingActionButton fab = findViewById(R.id.fab);
        mUserLearnedDrawer = Boolean.valueOf(readSharedSetting(this, PREF_USER_LEARNED_DRAWER, "false"));

        mDrawerLayout = (DrawerLayout) findViewById(R.id.nav_drawer);
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        loadingProgressBar = findViewById(R.id.loading_progress_bar);
        errorPanelRoot = findViewById(R.id.error_panel);
        errorButtonRetry = findViewById(R.id.error_button_retry);
        errorTextView = findViewById(R.id.error_message_view);
        empty_view = (TextView) findViewById(R.id.empty_view);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        LinearLayout linearLayoutCategoryContainer = findViewById(R.id.linearLayoutCategoryContainer);
        musics_category_offline = getLayoutInflater().inflate(R.layout.musics_category_offline, null);
        trending_video_categories = getLayoutInflater().inflate(R.layout.dynamic_home_video_categories, null);
        tvShow_video_categories = getLayoutInflater().inflate(R.layout.dynamic_home_video_categories, null);
        movies_video_categories = getLayoutInflater().inflate(R.layout.dynamic_home_video_categories, null);
        news_video_categories = getLayoutInflater().inflate(R.layout.dynamic_home_video_categories, null);
        comedy_video_categories = getLayoutInflater().inflate(R.layout.dynamic_home_video_categories, null);
        TextView moreGenresTextView = musics_category_offline.findViewById(R.id.moreGenresTextView);
//        linearLayoutCategoryContainer.addView(trending_video_categories,1);
        linearLayoutCategoryContainer.addView(musics_category_offline,0);
//        linearLayoutCategoryContainer.addView(tvShow_video_categories);
//        linearLayoutCategoryContainer.addView(movies_video_categories);
//        linearLayoutCategoryContainer.addView(news_video_categories);
//        linearLayoutCategoryContainer.addView(comedy_video_categories);
        setUpToolbar();
        setUpNavDrawer();

        getBookMarkedDate();

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getBookMarkedDate();
            }
        });
        errorButtonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getBookMarkedDate();
            }
        });
        moreGenresTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent genresAlbumsListIntent = new Intent(MainActivity.this, GenresAlbumsGridList.class);
                startActivity(genresAlbumsListIntent);
            }
        });
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                switch (menuItem.getItemId()) {
                    case R.id.navigation_item_1:
                        Intent genresAlbumsListIntent = new Intent(MainActivity.this, GenresAlbumsGridList.class);
                        startActivity(genresAlbumsListIntent);
//                        Snackbar.make(mContentFrame, "Item One", Snackbar.LENGTH_SHORT).show();
                        mCurrentSelectedPosition = 0;
                        return true;
                    case R.id.navigation_item_2:
//                        Snackbar.make(mContentFrame, "Item Two", Snackbar.LENGTH_SHORT).show();
                        mCurrentSelectedPosition = 1;
                        return true;
                    default:
                        return true;
                }
            }
        });

//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
//                startActivity(intent);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
//                    overridePendingTransition(R.animator.right_in, R.animator.left_out);
//                }
//            }
//        });
    }

    private void prepareGenresAlbums() {
        RecyclerView genresAlbumsHorizontalRecyclerView = (RecyclerView) musics_category_offline.findViewById(R.id.genresAlbumsHorizontalRV);
        prepareAlbums();
        //Horizontal view=0 change view
        genresAlbumsGridAdapter = new GenresAlbumsGridAdapter(this, albumList, 0);
        if (albumList != null) {
            genresAlbumsHorizontalRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            genresAlbumsHorizontalRecyclerView.setItemAnimator(new DefaultItemAnimator());
            genresAlbumsHorizontalRecyclerView.setNestedScrollingEnabled(false);
            genresAlbumsHorizontalRecyclerView.setAdapter(genresAlbumsGridAdapter);
        } else {
            musics_category_offline.findViewById(R.id.musicsCategoryLayout).setVisibility(View.GONE);
        }
    }

    private void prepareVideosGridRV(ArrayList<YoutubeDataModel> mListData, RecyclerView recyclerView,
                                     LinearLayout contentLayout, TextView videosTitleTv, String title) {
        if (videosTitleTv != null) {
            videosTitleTv.setText(title);
        }
        System.out.println("**Prepare Videos Grid RV ListData**" + "videosTitleTv=" + title );
        if (mListData != null && mListData.size() > 0) {
            System.out.println("**Prepare Videos Grid RV ListData** mListData" + mListData.size());
            contentLayout.setVisibility(View.VISIBLE);
            ArrayList<YoutubeDataModel> videosBookmarkedList = new ArrayList<YoutubeDataModel>();
            for (YoutubeDataModel youtubeDataModel : mListData) {
                if ("youtube#video".equals(youtubeDataModel.getKind())) {
                    videosBookmarkedList.add(youtubeDataModel);
                }
            }
            albumList = new ArrayList<>();
            //Horizontal view=0 change view
            FavouriteGridViewAdapter bookmarkVideosGridAdapter = new FavouriteGridViewAdapter(this, videosBookmarkedList, videosBookmarkedList, new OnItemClickListener() {
                @Override
                public void onItemClick(YoutubeDataModel item) {
                    onItemClickNavigation(item);
                }
            });
            RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.addItemDecoration(new GenresAlbumsGridList.GridSpacingItemDecoration(2, dpToPx(4), true));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setNestedScrollingEnabled(false);
            recyclerView.setAdapter(bookmarkVideosGridAdapter);
        } else {
            contentLayout.setVisibility(View.GONE);
        }
    }

    private void prepareFavouriteChannelGridRV(ArrayList<YoutubeDataModel> mListData) {
        if (mListData != null && mListData.size() > 0) {
            findViewById(R.id.favouriteChannelLayout).setVisibility(View.VISIBLE);
            ArrayList<YoutubeDataModel> videosBookmarkedList = new ArrayList<YoutubeDataModel>();
            for (YoutubeDataModel youtubeDataModel : mListData) {
                if ("youtube#channel".equals(youtubeDataModel.getKind())) {
                    videosBookmarkedList.add(youtubeDataModel);
                }
            }
            RecyclerView favouriteChannelHorizontalRV = (RecyclerView) findViewById(R.id.favouriteChannelHorizontalRV);
            albumList = new ArrayList<>();
            //Horizontal view=0 change view
            FavouriteGridViewAdapter bookmarkVideosGridAdapter = new FavouriteGridViewAdapter(this, videosBookmarkedList, videosBookmarkedList, new OnItemClickListener() {
                @Override
                public void onItemClick(YoutubeDataModel item) {
                    onItemClickNavigation(item);
                }
            });
            RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
            favouriteChannelHorizontalRV.setLayoutManager(mLayoutManager);
            favouriteChannelHorizontalRV.addItemDecoration(new GenresAlbumsGridList.GridSpacingItemDecoration(2, dpToPx(4), true));
            favouriteChannelHorizontalRV.setItemAnimator(new DefaultItemAnimator());
            favouriteChannelHorizontalRV.setNestedScrollingEnabled(false);
            favouriteChannelHorizontalRV.setAdapter(bookmarkVideosGridAdapter);
        } else {
            findViewById(R.id.favouriteChannelLayout).setVisibility(View.GONE);
        }
    }

    private void prepareFavouritePlaylistGridRV(ArrayList<YoutubeDataModel> mListData) {
        if (mListData != null && mListData.size() > 0) {
            findViewById(R.id.favouritePlaylistLayout).setVisibility(View.VISIBLE);
            RecyclerView favouritePlaylistHorizontalRV = (RecyclerView) findViewById(R.id.favouritePlaylistHorizontalRV);
            ArrayList<YoutubeDataModel> videosBookmarkedList = new ArrayList<YoutubeDataModel>();
            for (YoutubeDataModel youtubeDataModel : mListData) {
                if ("youtube#playlist".equals(youtubeDataModel.getKind())) {
                    videosBookmarkedList.add(youtubeDataModel);
                }
            }
            albumList = new ArrayList<>();
            //Horizontal view=0 change view
            FavouriteGridViewAdapter bookmarkVideosGridAdapter = new FavouriteGridViewAdapter(this, videosBookmarkedList, videosBookmarkedList, new OnItemClickListener() {
                @Override
                public void onItemClick(YoutubeDataModel item) {
                    onItemClickNavigation(item);
                }
            });
            RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
            favouritePlaylistHorizontalRV.setLayoutManager(mLayoutManager);
            favouritePlaylistHorizontalRV.addItemDecoration(new GenresAlbumsGridList.GridSpacingItemDecoration(2, dpToPx(4), true));
            favouritePlaylistHorizontalRV.setItemAnimator(new DefaultItemAnimator());
            favouritePlaylistHorizontalRV.setNestedScrollingEnabled(false);
            favouritePlaylistHorizontalRV.setAdapter(bookmarkVideosGridAdapter);
        } else {
            findViewById(R.id.favouritePlaylistLayout).setVisibility(View.GONE);
        }

    }

    private void onItemClickNavigation(YoutubeDataModel youtubeDataModel) {
        Intent intent;

        if (youtubeDataModel.getKind().equals(CHANNEL_TYPE)) {
            intent = new Intent(MainActivity.this, ChannelPlaylistActivityWithoutAnim.class);
            intent.putExtra(YoutubeDataModel.class.toString(), youtubeDataModel);
            intent.putExtra("activity", "MainActivity");
            startActivity(intent);
        } else if (youtubeDataModel.getKind().equals(VIDEOS_TYPE)) {
            intent = new Intent(MainActivity.this, VideoPlayerActivity.class);
            intent.putExtra(YoutubeDataModel.class.toString(), youtubeDataModel);
            intent.putExtra("activity", "MainActivity");
            startActivity(intent);
        } else if (youtubeDataModel.getKind().equals(PLAYLIST_TYPE)) {
            intent = new Intent(MainActivity.this, PlayListActivity.class);
            intent.putExtra(YoutubeDataModel.class.toString(), youtubeDataModel);
            intent.putExtra("activity", "MainActivity");
            startActivity(intent);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            overridePendingTransition(R.animator.right_in, R.animator.left_out);
        }

    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    private void getBookMarkedDate() {
        ConnectionDetector connectionDetector = new ConnectionDetector(getApplicationContext());
        if (connectionDetector.isConnectingToInternet()) {
            errorPanelRoot.setVisibility(View.GONE);
            //Music category offline grid view
            prepareGenresAlbums();
            // Get all posts from database
            youtubeDataModelsList = PostsDatabaseHelper.getInstance(getApplicationContext()).getAllPosts();
            if (youtubeDataModelsList.size() > 0) {
                empty_view.setVisibility(View.GONE);
            } else {
                empty_view.setVisibility(View.VISIBLE);
            }

            //Trending Videos
           /* ArrayList<YoutubeDataModel> trendingVideoListData = new ArrayList<>();
            HashMap<String, YoutubeDataModel> trendingVideoHmData = new HashMap<>();
            String trendingUrl = "https://www.googleapis.com/youtube/v3/search?part=snippet&chart=mostPopular&maxResults=10&key=" + ConstURL.GOOGLE_YOUTUBE_API_KEY;
            getSearchListFromServer(trendingUrl, "Trending Videos",
                    trending_video_categories.findViewById(R.id.trendingVideosRc),
                    trending_video_categories.findViewById(R.id.trendingVideosLayout),
                    trending_video_categories.findViewById(R.id.dynamicVideosTitle), trendingVideoListData, trendingVideoHmData);*/

/*            //Tv Shows
            ArrayList<YoutubeDataModel> tvShowsVideoListData = new ArrayList<>();
            HashMap<String, YoutubeDataModel> tvShowsVideoHmData = new HashMap<>();
            String tvShowsUrl = "https://www.googleapis.com/youtube/v3/search?part=snippet&chart=mostPopular&q=Tv shows&maxResults=10&key=" + ConstURL.GOOGLE_YOUTUBE_API_KEY;
            getSearchListFromServer(tvShowsUrl, "Tv Shows", tvShow_video_categories.findViewById(R.id.trendingVideosRc), tvShow_video_categories.findViewById(R.id.trendingVideosLayout),
                    tvShow_video_categories.findViewById(R.id.dynamicVideosTitle), tvShowsVideoListData, tvShowsVideoHmData);

            //Comedy
            ArrayList<YoutubeDataModel> comedyVideoListData = new ArrayList<>();
            HashMap<String, YoutubeDataModel> comedyVideoHmData = new HashMap<>();
            String comedyUrl= "https://www.googleapis.com/youtube/v3/search?part=snippet&chart=mostPopular&q=comedy&maxResults=10&key=" + ConstURL.GOOGLE_YOUTUBE_API_KEY;
            getSearchListFromServer(comedyUrl, "Comedy", comedy_video_categories.findViewById(R.id.trendingVideosRc),
                    comedy_video_categories.findViewById(R.id.trendingVideosLayout),
                    comedy_video_categories.findViewById(R.id.dynamicVideosTitle), comedyVideoListData, comedyVideoHmData);

            //News
            ArrayList<YoutubeDataModel> newsVideoListData = new ArrayList<>();
            HashMap<String, YoutubeDataModel> newsVideoHmData = new HashMap<>();
            String newsUrl = "https://www.googleapis.com/youtube/v3/search?part=snippet&chart=mostPopular&q=news&maxResults=10&key=" + ConstURL.GOOGLE_YOUTUBE_API_KEY;
            getSearchListFromServer(newsUrl, "News", news_video_categories.findViewById(R.id.trendingVideosRc), news_video_categories.findViewById(R.id.trendingVideosLayout),
                    news_video_categories.findViewById(R.id.dynamicVideosTitle), newsVideoListData, newsVideoHmData);
            //Movies
            ArrayList<YoutubeDataModel> moviesVideoListData = new ArrayList<>();
            HashMap<String, YoutubeDataModel> moviesVideoHmData = new HashMap<>();
            String moviesUrl = "https://www.googleapis.com/youtube/v3/search?part=snippet&chart=mostPopular&q=movies&maxResults=10&key=" + ConstURL.GOOGLE_YOUTUBE_API_KEY;
            getSearchListFromServer(moviesUrl, "Movies", movies_video_categories.findViewById(R.id.trendingVideosRc), movies_video_categories.findViewById(R.id.trendingVideosLayout),
                    movies_video_categories.findViewById(R.id.dynamicVideosTitle), moviesVideoListData, moviesVideoHmData);*/

        } else {
            if (youtubeDataModelsList != null && youtubeDataModelsList.size() > 0) {
                youtubeDataModelsList.clear();
            }
            errorPanelRoot.setVisibility(View.VISIBLE);
            errorTextView.setText("No Network");
        }
        if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
        if (loadingProgressBar != null) animateView(loadingProgressBar, false, 0);
//        String url=search_url = "https://www.googleapis.com/youtube/v3/search?part=snippet&chart=mostPopular&q=" + genresNameTitle+" "+" songs and music video" + "&type=video&maxResults=10&key="

//        Favourite videos
        prepareVideosGridRV((ArrayList<YoutubeDataModel>) youtubeDataModelsList, findViewById(R.id.favouriteVideosHorizontalRV)
                , findViewById(R.id.favouriteVideosLayout), null, "");
//        FavouriteChannel
        prepareFavouriteChannelGridRV((ArrayList<YoutubeDataModel>) youtubeDataModelsList);
//        FavouritePlaylist
        prepareFavouritePlaylistGridRV((ArrayList<YoutubeDataModel>) youtubeDataModelsList);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_search:
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
                    overridePendingTransition(R.animator.right_in, R.animator.left_out);
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION, 0);
        Menu menu = mNavigationView.getMenu();
        menu.getItem(mCurrentSelectedPosition).setChecked(true);
    }

    private void setUpToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
    }

    private void setUpNavDrawer() {
        if (mToolbar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mToolbar.setNavigationIcon(R.drawable.ic_drawer);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

        if (!mUserLearnedDrawer) {
            mDrawerLayout.openDrawer(GravityCompat.START);
            mUserLearnedDrawer = true;
            saveSharedSetting(this, PREF_USER_LEARNED_DRAWER, "true");
        }

    }

    public static void saveSharedSetting(Context ctx, String settingName, String settingValue) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(settingName, settingValue);
        editor.apply();
    }

    public static String readSharedSetting(Context ctx, String settingName, String defaultValue) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return sharedPref.getString(settingName, defaultValue);
    }

    /**
     * Adding few albums for testing
     */
    private void prepareAlbums() {
        albumList = new ArrayList<>();
        int[] covers = new int[]{
                R.drawable.gradient_1,
                R.drawable.gradient_2,
                R.drawable.gradient_3,
                R.drawable.gradient_4,
                R.drawable.gradient_5,
                R.drawable.gradient_6,
                R.drawable.gradient_7,
                R.drawable.gradient_8,
                R.drawable.gradient_9,
                R.drawable.gradient_10,
                R.drawable.gradient_11,
                R.drawable.gradient_12};
        List<String> musicGenres = Arrays.asList(getResources().getStringArray(R.array.music_genres));
        int coverGrandientColor = 0;
        for (int i = 0; i < 10; i++) {
            System.out.println("coverGradientColor = " + "I=" + i + "=" + coverGrandientColor);
            albumList.add(new VideosGridCategory(musicGenres.get(i), 13, covers[coverGrandientColor]));
            //Endless gradient color for card
            if (coverGrandientColor == covers.length - 1) {
                coverGrandientColor = 0;
            } else {
                coverGrandientColor++;
            }
        }
        if (genresAlbumsGridAdapter != null) {
            genresAlbumsGridAdapter.notifyDataSetChanged();
        }
    }


    private void getSearchListFromServer(String url, String videoTitle, RecyclerView recyclerView, LinearLayout contentLayout, TextView videosTitleTv,
                                         ArrayList<YoutubeDataModel> mListData, final HashMap<String, YoutubeDataModel> hmMainListData) {
        hmMainListData.clear();
        System.out.println("**Request Videos**" + url);
        //Retrieving response from the server
        JsonObjectRequest js = new JsonObjectRequest(Request.Method.GET, url, jsonObjUserDetail,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("**Response Videos** " + response);
                        HashMap<String, YoutubeDataModel> hmListData = parseVideoListFromResponse(response, hmMainListData);
                        System.out.println("****hmListData 11= " + hmListData.size());
                        System.out.println("****hmMainListData 22= " + hmMainListData.size());
                        if (videosIdArrayList != null && videosIdArrayList.size() != 0) {
                            getVideoStatisticsResponse(mListData, videosIdStatisticsQuery(appendWithCommaIds(videosIdArrayList)),
                                    videoTitle, recyclerView,
                                    contentLayout,
                                    videosTitleTv, hmListData);
                        }
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Server is not reachable!!! " + error, Toast.LENGTH_SHORT).show();
            }
        }) {

            //This is for Headers If You Needed
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "text/plain");
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(js, null);

    }


    public HashMap<String, YoutubeDataModel> parseVideoListFromResponse(JSONObject jsonObject, HashMap<String, YoutubeDataModel> hmMainListData) {

        HashMap<String, YoutubeDataModel> hmList = new HashMap<>();
        videosIdArrayList.clear();
        channelIdArrayList.clear();
        playListIdArrayList.clear();
        if (jsonObject.has("items")) {
            try {
                if (jsonObject.has("nextPageToken")) {
                    pageToken = jsonObject.getString("nextPageToken");
                }

                JSONArray jsonArray = jsonObject.getJSONArray("items");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    if (json.has("kind")) {
                        if (json.getString("kind").equals("youtube#searchResult")) {
                            String video_id = "";
                            String hashMapKey = "";
                            if (json.has("id")) {
                                YoutubeDataModel youtubeObject = new YoutubeDataModel();
                                JSONObject json1 = json.getJSONObject("id");
                                kind = json1.getString("kind");
                                JSONObject jsonSnippet = json.getJSONObject("snippet");

                                if (kind.equalsIgnoreCase(ConstURL.CHANNEL_TYPE)) {
                                    channelId = json1.getString("channelId");
                                    channelIdArrayList.add(channelId);
                                    youtubeObject.setVideo_id(video_id);
                                    hashMapKey = channelId;
                                    System.out.println("channelId = " + channelId);
                                    youtubeObject.setChannel_id(channelId);
                                }
                                if (kind.equalsIgnoreCase(ConstURL.VIDEOS_TYPE)) {
                                    video_id = json1.getString("videoId");
                                    videosIdArrayList.add(video_id);
                                    hashMapKey = video_id;
                                    channelId = jsonSnippet.getString("channelId");
                                    youtubeObject.setChannel_id(channelId);
                                    System.out.println("video_id = " + video_id);
                                }
                                if (kind.equalsIgnoreCase(ConstURL.PLAYLIST_TYPE)) {
                                    playListID = json1.getString("playlistId");
                                    playListIdArrayList.add(playListID);
                                    hashMapKey = playListID;
                                    channelId = jsonSnippet.getString("channelId");
                                    youtubeObject.setChannel_id(channelId);
                                    System.out.println("PlayListID = " + playListID);
                                    youtubeObject.setPlayList_id(playListID);
                                }

                                String title = jsonSnippet.getString("title");
                                String description = jsonSnippet.getString("description");
                                System.out.println("description = " + description);
                                String publishedAt = jsonSnippet.getString("publishedAt");
                                System.out.println("publishedAt = " + publishedAt);
                                String thumbnailHigh = jsonSnippet.getJSONObject("thumbnails").getJSONObject("high").getString("url");
                                String thumbnailMedium = jsonSnippet.getJSONObject("thumbnails").getJSONObject("medium").getString("url");
                                String thumbnailDefault = jsonSnippet.getJSONObject("thumbnails").getJSONObject("default").getString("url");
                                youtubeObject.setTitle(title);
                                youtubeObject.setDescription(description);
                                youtubeObject.setPublishedAt(publishedAt);
                                youtubeObject.setThumbnailHigh(thumbnailHigh);
                                youtubeObject.setThumbnailMedium(thumbnailMedium);
                                youtubeObject.setThumbnailDefault(thumbnailDefault);
                                youtubeObject.setKind(kind);
                                youtubeObject.setChannelTitle(jsonSnippet.getString("channelTitle"));
                                hmList.put(hashMapKey, youtubeObject);
                            }
                        }
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return hmList;
    }

    private void getVideoStatisticsResponse(ArrayList<YoutubeDataModel> mListData, String url, String videosTitle
            , RecyclerView recyclerView, LinearLayout contentLayout, TextView videosTitleTv,
                                            HashMap<String, YoutubeDataModel> hmMainListData) {
        System.out.println("Request Video Statistics**" + url);
        JsonObjectRequest js = new JsonObjectRequest(Request.Method.GET, url, jsonObjUserDetail,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("**Get Video Statistics Response**" + response);
                        parseTrendingStatisticsResponse(response, hmMainListData);
                        System.out.println("**hmMainListData1 = " + hmMainListData.size());
                        System.out.println("**mListData1 = " + mListData.size());
                        mListData.addAll(new ArrayList<YoutubeDataModel>(hmMainListData.values()));
                        System.out.println("**hmMainListData2 = " + hmMainListData.size());
                        System.out.println("**mListData2 = " + mListData.size());
                        prepareVideosGridRV(mListData, recyclerView,
                                contentLayout,
                                videosTitleTv, videosTitle);
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Server is not reachable!!! " + error, Toast.LENGTH_SHORT).show();
            }
        }) {

            //This is for Headers If You Needed
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "text/plain");
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(js, null);
    }

    private void parseTrendingStatisticsResponse(JSONObject jsonObject, HashMap<String, YoutubeDataModel> hmMainListData) {
        HashMap<String, YoutubeDataModel> hmList = new HashMap<String, YoutubeDataModel>();
        if (jsonObject.has("items")) {
            try {
                JSONArray jsonArray = jsonObject.getJSONArray("items");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    String video_id = "";
                    String hashMapKey = "";
//                    YoutubeDataModel youtubeObject = new YoutubeDataModel();
                    kind = json.getString("kind");
                    if (hmMainListData.containsKey(json.getString("id"))) {
                        if (kind.equalsIgnoreCase(ConstURL.CHANNEL_TYPE)) {
                            channelId = json.getString("id");
                            hashMapKey = channelId;
                            hmMainListData.get(hashMapKey).setChannel_id(channelId);
                            System.out.println("channelId = " + channelId);
                            if (json.has("statistics") && json.getJSONObject("statistics").has("videoCount"))
                                hmMainListData.get(hashMapKey).setVideoCount(json.getJSONObject("statistics").getString("videoCount"));
                            if (json.has("statistics") && json.getJSONObject("statistics").has("viewCount"))
                                hmMainListData.get(hashMapKey).setViewCount(json.getJSONObject("statistics").getString("viewCount"));
                            if (json.has("statistics") && json.getJSONObject("statistics").has("subscriberCount"))
                                hmMainListData.get(hashMapKey).setSubscriberCount(json.getJSONObject("statistics").getString("subscriberCount"));
                        }
                        if (kind.equalsIgnoreCase(ConstURL.VIDEOS_TYPE)) {
                            video_id = json.getString("id");
                            hashMapKey = video_id;
                            hmMainListData.get(hashMapKey).setVideo_id(video_id);
                            if (json.has("contentDetails") && json.getJSONObject("contentDetails").has("duration"))
                                hmMainListData.get(hashMapKey).setDuration(json.getJSONObject("contentDetails").getString("duration"));
                            if (json.has("contentDetails") && json.getJSONObject("contentDetails").has("subscriberCount"))
                                hmMainListData.get(hashMapKey).setSubscriberCount(json.getJSONObject("statistics").getString("subscriberCount"));
                            if (json.has("statistics") && json.getJSONObject("statistics").has("viewCount"))
                                hmMainListData.get(hashMapKey).setViewCount(json.getJSONObject("statistics").getString("viewCount"));
                            if (json.has("statistics") && json.getJSONObject("statistics").has("likeCount"))
                                hmMainListData.get(hashMapKey).setLikeCount(json.getJSONObject("statistics").getString("likeCount"));
                            if (json.has("statistics") && json.getJSONObject("statistics").has("dislikeCount"))
                                hmMainListData.get(hashMapKey).setDislikeCount(json.getJSONObject("statistics").getString("dislikeCount"));
                            if (json.has("statistics") && json.getJSONObject("statistics").has("favoriteCount"))
                                hmMainListData.get(hashMapKey).setFavoriteCount(json.getJSONObject("statistics").getString("favoriteCount"));
                            if (json.has("statistics") && json.getJSONObject("statistics").has("commentCount"))
                                hmMainListData.get(hashMapKey).setCommentCount(json.getJSONObject("statistics").getString("commentCount"));
                            System.out.println("video_id = " + video_id);
                        }
                        if (kind.equalsIgnoreCase(ConstURL.PLAYLIST_TYPE)) {
                            playListID = json.getString("id");
                            hashMapKey = playListID;
                            hmMainListData.get(hashMapKey).setPlayList_id(playListID);
                            if (json.has("contentDetails") && json.getJSONObject("contentDetails").has("itemCount"))
                                hmMainListData.get(hashMapKey).setVideoCount(json.getJSONObject("contentDetails").getString("itemCount"));
                            System.out.println("PlayListID = " + playListID);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
//            adapter.notifyDataSetChanged();
        }
    }

    private String videosIdStatisticsQuery(String ids) {
        return "https:///www.googleapis.com/youtube/v3/videos?part=contentDetails,statistics&id=" + ids + "&key=" + ConstURL.GOOGLE_YOUTUBE_API_KEY;
    }

    private String appendWithCommaIds(ArrayList<String> idArrayList) {
        StringBuilder result = new StringBuilder();
        for (String string : idArrayList) {
            result.append(string);
            result.append(",");
        }
        return result.length() > 0 ? result.substring(0, result.length() - 1) : "";
    }
}
