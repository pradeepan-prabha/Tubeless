package com.techmind.tubeless;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.techmind.tubeless.Sqlite.PostsDatabaseHelper;
import com.techmind.tubeless.adapters.VideoPostAdapter;
import com.techmind.tubeless.config.AppController;
import com.techmind.tubeless.config.ConstURL;
import com.techmind.tubeless.interfaces.OnItemClickListener;
import com.techmind.tubeless.models.YoutubeDataModel;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.techmind.tubeless.config.ConstURL.CHANNEL_TYPE;
import static com.techmind.tubeless.config.ConstURL.GOOGLE_YOUTUBE_API_KEY;


public class ChannelPlaylistActivity extends AppCompatActivity {

    private CollapsingToolbarLayout collapsingToolbar;
    private AppBarLayout appBarLayout;
    private Menu collapsedMenu;
    private boolean appBarExpanded = true;

    private YoutubeDataModel youtubeDataModel = null;

    private RecyclerView mList_videos = null;
    private VideoPostAdapter adapter;
    JSONObject jsonObjUserDetail = new JSONObject();
    private String CHANNEL_GET_URL;
    private String CHANNEL_Banner_GET_URL;
    private ImageView imageViewBanner;
    private EndlessRecyclerViewScrollListener scrollListener;
    private String pageToken;
    private ArrayList<YoutubeDataModel> mListData;
    private ImageButton img_bookmark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_animate_toolbar);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.anim_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        appBarLayout = (AppBarLayout) findViewById(R.id.appbar);

        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
//        collapsingToolbar.setTitle(getString(R.string.android_desserts));

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.header);

        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @SuppressWarnings("ResourceType")
            @Override
            public void onGenerated(Palette palette) {
                int vibrantColor = palette.getVibrantColor(R.color.colorPrimary);
                collapsingToolbar.setContentScrimColor(vibrantColor);
                collapsingToolbar.setStatusBarScrimColor(R.color.black_trans80);
            }
        });

        //  Use when your list size is constant for better performance

        youtubeDataModel = getIntent().getParcelableExtra(YoutubeDataModel.class.toString());
        collapsingToolbar.setTitle(youtubeDataModel.getTitle());

        mList_videos = (RecyclerView) findViewById(R.id.scrollableview);
        img_bookmark = findViewById(R.id.img_bookmark);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mList_videos.setLayoutManager(linearLayoutManager );
        mList_videos.setHasFixedSize(true);
        CircleImageView imageViewProfile=findViewById(R.id.imageViewProfile);
        imageViewBanner=findViewById(R.id.header);
        Picasso.get()
                .load(youtubeDataModel.getThumbnail())
                .into(imageViewProfile);

        CHANNEL_GET_URL = "https://www.googleapis.com/youtube/v3/search?part=snippet&chart=mostPopular&order=date&channelId=" +
                youtubeDataModel.getChannel_id() + "&maxResults=10&key=" + GOOGLE_YOUTUBE_API_KEY + "&part=contentDetails";
        CHANNEL_Banner_GET_URL = "https://www.googleapis.com/youtube/v3/channels?part=brandingSettings&id=" + youtubeDataModel.getChannel_id() + "&key=" + GOOGLE_YOUTUBE_API_KEY;
        getChannelListFromServer(CHANNEL_GET_URL);
        getBannerChannelListFromServer(CHANNEL_Banner_GET_URL);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                Log.d(ChannelPlaylistActivity.class.getSimpleName(), "onOffsetChanged: verticalOffset: " + verticalOffset);

                //  Vertical offset == 0    indicates appBar is fully expanded.
                if (Math.abs(verticalOffset) > 200) {
                    appBarExpanded = false;
                    invalidateOptionsMenu();
                } else {
                    appBarExpanded = true;
                    invalidateOptionsMenu();
                }
            }
        });
        // Adds the scroll listener to RecyclerView
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager ) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
//                loadNextDataFromApi(page);
                System.out.println("totalItemsCount ++++++++++++= " + totalItemsCount);
                System.out.println("page =++++++++++++++++++++++++++++++++ " + page);
                if(pageToken!=null&&!pageToken.isEmpty())
                {
                    System.out.println("pageToken = " + pageToken);
                nextPageToken(pageToken);
                }else{
                    System.out.println("pageToken is size = " + page);
                }
            }
        };
        mList_videos.addOnScrollListener(scrollListener);
        img_bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Add sample post to the database
