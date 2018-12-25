package com.techmind.tubeless;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.techmind.tubeless.adapters.MultiViewAdapter;
import com.techmind.tubeless.config.AppController;
import com.techmind.tubeless.config.ConstURL;
import com.techmind.tubeless.interfaces.OnItemClickListener;
import com.techmind.tubeless.models.YoutubeDataModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private Toolbar mToolbar;
    FrameLayout mContentFrame;
    private View layoutView;
    private ProgressDialog mProgressDialog;
    private String requestType;
    //Handler to run a thread which could fill the list after downloading data
    //from the internet and inflating the images, title and description
    private Handler handler;

    //results list of type VideoItem to store the results so that each item
    //int the array list has id, title, description and thumbnail url
    private List<YoutubeDataModel> searchResults;
    private RecyclerView mList_videos = null;
    private MultiViewAdapter adapter = null;
    private String pageToken;
    private ArrayList<YoutubeDataModel> mListData = new ArrayList<>();
    public static String CHANNEL_GET_URL;
    JSONObject jsonObjUserDetail = new JSONObject();
    private String search_type = "";
    private String search_url;
    private String kind;
    private String channelId;
    private EndlessRecyclerViewScrollListener scrollListener;
    private CheckBox searchAnyCB;
    private String searchQuery;
    private String queryStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.search_activity);
        setUpToolbar();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        layoutView = LayoutInflater.from(this).inflate(R.layout.fragment_live, mContentFrame, false);
        mList_videos = (RecyclerView) layoutView.findViewById(R.id.mList_videos);
        searchAnyCB = layoutView.findViewById(R.id.searchAnyCB);
        if (searchAnyCB.isChecked()) {
            searchQuery = "any";
        } else {
            searchQuery = "channel";
        }
        searchAnyCB.setOnCheckedChangeListener(this);

        mList_videos.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mList_videos.setLayoutManager(linearLayoutManager);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Searching...");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        SearchView searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setQuery("", true);
        searchView.setFocusable(true);
        searchView.setIconified(false);
        searchView.requestFocusFromTouch();
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                 queryStr=query;
                //setting progress message so that users can understand what is happening
                mProgressDialog.setMessage("Finding videos for " + query.trim());
                search_url = "https://www.googleapis.com/youtube/v3/search?part=snippet&q=" + query + "&type=" + searchQuery + "&maxResults=10&key="
                        + ConstURL.GOOGLE_YOUTUBE_API_KEY + "&part=contentDetails";
                if (!query.isEmpty() && search_type != null) {
                    mProgressDialog.show();
                    mListData.clear();
                    getSearchListFromServer(search_url);
                } else {
                    Toast.makeText(SearchActivity.this, "Select request Type", Toast.LENGTH_SHORT).show();
                }

                //getting instance of the keyboard or any other input from which user types
                InputMethodManager imm = (InputMethodManager) SearchActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                //hiding the keyboard once search button is clicked
                imm.hideSoftInputFromWindow(SearchActivity.this.getCurrentFocus().getWindowToken(),
                        InputMethodManager.RESULT_UNCHANGED_SHOWN);
                System.out.println("title = " + query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
//        initList(mListData);
        mContentFrame = (FrameLayout) findViewById(R.id.nav_contentframe);
        mContentFrame.addView(layoutView);
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
//                loadNextDataFromApi(page);
                System.out.println("totalItemsCount ++++++++++++= " + totalItemsCount);
                System.out.println("page =++++++++++++++++++++++++++++++++ " + page);
//                ArrayList<YoutubeDataModel>nextPageTokenArrayList=nextPageToken(pageToken);
                System.out.println("pageToken = " + pageToken);
                if (pageToken != null && !pageToken.isEmpty()) {
                    nextPageToken(pageToken);
                } else {
                    System.out.println("pageToken is size = " + page);
                }
            }
        };
        mList_videos.addOnScrollListener(scrollListener);

    }

    private ArrayList<YoutubeDataModel> nextPageToken(String pageToken) {
        CHANNEL_GET_URL = "https://www.googleapis.com/youtube/v3/search?&part=snippet&q=" + queryStr +"&pageToken=" + pageToken + "&type=" + searchQuery +
                "&maxResults=10&key=" + ConstURL.GOOGLE_YOUTUBE_API_KEY + "&part=contentDetails";
        return getEndlessListFromServer(CHANNEL_GET_URL);

    }

    private void getSearchListFromServer(String url) {
        System.out.println("CHANNLE_GET_URL*************= " + url);
        //Retrieving response from the server
        JsonObjectRequest js = new JsonObjectRequest(Request.Method.GET, url, jsonObjUserDetail,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                        System.out.println("response Channel Api = " + response);
                        mListData = parseTrendingVideoListFromResponse(response);
                        initList(mListData);

                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (mProgressDialog.isShowing())
                    Toast.makeText(SearchActivity.this, "Server is not reachable!!! " + error, Toast.LENGTH_SHORT).show();
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
                        if (mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                        System.out.println("response Channel Api = " + response);
                        ArrayList<YoutubeDataModel> nextTokenArrayList = new ArrayList<>();
                        nextTokenArrayList = parseTrendingVideoListFromResponse(response);
//                        initList(mListData);
                        System.out.println("Size=" + nextTokenArrayList.size() + "nextTokenArrayList =" + nextTokenArrayList);
                        mListData.addAll(nextTokenArrayList);
                        System.out.println("Size=" + mListData.size() + "mListData = " + mListData);
                        adapter.notifyItemInserted(mListData.size());
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                Toast.makeText(SearchActivity.this, "Server is not reachable!!! " + error, Toast.LENGTH_SHORT).show();
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

        adapter = new MultiViewAdapter(SearchActivity.this, mListData, mList_videos, new OnItemClickListener() {
            private Intent intent;

            @Override
            public void onItemClick(YoutubeDataModel item) {
                YoutubeDataModel youtubeDataModel = item;
                if (youtubeDataModel.getKind().equalsIgnoreCase("youtube#channel")) {
//                    &&requestTypeCallbackStr.equalsIgnoreCase(getString(R.string.channelKey))
                    intent = new Intent(SearchActivity.this, ChannelPlaylistActivity.class);
                    intent.putExtra(YoutubeDataModel.class.toString(), youtubeDataModel);
                    intent.putExtra("activity", "SearchActivity");
                    startActivity(intent);
//                    requestTypeCallbackStr.equalsIgnoreCase(getString(R.string.videoKey))
                } else if (youtubeDataModel.getKind().equalsIgnoreCase("youtube#video")) {
                    intent = new Intent(SearchActivity.this, VideoPlayerActivity.class);
                    intent.putExtra(YoutubeDataModel.class.toString(), youtubeDataModel);
                    intent.putExtra("activity", "SearchActivity");
                    startActivity(intent);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
                    overridePendingTransition(R.animator.right_in, R.animator.left_out);
                }
            }
        });

        mList_videos.setAdapter(adapter);
//        mList_videos.smoothScrollToPosition(previousListPosition);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.search_type, menu);
        return true;
    }

    public ArrayList<YoutubeDataModel> parseTrendingVideoListFromResponse(JSONObject jsonObject) {

        ArrayList<YoutubeDataModel> mList = new ArrayList<>();

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
                            if (json.has("id")) {
                                JSONObject json1 = json.getJSONObject("id");
                                kind = json1.getString("kind");
                                if (kind.equalsIgnoreCase("youtube#channel")) {

                                    channelId = json1.getString("channelId");
                                    System.out.println("channelId = " + channelId);
                                }
                                if (kind.equalsIgnoreCase("youtube#video")) {
                                    video_id = json1.getString("videoId");
                                    System.out.println("video_id = " + video_id);
                                }
                            }
                       /* if (jsonID.has("videoId")) {
                            video_id = jsonID.getString("videoId");
                        }*/


                            YoutubeDataModel youtubeObject = new YoutubeDataModel();
                            JSONObject jsonSnippet = json.getJSONObject("snippet");
                            String title = jsonSnippet.getString("title");
                            if (json.has("videoId")) {
                                video_id = json.getString("videoId");
                            }
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
                            youtubeObject.setChannel_id(channelId);
                            youtubeObject.setKind(kind);
                            youtubeObject.setVideo_id(video_id);
                            mList.add(youtubeObject);

                        }
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return mList;
    }

    private void setUpToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
    }

    @Override
    public void onBackPressed() {
        backPress();
        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        backPress();
        return super.onSupportNavigateUp();
    }

    private void backPress() {
        Intent intentActivity = new Intent(this, MainActivity.class);
        intentActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intentActivity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            overridePendingTransition(R.animator.left_to_right, R.animator.right_to_left);
        }
        finish();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checkSearch) {
        switch (compoundButton.getId()) {
            case R.id.searchAnyCB:
                if (checkSearch) {
                    searchQuery = "any";
                } else {
                    searchQuery = "channel";
                }
                break;
        }
    }
}
