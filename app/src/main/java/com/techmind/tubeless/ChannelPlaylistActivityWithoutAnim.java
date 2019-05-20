package com.techmind.tubeless;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.squareup.picasso.Picasso;
import com.techmind.tubeless.Sqlite.PostsDatabaseHelper;
import com.techmind.tubeless.adapters.VideoPostAdapter;
import com.techmind.tubeless.config.AppController;
import com.techmind.tubeless.config.ConstURL;
import com.techmind.tubeless.interfaces.OnItemClickListener;
import com.techmind.tubeless.models.YoutubeDataModel;
import com.techmind.tubeless.util.ConnectionDetector;
import com.techmind.tubeless.util.Localization;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.techmind.tubeless.config.AppController.showToast;
import static com.techmind.tubeless.config.ConstURL.CHANNEL_TYPE;
import static com.techmind.tubeless.config.ConstURL.GOOGLE_YOUTUBE_API_KEY;
import static com.techmind.tubeless.util.AnimationUtils.animateView;


public class ChannelPlaylistActivityWithoutAnim extends AppCompatActivity {

    private YoutubeDataModel youtubeDataModel = null;

    private RecyclerView mList_videos = null;
    private VideoPostAdapter adapter;
    JSONObject jsonObjUserDetail = new JSONObject();
    private String CHANNEL_GET_URL;
    private String CHANNEL_Banner_GET_URL;
    private EndlessRecyclerViewScrollListener scrollListener;
    private String pageToken;
    private ImageButton img_bookmark;
    private boolean bookmarkedId = false;
    private ArrayList<YoutubeDataModel> mListData;
    private ImageView channel_banner_image;
    private String channelBannerImageUrl = "";
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar loadingProgressBar;
    private View errorPanelRoot;
    private Button errorButtonRetry;
    private TextView errorTextView;
    private TextView channel_subscriber_view;
    private CircleImageView imageViewProfile;
    private boolean requestStatistics;
    private TextView listErrorMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_channel);

        youtubeDataModel = getIntent().getParcelableExtra(YoutubeDataModel.class.toString());
        requestStatistics = getIntent().getExtras().getBoolean("requestStatistics");

        listErrorMsg = findViewById(R.id.listErrorMsg);
        mList_videos = (RecyclerView) findViewById(R.id.mList_videos);
        img_bookmark = findViewById(R.id.img_bookmark);
        imageViewProfile = findViewById(R.id.channel_avatar_view);
        channel_banner_image = findViewById(R.id.channel_banner_image);
        TextView channel_title_view = findViewById(R.id.channel_title_view);
        channel_subscriber_view = findViewById(R.id.channel_subscriber_view);
        loadingProgressBar = findViewById(R.id.loading_progress_bar);
        errorPanelRoot = findViewById(R.id.error_panel);
        errorButtonRetry = findViewById(R.id.error_button_retry);
        errorTextView = findViewById(R.id.error_message_view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mList_videos.setLayoutManager(linearLayoutManager);
        mList_videos.setHasFixedSize(true);


        CHANNEL_GET_URL = "https://www.googleapis.com/youtube/v3/search?part=snippet&chart=mostPopular&order=date&channelId=" +
                youtubeDataModel.getChannel_id() + "&maxResults=10&key=" + GOOGLE_YOUTUBE_API_KEY;
        CHANNEL_Banner_GET_URL = "https://www.googleapis.com/youtube/v3/channels?part=brandingSettings&id=" + youtubeDataModel.getChannel_id() + "&key=" + GOOGLE_YOUTUBE_API_KEY;
        getChannelAndChannelBannerInfo();
        channel_title_view.setText(youtubeDataModel.getChannelTitle());
        if (!requestStatistics) {
            if (!youtubeDataModel.getSubscriberCount().isEmpty())
                channel_subscriber_view.setText(Localization.localizeSubscribersCount(getApplicationContext(),
                        Long.parseLong(youtubeDataModel.getSubscriberCount())));
            if (!youtubeDataModel.getThumbnailMedium().isEmpty())
                Picasso.get()
                        .load(youtubeDataModel.getThumbnailMedium())
                        .into(imageViewProfile);
        } else {
            getStatisticsResponse(channelIdStatisticsQuery(youtubeDataModel.getChannel_id()), youtubeDataModel, true);
        }
        checkBookmarkTag();


        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(mListData!=null&&mListData.size()>0) {
                    mListData.clear();
                    adapter.notifyDataSetChanged();
                }
                getChannelAndChannelBannerInfo();
            }
        });
        errorButtonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mListData!=null&&mListData.size()>0) {
                    mListData.clear();
                    adapter.notifyDataSetChanged();
                }
                getChannelAndChannelBannerInfo();
            }
        });
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
                    if (!channelBannerImageUrl.isEmpty()) {
                        youtubeDataModel.setChannelBannerImageUrl(channelBannerImageUrl);

                        bookmarkedDetailsInDB();
                    } else {
                        bookmarkedDetailsInDB();
                    }

                } else {
                    if (PostsDatabaseHelper.getInstance(view.getContext()).deleteId(youtubeDataModel.getChannel_id())) {
                        showToast("Channel bookmarked is removed successfully");
                        checkBookmarkTag();
                    }
                }
            }

            private void bookmarkedDetailsInDB() {
                youtubeDataModel.setKind(CHANNEL_TYPE);
                if (PostsDatabaseHelper.getInstance(getApplicationContext()).addPost(youtubeDataModel, CHANNEL_TYPE)) {
                    showToast("Channel is Bookmarked successfully");
                    checkBookmarkTag();
                } else {
                    Toast.makeText(getApplicationContext(), "Unable to Bookmark", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //    private void getStatisticsResponse(String url, YoutubeDataModel item,Boolean passToNextActivity ) {
//        System.out.println("Request_Statistics_URL=****************** " + url);
//        JsonObjectRequest js = new JsonObjectRequest(Request.Method.GET, url, jsonObjUserDetail,
//                new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        System.out.println("Get Statistics Response Api************* = " + response);
//                        Intent intent;
//                        parseTrendingStatisticsResponse(response, item);
//                        if(passToNextActivity) {
//                            if (item.getKind().equalsIgnoreCase("youtube#video")) {
////                                parseTrendingStatisticsResponse(response, item);
//                                intent = new Intent(getApplicationContext(), VideoPlayerActivity.class);
//                                intent.putExtra(YoutubeDataModel.class.toString(), item);
//                                intent.putExtra("activity", "VideoPlayerActivity");
//                                startActivity(intent);
//                            }
//                        }
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
//                            overridePendingTransition(R.animator.right_in, R.animator.left_out);
//                        }
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Toast.makeText(ChannelPlaylistActivityWithoutAnim.this, "Server is not reachable!!! " + error, Toast.LENGTH_SHORT).show();
//            }
//        }) {
//
//            //This is for Headers If You Needed
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("Content-Type", "text/plain");
//                return params;
//            }
//        };
//        AppController.getInstance().addToRequestQueue(js, null);
//    }
    private void getChannelAndChannelBannerInfo() {
        ConnectionDetector connectionDetector = new ConnectionDetector(getApplicationContext());
        if (connectionDetector.isConnectingToInternet()) {
            errorPanelRoot.setVisibility(View.INVISIBLE);
            getChannelListFromServer(CHANNEL_GET_URL);
            getBannerChannelListFromServer(CHANNEL_Banner_GET_URL);
        } else {
            errorPanelRoot.setVisibility(View.VISIBLE);
            errorTextView.setText("No Network");
            if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }
    }


    private void checkBookmarkTag() {
        if (PostsDatabaseHelper.getInstance(getApplicationContext()).checkTypeIdExistsOrNot(youtubeDataModel.getChannel_id()) == -1) {
            img_bookmark.setImageResource(R.drawable.ic_bookmarks_outline);
            bookmarkedId = false;
        } else {
            img_bookmark.setImageResource(R.drawable.ic_bookmarks_color);
            bookmarkedId = true;
        }
    }

    private void nextPageToken(String pageToken) {
        CHANNEL_GET_URL = "https://www.googleapis.com/youtube/v3/search?channelId=" +
                youtubeDataModel.getChannel_id() + "&pageToken=" + pageToken + "&part=snippet&" +
                "maxResults=10&order=date&key=" + ConstURL.GOOGLE_YOUTUBE_API_KEY + "&chart=mostPopular&type=videos&order=date&part=contentDetails";
        getEndlessListFromServer(CHANNEL_GET_URL);

    }

    private String channelIdStatisticsQuery(String ids) {
        return "https://www.googleapis.com/youtube/v3/channels?part=snippet,statistics&id=" + ids + "&key=" + ConstURL.GOOGLE_YOUTUBE_API_KEY;
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
                    System.out.println("json" + i + " = " + json);
                    if (json.has("id")) {
                        JSONObject jsonID = json.getJSONObject("id");
                        String video_id = "";
                        if (jsonID.has("kind")) {
                            if (jsonID.getString("kind").equals("youtube#video")) {
                                YoutubeDataModel youtubeObject = new YoutubeDataModel();
                                JSONObject jsonSnippet = json.getJSONObject("snippet");
                                if (jsonID.has("kind")) {
                                    youtubeObject.setKind(jsonID.getString("kind"));
                                }
                                if (jsonID.has("videoId")) {
                                    video_id = jsonID.getString("videoId");
                                    youtubeObject.setChannel_id(jsonSnippet.getString("channelId"));
                                }
                                String title = jsonSnippet.getString("title");
                                String description = jsonSnippet.getString("description");
                                String publishedAt = jsonSnippet.getString("publishedAt");
                                String thumbnailHigh = jsonSnippet.getJSONObject("thumbnails").getJSONObject("high").getString("url");
                                String thumbnailMedium = jsonSnippet.getJSONObject("thumbnails").getJSONObject("medium").getString("url");
                                String thumbnailDefault = jsonSnippet.getJSONObject("thumbnails").getJSONObject("default").getString("url");
                                youtubeObject.setChannelTitle(jsonSnippet.getString("channelTitle"));
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
        if(mListData!=null&&mListData.size()>0)
            mListData.clear();
        if (loadingProgressBar != null) animateView(loadingProgressBar, true, 400);
        System.out.println("getChannelListFromServer*************= " + url);
        //Retrieving response from the server
        JsonObjectRequest js = new JsonObjectRequest(Request.Method.GET, url, jsonObjUserDetail,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(" getChannelListFromServerResponse = " + response);

                        mListData = parseVideoListFromResponse(response);

                        if(mListData.size()>0) {
                            listErrorMsg.setVisibility(View.GONE);
                            initList(mListData);
                        }else{
                            listErrorMsg.setVisibility(View.VISIBLE);
                        }
                        errorPanelRoot.setVisibility(View.INVISIBLE);

                        if (loadingProgressBar != null) animateView(loadingProgressBar, false, 0);
                        if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (loadingProgressBar != null) animateView(loadingProgressBar, false, 0);
                animateView(errorPanelRoot, false, 150);
                if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
                Toast.makeText(ChannelPlaylistActivityWithoutAnim.this, "Server is not reachable!!! " + error, Toast.LENGTH_SHORT).show();
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
                                if (thumbnailArray.getJSONObject("brandingSettings").getJSONObject("image").has("bannerMobileExtraHdImageUrl")) {
                                    channelBannerImageUrl = thumbnailArray.getJSONObject("brandingSettings").getJSONObject("image").getString("bannerMobileExtraHdImageUrl");

                                    Picasso.get()
                                            .load(channelBannerImageUrl)
                                            .into(channel_banner_image);
                                    System.out.println("thumbnail = " + channelBannerImageUrl);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ChannelPlaylistActivityWithoutAnim.this, "Server is not reachable!!! " + error, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(ChannelPlaylistActivityWithoutAnim.this, "Server is not reachable!!! " + error, Toast.LENGTH_SHORT).show();
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
        adapter = new VideoPostAdapter(this, mListData, mList_videos, new OnItemClickListener() {
            @Override
            public void onItemClick(YoutubeDataModel item) {
                YoutubeDataModel youtubeDataModel = item;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
                    overridePendingTransition(R.animator.right_in, R.animator.left_out);
                }
                if (item != null && !item.getVideo_id().isEmpty()) {
                    getStatisticsResponse(videosIdStatisticsQuery(item.getVideo_id()), item, false);
                }
            }
        });
        mList_videos.setAdapter(adapter);
//        mList_videos.smoothScrollToPosition(previousListPosition);
    }

    @Override
    public void onBackPressed() {
        finish();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            this.overridePendingTransition(R.animator.left_to_right, R.animator.right_to_left);
        }
        super.onBackPressed();
    }

    private String videosIdStatisticsQuery(String ids) {
        return "https:///www.googleapis.com/youtube/v3/videos?part=contentDetails,statistics&id=" + ids + "&key=" + ConstURL.GOOGLE_YOUTUBE_API_KEY;
    }

    private void getStatisticsResponse(String url, YoutubeDataModel item, Boolean requestChannelDetails) {
        System.out.println("url = " + url);
        JsonObjectRequest js = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("getStatisticsResponse Api = " + response);
                        parseTrendingStatisticsResponse(response, item);
                        if (requestChannelDetails) {
                           /* if(!youtubeDataModel.getSubscriberCount().isEmpty()) {
                                channel_subscriber_view.setText(Localization.localizeSubscribersCount(getApplicationContext(),
                                        Long.parseLong(youtubeDataModel.getSubscriberCount())));
                            }*/
                        } else {
                            Intent intent = new Intent(ChannelPlaylistActivityWithoutAnim.this, VideoPlayerActivity.class);
                            intent.putExtra(YoutubeDataModel.class.toString(), item);
                            startActivity(intent);
                        }
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ChannelPlaylistActivityWithoutAnim.this, "Server is not reachable!!! " + error, Toast.LENGTH_SHORT).show();
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

    private void parseTrendingStatisticsResponse(JSONObject jsonObject, YoutubeDataModel item) {
        HashMap<String, YoutubeDataModel> hmList = new HashMap<String, YoutubeDataModel>();
        if (jsonObject.has("items")) {
            try {
                JSONArray jsonArray = jsonObject.getJSONArray("items");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    String video_id = "";
                    String hashMapKey = "";
//                    YoutubeDataModel youtubeObject = new YoutubeDataModel();
                    String kind = json.getString("kind");
                    if (item.getChannel_id().equals(json.getString("id"))) {
                        if (kind.equalsIgnoreCase(ConstURL.CHANNEL_TYPE)) {
                            String channelId = json.getString("id");
                            item.setChannel_id(channelId);
                            System.out.println("channelId = " + channelId);
                            if (json.has("statistics") && json.getJSONObject("statistics").has("videoCount"))
                                item.setVideoCount(json.getJSONObject("statistics").getString("videoCount"));
                            if (json.has("statistics") && json.getJSONObject("statistics").has("viewCount"))
                                item.setViewCount(json.getJSONObject("statistics").getString("viewCount"));
                            if (json.has("statistics") && json.getJSONObject("statistics").has("subscriberCount"))
                                item.setSubscriberCount(json.getJSONObject("statistics").getString("subscriberCount"));
                            if (json.has("snippet") && json.getJSONObject("snippet").has("thumbnails"))
                                item.setUploaderAvatarUrl(json.getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("default").getString("url"));
                            if(requestStatistics)
                                item.setThumbnailMedium(item.getUploaderAvatarUrl());
                                item.setTitle(youtubeDataModel.getChannelTitle());
                            if (!item.getSubscriberCount().isEmpty())
                                channel_subscriber_view.setText(Localization.localizeSubscribersCount(getApplicationContext(),
                                        Long.parseLong(item.getSubscriberCount())));
                            if (!item.getThumbnailDefault().isEmpty())
                                Picasso.get()
                                        .load(item.getThumbnailMedium())
                                        .into(imageViewProfile);
                        }
                    }
                    if (item.getVideo_id().equals(json.getString("id"))) {
                        if (kind.equalsIgnoreCase(ConstURL.VIDEOS_TYPE)) {
                            video_id = json.getString("id");
                            item.setVideo_id(video_id);
                            if (json.has("contentDetails") && json.getJSONObject("contentDetails").has("duration"))
                                item.setDuration(json.getJSONObject("contentDetails").getString("duration"));
                            if (json.has("contentDetails") && json.getJSONObject("contentDetails").has("subscriberCount"))
                                item.setSubscriberCount(json.getJSONObject("statistics").getString("subscriberCount"));
                            if (json.has("statistics") && json.getJSONObject("statistics").has("viewCount"))
                                item.setViewCount(json.getJSONObject("statistics").getString("viewCount"));
                            if (json.has("statistics") && json.getJSONObject("statistics").has("likeCount"))
                                item.setLikeCount(json.getJSONObject("statistics").getString("likeCount"));
                            if (json.has("statistics") && json.getJSONObject("statistics").has("dislikeCount"))
                                item.setDislikeCount(json.getJSONObject("statistics").getString("dislikeCount"));
                            if (json.has("statistics") && json.getJSONObject("statistics").has("favoriteCount"))
                                item.setFavoriteCount(json.getJSONObject("statistics").getString("favoriteCount"));
                            if (json.has("statistics") && json.getJSONObject("statistics").has("commentCount"))
                                item.setCommentCount(json.getJSONObject("statistics").getString("commentCount"));
                            System.out.println("video_id = " + video_id);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            youtubeDataModel=item;
//            adapter.notifyDataSetChanged();
        }
    }
}
