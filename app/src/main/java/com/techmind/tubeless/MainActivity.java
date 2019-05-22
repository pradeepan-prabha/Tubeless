package com.techmind.tubeless;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.techmind.tubeless.Sqlite.PostsDatabaseHelper;
import com.techmind.tubeless.adapters.FavouriteGridViewAdapter;
import com.techmind.tubeless.adapters.GenresAlbumsGridAdapter;
import com.techmind.tubeless.adapters.MultiViewAdapter;
import com.techmind.tubeless.interfaces.OnItemClickListener;
import com.techmind.tubeless.models.YoutubeDataModel;
import com.techmind.tubeless.pojo.VideosGridCategory;
import com.techmind.tubeless.util.ConnectionDetector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    Toolbar toolbar;
    private ProgressBar loadingProgressBar;
    private View errorPanelRoot;
    private Button errorButtonRetry;
    private TextView errorTextView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private List<YoutubeDataModel> youtubeDataModelsList;
    private List<VideosGridCategory> albumList;
    private RecyclerView genresAlbumsHorizontalRecyclerView;
    private GenresAlbumsGridAdapter genresAlbumsGridAdapter;
    private TextView moreGenresTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_drawer);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.nav_drawer);
//        FloatingActionButton fab = findViewById(R.id.fab);
        mUserLearnedDrawer = Boolean.valueOf(readSharedSetting(this, PREF_USER_LEARNED_DRAWER, "false"));
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        loadingProgressBar = findViewById(R.id.loading_progress_bar);
        errorPanelRoot = findViewById(R.id.error_panel);
        errorButtonRetry = findViewById(R.id.error_button_retry);
        errorTextView = findViewById(R.id.error_message_view);
        moreGenresTextView = findViewById(R.id.moreGenresTextView);

        setUpToolbar();
        setUpNavDrawer();
