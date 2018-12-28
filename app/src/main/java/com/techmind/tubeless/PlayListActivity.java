package com.techmind.tubeless;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.techmind.tubeless.Sqlite.PostsDatabaseHelper;
import com.techmind.tubeless.adapters.VideoPostAdapter;
import com.techmind.tubeless.config.AppController;
import com.techmind.tubeless.config.ConstURL;
import com.techmind.tubeless.interfaces.OnItemClickListener;
import com.techmind.tubeless.models.YoutubeDataModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.techmind.tubeless.config.AppController.showToast;
import static com.techmind.tubeless.config.ConstURL.CHANNEL_TYPE;
import static com.techmind.tubeless.config.ConstURL.PLAYLIST_TYPE;

public class PlayListActivity extends AppCompatActivity {
    private String CHANNEL_PLAYLIST_GET_URL;
    private RecyclerView mList_videos = null;
    private VideoPostAdapter adapter = null;
    private ArrayList<YoutubeDataModel> mListData = new ArrayList<>();
    private EndlessRecyclerViewScrollListener scrollListener;
    private String pageToken;
    private ImageButton img_bookmark;
    private boolean bookmarkedId = false;
    private YoutubeDataModel youtubeDataModel = null;
    private String playList_GET_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistic_playlist_control);

        mList_videos = (RecyclerView)findViewById(R.id.mList_videos);
        img_bookmark = findViewById(R.id.img_bookmark);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mList_videos.setLayoutManager(linearLayoutManager);
        mList_videos.setHasFixedSize(true);
        youtubeDataModel = getIntent().getParcelableExtra(YoutubeDataModel.class.toString());
        CHANNEL_PLAYLIST_GET_URL = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&playlistId=" +
                youtubeDataModel.getPlayList_id()  + "&maxResults=10&key=" + ConstURL.GOOGLE_YOUTUBE_API_KEY + "";
  rquestPlayList();
        checkBookmarkTag();

        // Adds the scroll listener to RecyclerView
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
//                loadNextDataFromApi(page);
                System.out.println("totalItemsCount ++++++++++++= " + totalItemsCount);
                System.out.println("page =++++++++++++++++++++++++++++++++ " + page);
                if (pageToken != null && !pageToken.isEmpty()) {
                    System.out.println("pageToken = " + pageToken);
                    nextPageToken(pageToken);
                } else {
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
                if (!bookmarkedId) {
                    if (PostsDatabaseHelper.getInstance(view.getContext()).addPost(youtubeDataModel, PLAYLIST_TYPE)) {
                        showToast("Channel is Bookmarked successfully");
                        checkBookmarkTag();
                    } else {
                        Toast.makeText(getApplicationContext(), "Unable to Bookmark", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (PostsDatabaseHelper.getInstance(view.getContext()).deleteId(youtubeDataModel.getPlayList_id())) {
                        showToast("Channel bookmarked is removed successfully");
                        checkBookmarkTag();
                    }
                }
            }
        });

    }
    private void nextPageToken(String pageToken) {

        playList_GET_URL = "https://www.googleapis.com/youtube/v3/playlistItems?playlistId=" +
                youtubeDataModel.getPlayList_id() + "&pageToken=" + pageToken + "&part=snippet&" +
                "maxResults=10&order=date&chart=mostPopular&type=videos&key="+ ConstURL.GOOGLE_YOUTUBE_API_KEY ;
        getEndlessListFromServer(playList_GET_URL);

    }
    private ArrayList<YoutubeDataModel> getEndlessListFromServer(String url) {
        System.out.println("PlayList URL*************= " + url);
        //Retrieving response from the server
        JsonObjectRequest js = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("response PlayList Api = " + response);
                        ArrayList<YoutubeDataModel> nextTokenArrayList = new ArrayList<>();
                        nextTokenArrayList = parseVideoListFromResponse(response);
//                        initList(mListData);
                        System.out.println("nextTokenArrayList = " + nextTokenArrayList + "       Size=" + nextTokenArrayList.size());
                        mListData.addAll(nextTokenArrayList);
                        System.out.println("mListData = " + mListData.size() + "       Size=" + mListData);
                        adapter.notifyItemInserted(mListData.size());
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(PlayListActivity.this, "Server is not reachable!!! " + error, Toast.LENGTH_SHORT).show();
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

    private void checkBookmarkTag() {
        if (PostsDatabaseHelper.getInstance(getApplicationContext()).checkTypeIdExistsOrNot(youtubeDataModel.getPlayList_id()) == -1) {
            img_bookmark.setImageResource(R.drawable.ic_bookmarks_outline);
            bookmarkedId = false;
        } else {
            img_bookmark.setImageResource(R.drawable.ic_bookmarks_color);
            bookmarkedId = true;
        }
    }

    private void initList(ArrayList<YoutubeDataModel> mListData) {
        adapter = new VideoPostAdapter(PlayListActivity.this, mListData,mList_videos, new OnItemClickListener() {
            @Override
            public void onItemClick(YoutubeDataModel item) {
                YoutubeDataModel youtubeDataModel = item;
                Intent intent = new Intent(PlayListActivity.this, VideoPlayerActivity.class);
                intent.putExtra(YoutubeDataModel.class.toString(), youtubeDataModel);
                startActivity(intent);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
                    overridePendingTransition(R.animator.right_in, R.animator.left_out);
                }
            }
        });
        mList_videos.setAdapter(adapter);

    }
    public ArrayList<YoutubeDataModel> parseVideoListFromResponse(JSONObject jsonObject) {
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
                        if (json.getString("kind").equals("youtube#playlistItem")) {
                            YoutubeDataModel youtubeObject = new YoutubeDataModel();
                            JSONObject jsonSnippet = json.getJSONObject("snippet");
                            String video_id = "";
                            if (jsonSnippet.has("resourceId")) {
                                JSONObject jsonResource = jsonSnippet.getJSONObject("resourceId");
                                video_id = jsonResource.getString("videoId");
                            }
                            String description = jsonSnippet.getString("description");
                            String publishedAt = jsonSnippet.getString("publishedAt");
                            String thumbnailHigh = jsonSnippet.getJSONObject("thumbnails").getJSONObject("high").getString("url");
                            String thumbnailMedium = jsonSnippet.getJSONObject("thumbnails").getJSONObject("medium").getString("url");
                            String thumbnailDefault = jsonSnippet.getJSONObject("thumbnails").getJSONObject("default").getString("url");
                            youtubeObject.setChannelTitle(jsonSnippet.getString("channelTitle"));
                            String title = jsonSnippet.getString("title");
                            youtubeObject.setTitle(title);
                            youtubeObject.setVideo_id(video_id);
                            youtubeObject.setDescription(description);
                            youtubeObject.setPublishedAt(publishedAt);
                            youtubeObject.setThumbnailHigh(thumbnailHigh);
                            youtubeObject.setThumbnailMedium(thumbnailMedium);
                            youtubeObject.setThumbnailDefault(thumbnailDefault);
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
    private void rquestPlayList() {

        JSONObject jsonObjUserDetail = new JSONObject();

        //Retrieving response from the server
        JsonObjectRequest js = new JsonObjectRequest(Request.Method.GET, CHANNEL_PLAYLIST_GET_URL, jsonObjUserDetail,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("CHANNLE_GET_URL response = " + response);
                        mListData = parseVideoListFromResponse(response);
                        initList(mListData);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(PlayListActivity.this, "Server is not reachable!!! " + error, Toast.LENGTH_SHORT).show();
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
                    this.overridePendingTransition(R.animator.left_to_right, R.animator.right_to_left);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        finish();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            this.overridePendingTransition(R.animator.left_to_right, R.animator.right_to_left);
        }
        super.onBackPressed();
    }
}
