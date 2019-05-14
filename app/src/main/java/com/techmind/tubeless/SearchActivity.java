package com.techmind.tubeless;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.techmind.tubeless.adapters.MultiViewAdapter;
import com.techmind.tubeless.config.AppController;
import com.techmind.tubeless.config.ConstURL;
import com.techmind.tubeless.interfaces.OnItemClickListener;
import com.techmind.tubeless.models.YoutubeDataModel;
import com.techmind.tubeless.util.ConnectionDetector;
import android.widget.CursorAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
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
    private String channelId = "";
    private EndlessRecyclerViewScrollListener scrollListener;
    private CheckBox searchAnyCB;
    private String searchQueryType;
    private String queryStr;
    private String playListID = "";
    ArrayList<String> videosIdArrayList = new ArrayList<String>();
    ArrayList<String> channelIdArrayList = new ArrayList<String>();
    ArrayList<String> playListIdArrayList = new ArrayList<String>();
    //    ArrayList<YoutubeDataModel> mainArrayList = new ArrayList<YoutubeDataModel>();
    private HashMap<String, YoutubeDataModel> hmMainListData = new HashMap<>();
    private HashMap<String, YoutubeDataModel> hmTempListData = new HashMap<>();
    private ProgressBar loadingProgressBar;
    private View errorPanelRoot;
    private Button errorButtonRetry;
    private TextView errorTextView;
    private TextView listErrorMsg;

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
        listErrorMsg = findViewById(R.id.empty_view);
        if (searchAnyCB.isChecked()) {
            searchQueryType = "any";
        } else {
            searchQueryType = "channel";
        }
        searchAnyCB.setOnCheckedChangeListener(this);

        mList_videos.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mList_videos.setLayoutManager(linearLayoutManager);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Searching...");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loadingProgressBar = findViewById(R.id.loading_progress_bar);
        errorPanelRoot = findViewById(R.id.error_panel);
        errorButtonRetry = findViewById(R.id.error_button_retry);
        errorTextView = findViewById(R.id.error_message_view);

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
        errorButtonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshQuery();
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                queryStr = query;
                //setting progress message so that users can understand what is happening
                mProgressDialog.setMessage("Finding videos for " + queryStr.trim());
                search_url = "https://www.googleapis.com/youtube/v3/search?part=snippet&q=" + queryStr + "&type=" + searchQueryType + "&maxResults=10&key="
                        + ConstURL.GOOGLE_YOUTUBE_API_KEY;
                refreshQuery();
               /* if (!query.isEmpty() && search_type != null) {
                    if(mProgressDialog!=null&&!mProgressDialog.isShowing()) {
                        mProgressDialog.show();
                    }
                    mListData.clear();
                    getSearchListFromServer(search_url);
                } else {
                    Toast.makeText(SearchActivity.this, "Select request Type", Toast.LENGTH_SHORT).show();
                }*/

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
                if(newText.length() > 0) {
                    newText = newText.replace(" ", "+");
                    String url = "https://suggestqueries.google.com/complete/search?client=youtube&ds=yt&client=firefox&q="
                            + newText;
                    System.out.println("**Suggest queries request** " + url);

                    JsonArrayRequest req = new JsonArrayRequest(url,
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    try {
                                        System.out.println("**Suggest queries response** " + response);
                                        JSONArray jsonArraySuggestion = (JSONArray) response.get(1);
                                        String[] suggestions = new String[10];
                                        for (int i = 0; i < 10; i++) {
                                            if (!jsonArraySuggestion.isNull(i)) {
                                                suggestions[i] = jsonArraySuggestion.get(i).toString();
                                            }
                                        }
                                        Log.d("Suggestions", Arrays.toString(suggestions));
                                        //Cursor Adaptor
                                        String[] columnNames = {"_id", "suggestion"};
                                        MatrixCursor cursor = new MatrixCursor(columnNames);
                                        String[] temp = new String[2];
                                        int id = 0;
                                        for (String item : suggestions) {
                                            if (item != null) {
                                                temp[0] = Integer.toString(id++);
                                                temp[1] = item;
                                                cursor.addRow(temp);
                                            }
                                        }
                                        CursorAdapter cursorAdapter = new CursorAdapter(getApplicationContext(), cursor, false) {
                                            @Override
                                            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                                                return LayoutInflater.from(context).inflate(R.layout.search_suggestion_list_item, parent, false);
                                            }

                                            @Override
                                            public void bindView(View view, Context context, Cursor cursor) {
                                                final TextView suggest = (TextView) view.findViewById(R.id.suggest);
                                                ImageView putInSearchBox = (ImageView) view.findViewById(R.id.put_in_search_box);
                                                String body = cursor.getString(cursor.getColumnIndexOrThrow("suggestion"));
                                                suggest.setTextColor(getResources().getColor(R.color.white));
                                                suggest.setText(body);
                                                suggest.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        searchView.setQuery(suggest.getText(), true);
                                                        searchView.clearFocus();
                                                    }
                                                });
                                                putInSearchBox.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        searchView.setQuery(suggest.getText(), false);
                                                    }
                                                });
                                            }
                                        };
                                        searchView.setSuggestionsAdapter(cursorAdapter);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            VolleyLog.d("Tag", "Error: " + error.getMessage());
                            System.out.println("**Error getMessage**" + error.getMessage());
                            Toast.makeText(getApplicationContext(),
                                    error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    // Adding request to request queue
                    AppController.getInstance().addToRequestQueue(req);

                }
                return true;
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

    private void refreshQuery() {
        ConnectionDetector connectionDetector = new ConnectionDetector(getApplicationContext());
        if (connectionDetector.isConnectingToInternet()) {
            errorPanelRoot.setVisibility(View.INVISIBLE);

            mProgressDialog.setMessage("Finding videos for " + queryStr.trim());
            search_url = "https://www.googleapis.com/youtube/v3/search?part=snippet&q=" + queryStr + "&type=" + searchQueryType + "&maxResults=10&key="
                    + ConstURL.GOOGLE_YOUTUBE_API_KEY;
            if (!queryStr.isEmpty() && search_type != null) {
                if (mProgressDialog != null && !mProgressDialog.isShowing()) {
                    mProgressDialog.show();
                }
                mListData.clear();
                getSearchListFromServer(search_url);
            } else {
                Toast.makeText(SearchActivity.this, "Select request Type", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (mListData.size() > 0) {
                mListData.clear();
                adapter.notifyDataSetChanged();
            }
            errorPanelRoot.setVisibility(View.VISIBLE);
            errorTextView.setText("No Network");
        }
    }

    private void nextPageToken(String pageToken) {
        System.out.println("pageToken =************************ " + pageToken);
        CHANNEL_GET_URL = "https://www.googleapis.com/youtube/v3/search?&part=snippet&q=" + queryStr + "&pageToken=" + pageToken + "&type=" + searchQueryType +
                "&maxResults=10&key=" + ConstURL.GOOGLE_YOUTUBE_API_KEY;
        getEndlessListFromServer(CHANNEL_GET_URL);
        pageToken = null;
    }

    private void initList(ArrayList<YoutubeDataModel> mListData) {
        System.out.println("mListData = " + mListData);
        adapter = new MultiViewAdapter(SearchActivity.this, mListData, mList_videos, new OnItemClickListener() {
            private Intent intent;

            @Override
            public void onItemClick(YoutubeDataModel item) {
                YoutubeDataModel youtubeDataModel = item;
                if (youtubeDataModel.getKind().equalsIgnoreCase(ConstURL.CHANNEL_TYPE)) {
//                    &&requestTypeCallbackStr.equalsIgnoreCase(getString(R.string.channelKey))
                    intent = new Intent(SearchActivity.this, ChannelPlaylistActivityWithoutAnim.class);
                    intent.putExtra(YoutubeDataModel.class.toString(), youtubeDataModel);
                    startActivity(intent);
//                    requestTypeCallbackStr.equalsIgnoreCase(getString(R.string.videoKey))
                } else if (youtubeDataModel.getKind().equalsIgnoreCase(ConstURL.VIDEOS_TYPE)) {
                    intent = new Intent(SearchActivity.this, VideoPlayerActivity.class);
                    intent.putExtra(YoutubeDataModel.class.toString(), youtubeDataModel);
                    startActivity(intent);
                } else if (youtubeDataModel.getKind().equalsIgnoreCase(ConstURL.PLAYLIST_TYPE)) {
                    intent = new Intent(SearchActivity.this, PlayListActivity.class);
                    intent.putExtra(YoutubeDataModel.class.toString(), youtubeDataModel);
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

    private void getSearchListFromServer(String url) {
        hmMainListData.clear();
        System.out.println("Search request Channel or Any*************= " + url);
        //Retrieving response from the server
        JsonObjectRequest js = new JsonObjectRequest(Request.Method.GET, url, jsonObjUserDetail,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        System.out.println("response Channel or Any Api = " + response);
                        hmMainListData = parseVideoListFromResponse(response);
                        if (hmMainListData.size() > 0) {
                            listErrorMsg.setVisibility(View.GONE);
                        } else {
                            listErrorMsg.setVisibility(View.VISIBLE);
                        }
                            if (videosIdArrayList != null && videosIdArrayList.size() != 0) {
                                getStatisticsResponse(videosIdStatisticsQuery(appendWithCommaIds(videosIdArrayList)), true);
                            }
                            if (channelIdArrayList != null && channelIdArrayList.size() != 0) {
                                getStatisticsResponse(channelIdStatisticsQuery(appendWithCommaIds(channelIdArrayList)), true);
                            }
                            if (playListIdArrayList != null && playListIdArrayList.size() != 0) {
                                getStatisticsResponse(playListIdStatisticsQuery(appendWithCommaIds(playListIdArrayList)), true);
                            }

//                        hmMainListData.putAll(hmVideosStatisticsListData);
//                        hmMainListData.putAll(hmChannelsStatisticsListData);
//                        hmMainListData.putAll(hmPlayListStatisticsListData);

                        if (mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
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

    private void getEndlessListFromServer(String url) {
        hmTempListData.clear();
        System.out.println("Request EndlessList*************= " + url);
        //Retrieving response from the server
        JsonObjectRequest js = new JsonObjectRequest(Request.Method.GET, url, jsonObjUserDetail,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                        System.out.println("response EndlessList Api *****************= " + response);
                        ArrayList<YoutubeDataModel> nextTokenArrayList = new ArrayList<>();
                        hmTempListData = parseVideoListFromResponse(response);
                        hmMainListData.putAll(hmTempListData);
                        if (videosIdArrayList != null && videosIdArrayList.size() != 0) {
                            getStatisticsResponse(videosIdStatisticsQuery(appendWithCommaIds(videosIdArrayList)), false);
                        }
                        if (channelIdArrayList != null && channelIdArrayList.size() != 0) {
                            getStatisticsResponse(channelIdStatisticsQuery(appendWithCommaIds(channelIdArrayList)), false);
                        }
                        if (playListIdArrayList != null && playListIdArrayList.size() != 0) {
                            getStatisticsResponse(playListIdStatisticsQuery(appendWithCommaIds(playListIdArrayList)), false);
                        }
//                        initList(mListData);
//                        System.out.println("Size=" + nextTokenArrayList.size() + "nextTokenArrayList =" + nextTokenArrayList);
//                        mListData.addAll(new ArrayList<YoutubeDataModel>(hmTempListData.values()));
//                        System.out.println("Size=" + mListData.size() + "mListData = " + mListData);
//                        initList(mListData);
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
    }

    public HashMap<String, YoutubeDataModel> parseVideoListFromResponse(JSONObject jsonObject) {

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

    private void getStatisticsResponse(String url, Boolean firstInitList) {
        System.out.println("Request_Statistics_URL=****************** " + url);
        JsonObjectRequest js = new JsonObjectRequest(Request.Method.GET, url, jsonObjUserDetail,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                        System.out.println("get Statistics Response Api = " + response);
                        parseTrendingStatisticsResponse(response);
                        if (firstInitList) {
                            mListData.addAll(new ArrayList<YoutubeDataModel>(hmMainListData.values()));
                            initList(mListData);

                        } else {
                            mListData.addAll(new ArrayList<YoutubeDataModel>(hmTempListData.values()));
                            adapter.notifyItemInserted(mListData.size());
                        }


//                        initList(mListData);
//                        System.out.println("Size=" + nextTokenArrayList.size() + "nextTokenArrayList =" + nextTokenArrayList);
//                        mListData.addAll(nextTokenArrayList);
//                        System.out.println("Size=" + mListData.size() + "mListData = " + mListData);
//                        adapter.notifyItemInserted(mListData.size());
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
    }

    private void parseTrendingStatisticsResponse(JSONObject jsonObject) {
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

    private String channelIdStatisticsQuery(String ids) {
        return "https://www.googleapis.com/youtube/v3/channels?part=statistics&id=" + ids + "&key=" + ConstURL.GOOGLE_YOUTUBE_API_KEY;
    }

    private String videosIdStatisticsQuery(String ids) {
        return "https:///www.googleapis.com/youtube/v3/videos?part=contentDetails,statistics&id=" + ids + "&key=" + ConstURL.GOOGLE_YOUTUBE_API_KEY;
    }

    private String playListIdStatisticsQuery(String ids) {
        return "https://www.googleapis.com/youtube/v3/playlists?part=contentDetails&id=" + ids + "&key=" + ConstURL.GOOGLE_YOUTUBE_API_KEY;
    }

    private String appendWithCommaIds(ArrayList<String> idArrayList) {
        StringBuilder result = new StringBuilder();
        for (String string : idArrayList) {
            result.append(string);
            result.append(",");
        }
        return result.length() > 0 ? result.substring(0, result.length() - 1) : "";
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
                    searchQueryType = "any";
                } else {
                    searchQueryType = "channel";
                }
                break;
        }
    }
}