//        mList_videos = (RecyclerView) findViewById(R.id.mList_videos);
        empty_view = (TextView) findViewById(R.id.empty_view);
        getBookMarkedDate();

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }
//        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
//        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                getBookMarkedDate();
//            }
//        });
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
        prepareGenresAlbums();
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
        genresAlbumsHorizontalRecyclerView = (RecyclerView) findViewById(R.id.genresAlbumsHorizontalRV);
        albumList = new ArrayList<>();
        //Horizontal view=0 change view
        genresAlbumsGridAdapter = new GenresAlbumsGridAdapter(this, albumList, 0);
        prepareAlbums();
        genresAlbumsHorizontalRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        genresAlbumsHorizontalRecyclerView.setItemAnimator(new DefaultItemAnimator());
        genresAlbumsHorizontalRecyclerView.setNestedScrollingEnabled(false);
        genresAlbumsHorizontalRecyclerView.setAdapter(genresAlbumsGridAdapter);

    }

    private void prepareFavouriteVideosHorizontalRV(ArrayList<YoutubeDataModel> mListData) {
        if (mListData != null && mListData.size() > 0) {
            findViewById(R.id.favouriteVideosLayout).setVisibility(View.VISIBLE);
            ArrayList<YoutubeDataModel> videosBookmarkedList = new ArrayList<YoutubeDataModel>();
            for (YoutubeDataModel youtubeDataModel : mListData) {
                if ("youtube#video".equals(youtubeDataModel.getKind())) {
                    videosBookmarkedList.add(youtubeDataModel);
                }
            }
            RecyclerView favouriteVideosHorizontalRV = (RecyclerView) findViewById(R.id.favouriteVideosHorizontalRV);
            albumList = new ArrayList<>();
            //Horizontal view=0 change view
            FavouriteGridViewAdapter bookmarkVideosGridAdapter = new FavouriteGridViewAdapter(this, videosBookmarkedList, videosBookmarkedList, new OnItemClickListener() {
                @Override
                public void onItemClick(YoutubeDataModel item) {
                    onItemClickNavigation(item);
                }
            });
            RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
            favouriteVideosHorizontalRV.setLayoutManager(mLayoutManager);
            favouriteVideosHorizontalRV.addItemDecoration(new GenresAlbumsGridList.GridSpacingItemDecoration(2, dpToPx(5), true));
            favouriteVideosHorizontalRV.setItemAnimator(new DefaultItemAnimator());
            favouriteVideosHorizontalRV.setNestedScrollingEnabled(false);
            favouriteVideosHorizontalRV.setAdapter(bookmarkVideosGridAdapter);
        } else {
            findViewById(R.id.favouriteChannelLayout).setVisibility(View.GONE);
        }
    }

    private void prepareFavouriteChannelHorizontalRV(ArrayList<YoutubeDataModel> mListData) {
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
            favouriteChannelHorizontalRV.addItemDecoration(new GenresAlbumsGridList.GridSpacingItemDecoration(2, dpToPx(5), true));
            favouriteChannelHorizontalRV.setItemAnimator(new DefaultItemAnimator());
            favouriteChannelHorizontalRV.setNestedScrollingEnabled(false);
            favouriteChannelHorizontalRV.setAdapter(bookmarkVideosGridAdapter);
        } else {
            findViewById(R.id.favouriteChannelLayout).setVisibility(View.GONE);
        }
    }

    private void prepareFavouritePlaylistHorizontalRV(ArrayList<YoutubeDataModel> mListData) {
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
            favouritePlaylistHorizontalRV.addItemDecoration(new GenresAlbumsGridList.GridSpacingItemDecoration(2, dpToPx(5), true));
            favouritePlaylistHorizontalRV.setItemAnimator(new DefaultItemAnimator());
            favouritePlaylistHorizontalRV.setNestedScrollingEnabled(false);
            favouritePlaylistHorizontalRV.setAdapter(bookmarkVideosGridAdapter);
        } else {
            findViewById(R.id.favouriteChannelLayout).setVisibility(View.GONE);
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
            // Get all posts from database
            youtubeDataModelsList = PostsDatabaseHelper.getInstance(getApplicationContext()).getAllPosts();
            if (youtubeDataModelsList.size() > 0) {
                empty_view.setVisibility(View.GONE);
            } else {
                empty_view.setVisibility(View.VISIBLE);
            }
//        for (YoutubeDataModel youtubeDataModelsIndex : youtubeDataModelsList) {
//        }
            initList((ArrayList<YoutubeDataModel>) youtubeDataModelsList);

        } else {
            if (youtubeDataModelsList != null && youtubeDataModelsList.size() > 0) {
                youtubeDataModelsList.clear();
                adapter.notifyDataSetChanged();
            }
            errorPanelRoot.setVisibility(View.VISIBLE);
            errorTextView.setText("No Network");
        }
        if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
        if (loadingProgressBar != null) animateView(loadingProgressBar, false, 0);

        prepareFavouriteVideosHorizontalRV((ArrayList<YoutubeDataModel>) youtubeDataModelsList);
        prepareFavouriteChannelHorizontalRV((ArrayList<YoutubeDataModel>) youtubeDataModelsList);
        prepareFavouritePlaylistHorizontalRV((ArrayList<YoutubeDataModel>) youtubeDataModelsList);
    }

    private void initList(ArrayList<YoutubeDataModel> mListData) {
    /*    mList_videos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MultiViewAdapter(this, mListData, mList_videos, new OnItemClickListener() {
            private Intent intent;

            @Override
            public void onItemClick(YoutubeDataModel item) {
                onItemClickNavigation(item);
            }
        });
        mList_videos.setAdapter(adapter);
//        mList_videos.smoothScrollToPosition(previousListPosition);*/
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
        genresAlbumsGridAdapter.notifyDataSetChanged();
    }
}
    /*

    private static final String ACTION_MEDIA_CONTROL = "media_control";
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Rational;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.techmind.tubeless.widget.MovieView;

import java.util.ArrayList;

import static com.techmind.tubeless.config.ConstURL.GOOGLE_YOUTUBE_API_KEY;

//* Demonstrates usage of Picture-in-Picture mode on phones and tablets.

    @RequiresApi(api = Build.VERSION_CODES.O)
    public class MainActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

//* Intent action for media controls from Picture-in-Picture mode.

    }
    public static final boolean DEBUG = !BuildConfig.BUILD_TYPE.equals("release");

//* Intent extra for media controls from Picture-in-Picture mode.

    private static final String EXTRA_CONTROL_TYPE = "control_type";

//* The request code for play action PendingIntent.

    private static final int REQUEST_PLAY = 1;

//* The request code for pause action PendingIntent.

    private static final int REQUEST_PAUSE = 2;

//* The request code for info action PendingIntent.

    private static final int REQUEST_INFO = 3;

//* The intent extra value for play action.

    private static final int CONTROL_TYPE_PLAY = 1;

//* The intent extra value for pause action.

    private static final int CONTROL_TYPE_PAUSE = 2;

//* The arguments to be used for Picture-in-Picture mode.

    private final PictureInPictureParams.Builder mPictureInPictureParamsBuilder =
            new PictureInPictureParams.Builder();

//* This shows the video.

//    private MovieView mMovieView;

//* The bottom half of the screen; hidden on landscape

    private ScrollView mScrollView;

//* A {@link BroadcastReceiver} to receive action item events from Picture-in-Picture mode.

    private BroadcastReceiver mReceiver;

    private String mPlay;
    private String mPause;

    private final View.OnClickListener mOnClickListener =
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (view.getId()) {
                        case R.id.pip:
                            minimize();
                            break;
                    }
                }
            };

//* Callbacks from the {@link MovieView} showing the video playback.

    private MovieView.MovieListener mMovieListener =
            new MovieView.MovieListener() {

                @Override
                public void onMovieStarted() {
                    // We are playing the video now. In PiP mode, we want to show an action item to
                    // pause
                    // the video.
                    updatePictureInPictureActions(
                            R.drawable.ic_pause_24dp, mPause, CONTROL_TYPE_PAUSE, REQUEST_PAUSE);
                }

                @Override
                public void onMovieStopped() {
                    // The video stopped or reached its end. In PiP mode, we want to show an action
                    // item to play the video.
                    updatePictureInPictureActions(
                            R.drawable.ic_play_arrow_24dp, mPlay, CONTROL_TYPE_PLAY, REQUEST_PLAY);
                }

                @Override
                public void onMovieMinimized() {
                    // The MovieView wants us to minimize it. We enter Picture-in-Picture mode now.
                    minimize();
                }
            };
    private YouTubePlayerView mYoutubePlayerView;
    private YouTubePlayer mYoutubePlayer;

//*
//     * Update the state of pause/resume action item in Picture-in-Picture mode.
//     *
//     * @param iconId The icon to be used.
//     * @param title The title text.
//     * @param controlType The type of the action. either {@link #CONTROL_TYPE_PLAY} or {@link
//     *     #CONTROL_TYPE_PAUSE}.
//     * @param requestCode The request code for the {@link PendingIntent}.


    void updatePictureInPictureActions(
            @DrawableRes int iconId, String title, int controlType, int requestCode) {
        final ArrayList<RemoteAction> actions = new ArrayList<>();

        // This is the PendingIntent that is invoked when a user clicks on the action item.
        // You need to use distinct request codes for play and pause, or the PendingIntent won't
        // be properly updated.
        final PendingIntent intent =
                PendingIntent.getBroadcast(
                        MainActivity.this,
                        requestCode,
                        new Intent(ACTION_MEDIA_CONTROL).putExtra(EXTRA_CONTROL_TYPE, controlType),
                        0);
        final Icon icon = Icon.createWithResource(MainActivity.this, iconId);
        actions.add(new RemoteAction(icon, title, title, intent));

        // Another action item. This is a fixed action.
        actions.add(
                new RemoteAction(
                        Icon.createWithResource(MainActivity.this, R.drawable.ic_info_24dp),
                        getString(R.string.info),
                        getString(R.string.info_description),
                        PendingIntent.getActivity(
                                MainActivity.this,
                                REQUEST_INFO,
                                new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(getString(R.string.info_uri))),
                                0)));

        mPictureInPictureParamsBuilder.setActions(actions);

        // This is how you can update action items (or aspect ratio) for Picture-in-Picture mode.
        // Note this call can happen even when the app is not in PiP mode. In that case, the
        // arguments will be used for at the next call of #enterPictureInPictureMode.
        setPictureInPictureParams(mPictureInPictureParamsBuilder.build());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Prepare string resources for Picture-in-Picture actions.
        mPlay = getString(R.string.play);
        mPause = getString(R.string.pause);

        // View references
        mYoutubePlayerView = (YouTubePlayerView) findViewById(R.id.youtube_player);
        mYoutubePlayerView.initialize(GOOGLE_YOUTUBE_API_KEY, this);
        mScrollView = findViewById(R.id.scroll);

        Button switchExampleButton = findViewById(R.id.switch_example);
        switchExampleButton.setText(getString(R.string.switch_media_session));
        switchExampleButton.setOnClickListener(new SwitchActivityOnClick());

        // Set up the video; it automatically starts.
//        mMovieView.setMovieListener(mMovieListener);
        findViewById(R.id.pip).setOnClickListener(mOnClickListener);
    }

    @Override
    protected void onStop() {
        // On entering Picture-in-Picture mode, onPause is called, but not onStop.
        // For this reason, this is the place where we should pause the video playback.
//        mMovieView.pause();
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!isInPictureInPictureMode()) {
            // Show the video controls so the video can be easily resumed.
//            mMovieView.showControls();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        adjustFullScreen(newConfig);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            adjustFullScreen(getResources().getConfiguration());
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onPictureInPictureModeChanged(
            boolean isInPictureInPictureMode, Configuration configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, configuration);
        if (isInPictureInPictureMode) {
            // Starts receiving events from action items in PiP mode.
            mReceiver =
                    new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            if (intent == null
                                    || !ACTION_MEDIA_CONTROL.equals(intent.getAction())) {
                                return;
                            }

                            // This is where we are called back from Picture-in-Picture action
                            // items.
                            final int controlType = intent.getIntExtra(EXTRA_CONTROL_TYPE, 0);
                            switch (controlType) {
                                case CONTROL_TYPE_PLAY:
                                    mYoutubePlayer.play();
                                    break;
                                case CONTROL_TYPE_PAUSE:
                                    mYoutubePlayer.pause();
                                    break;
                            }
                        }
                    };
            registerReceiver(mReceiver, new IntentFilter(ACTION_MEDIA_CONTROL));
        } else {
            // We are out of PiP mode. We can stop receiving events from it.
            unregisterReceiver(mReceiver);
            mReceiver = null;
            // Show the video controls if the video is not playing
//            if (mMovieView != null && !mMovieView.isPlaying()) {
//                mMovieView.showControls();
//            }
        }
    }

//* Enters Picture-in-Picture mode.

    void minimize() {
        if (mYoutubePlayerView== null) {
            return;
        }
        // Hide the controls in picture-in-picture mode.
//        mMovieView.hideControls();
        // Calculate the aspect ratio of the PiP screen.
        Rational aspectRatio = new Rational(mYoutubePlayerView.getWidth(), mYoutubePlayerView.getHeight());
        mPictureInPictureParamsBuilder.setAspectRatio(aspectRatio).build();
        enterPictureInPictureMode(mPictureInPictureParamsBuilder.build());
    }

//*
//     * Adjusts immersive full-screen flags depending on the screen orientation.
//     *
//     * @param config The current {@link Configuration}.


    private void adjustFullScreen(Configuration config) {
        final View decorView = getWindow().getDecorView();
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            mScrollView.setVisibility(View.GONE);
//            mMovieView.setAdjustViewBounds(false);
        } else {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            mScrollView.setVisibility(View.VISIBLE);
//            mMovieView.setAdjustViewBounds(true);
        }
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        youTubePlayer.setPlayerStateChangeListener(playerStateChangeListener);
        youTubePlayer.setPlaybackEventListener(playbackEventListener);
        if (!wasRestored) {
            youTubePlayer.loadVideo("c2UNv38V6y4");
        }
        mYoutubePlayer = youTubePlayer;
    }

    private YouTubePlayer.PlaybackEventListener playbackEventListener = new YouTubePlayer.PlaybackEventListener() {
        @Override
        public void onPlaying() {

        }

        @Override
        public void onPaused() {

        }

        @Override
        public void onStopped() {

        }

        @Override
        public void onBuffering(boolean b) {

        }

        @Override
        public void onSeekTo(int i) {

        }
    };

    private YouTubePlayer.PlayerStateChangeListener playerStateChangeListener = new YouTubePlayer.PlayerStateChangeListener() {
        @Override
        public void onLoading() {

        }

        @Override
        public void onLoaded(String s) {
            mYoutubePlayer.play();
        }

        @Override
        public void onAdStarted() {

        }

        @Override
        public void onVideoStarted() {

        }

        @Override
        public void onVideoEnded() {

        }

        @Override
        public void onError(YouTubePlayer.ErrorReason errorReason) {

        }
    };

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        System.out.println("YouTubePlayer.Provider *****= " + provider);
        System.out.println("youTubeInitializationResult = " + youTubeInitializationResult);
    }


//* Launches {@link MediaSessionPlaybackActivity} and closes this activity.

    private class SwitchActivityOnClick implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            startActivity(new Intent(view.getContext(), MediaSessionPlaybackActivity.class));
            finish();
        }
    }
}
*/