//                System.out.println("youtubeDataModel.getVideo_id()%%%%% = " + youtubeDataModel.getChannel_id());

                if(PostsDatabaseHelper.getInstance(view.getContext()).addPost(youtubeDataModel,CHANNEL_TYPE)){
                    Toast.makeText(getApplicationContext(),"Channel is Bookmarked successfully",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getApplicationContext(),"Channel is already Bookmarked",Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
    private void nextPageToken(String pageToken) {
        CHANNEL_GET_URL = "https://www.googleapis.com/youtube/v3/search?channelId=" +
        youtubeDataModel.getChannel_id()+"&pageToken=" + pageToken + "&part=snippet&" +
                "maxResults=10&order=date&key=" + ConstURL.GOOGLE_YOUTUBE_API_KEY + "&chart=mostPopular&type=videos&order=date&part=contentDetails";
        getEndlessListFromServer(CHANNEL_GET_URL);

    }
    public ArrayList<YoutubeDataModel> parseVideoListFromResponse(JSONObject jsonObject) {
        ArrayList<YoutubeDataModel> mList = new ArrayList<>();
        if (jsonObject.has("items")) {
            try {
                if (jsonObject.has("nextPageToken")) {
                    pageToken = jsonObject.getString("nextPageToken");
                }
                System.out.println("pageToken = " + pageToken);
                JSONArray jsonArray = jsonObject.getJSONArray("items");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    System.out.println("json"+i+" = " + json);
                    if (json.has("id")) {
                        JSONObject jsonID = json.getJSONObject("id");
                        String video_id = "";
                        if (jsonID.has("videoId")) {
                            video_id = jsonID.getString("videoId");
                        }
                        if (jsonID.has("kind")) {
                            if (jsonID.getString("kind").equals("youtube#video")) {
                                YoutubeDataModel youtubeObject = new YoutubeDataModel();
                                JSONObject jsonSnippet = json.getJSONObject("snippet");
                                String title = jsonSnippet.getString("title");
                                String description = jsonSnippet.getString("description");
                                String publishedAt = jsonSnippet.getString("publishedAt");
                                String thumbnail = jsonSnippet.getJSONObject("thumbnails").getJSONObject("high").getString("url");

                                youtubeObject.setTitle(title);
                                youtubeObject.setDescription(description);
                                youtubeObject.setPublishedAt(publishedAt);
                                youtubeObject.setThumbnail(thumbnail);
                                youtubeObject.setVideo_id(video_id);
                                mList.add(youtubeObject);

                            }
                        }
//                        previousListPosition=mList.size();
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return mList;

    }

    private void getChannelListFromServer(String url) {
        System.out.println("getChannelListFromServer*************= " + url);
        //Retrieving response from the server
        JsonObjectRequest js = new JsonObjectRequest(Request.Method.GET, url, jsonObjUserDetail,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(" getChannelListFromServerResponse = " + response);

                            mListData = parseVideoListFromResponse(response);
                            initList(mListData);
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ChannelPlaylistActivity.this, "Server is not reachable!!! " + error, Toast.LENGTH_SHORT).show();
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

    private void getBannerChannelListFromServer(String url) {
       mListData = new ArrayList<>();

        System.out.println("getBannerChannelListFromServer*************= " + url);
        //Retrieving response from the server
        JsonObjectRequest js = new JsonObjectRequest(Request.Method.GET, url, jsonObjUserDetail,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                        System.out.println("getBannerChannelListFromServerResponse = " + response);
                        JSONObject thumbnailArray = (JSONObject) response.getJSONArray("items").get(0);
                        if (thumbnailArray.has("brandingSettings")) {
                            String thumbnail = thumbnailArray.getJSONObject("brandingSettings").getJSONObject("image").getString("bannerMobileExtraHdImageUrl");

//                            String thumbnail = response.getJSONObject("items");
                            Picasso.get()
                                    .load(thumbnail)
                                    .into(imageViewBanner);
                            System.out.println("thumbnail = " + thumbnail);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(ChannelPlaylistActivity.this, "Server is not reachable!!! " + error, Toast.LENGTH_SHORT).show();
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

    private ArrayList<YoutubeDataModel> getEndlessListFromServer(String url) {
        System.out.println("CHANNLE_GET_URL*************= " + url);
        //Retrieving response from the server
        JsonObjectRequest js = new JsonObjectRequest(Request.Method.GET, url, jsonObjUserDetail,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("response Channel Api = " + response);
                        ArrayList<YoutubeDataModel> nextTokenArrayList= new ArrayList<>();
                        nextTokenArrayList = parseVideoListFromResponse(response);
//                        initList(mListData);
                        System.out.println("nextTokenArrayList = " + nextTokenArrayList+"       Size="+nextTokenArrayList.size());
                        mListData.addAll(nextTokenArrayList);
                        System.out.println("mListData = " +mListData.size()+"       Size="+ mListData);
                        adapter.notifyItemInserted(mListData.size());
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ChannelPlaylistActivity.this, "Server is not reachable!!! " + error, Toast.LENGTH_SHORT).show();
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
        return mListData;
    }

    private void initList(ArrayList<YoutubeDataModel> mListData) {
        adapter = new VideoPostAdapter(this, mListData,mList_videos, new OnItemClickListener() {
            @Override
            public void onItemClick(YoutubeDataModel item) {
                YoutubeDataModel youtubeDataModel = item;
                Intent intent = new Intent(ChannelPlaylistActivity.this, VideoPlayerActivity.class);
                intent.putExtra(YoutubeDataModel.class.toString(), youtubeDataModel);
                startActivity(intent);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
                    overridePendingTransition(R.animator.right_in, R.animator.left_out);
                }
            }
        });
        mList_videos.setAdapter(adapter);
//        mList_videos.smoothScrollToPosition(previousListPosition);
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (collapsedMenu != null
                && (!appBarExpanded || collapsedMenu.size() != 1)) {
            //collapsed
            collapsedMenu.add("Add")
                    .setIcon(R.drawable.ic_action_add)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        } else {
            //expanded
        }
        return super.onPrepareOptionsMenu(collapsedMenu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        collapsedMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_settings:
                return true;
        }
        if (item.getTitle() == "Add") {
            Toast.makeText(this, "clicked add", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }
}
