package com.techmind.tubeless;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.techmind.tubeless.Sqlite.PostsDatabaseHelper;
import com.techmind.tubeless.adapters.VideoPostAdapter;
import com.techmind.tubeless.config.AppController;
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

import static com.techmind.tubeless.config.ConstURL.CHANNEL_TYPE;
import static com.techmind.tubeless.config.ConstURL.GOOGLE_YOUTUBE_API_KEY;
import static com.techmind.tubeless.config.ConstURL.VIDEOS_TYPE;

public class ChannelActivity extends AppCompatActivity {
    private YoutubeDataModel youtubeDataModel = null;
    ArrayList<YoutubeDataModel> mList = new ArrayList<>();
    private RecyclerView mList_videos = null;
    private ArrayList<YoutubeDataModel> mListData = new ArrayList<>();
    private VideoPostAdapter adapter;
    JSONObject jsonObjUserDetail = new JSONObject();
    private String CHANNEL_GET_URL;
    private String CHANNEL_Banner_GET_URL;
    private ImageView imageViewBanner;
    private ImageButton img_bookmark;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_activity);
        youtubeDataModel = getIntent().getParcelableExtra(YoutubeDataModel.class.toString());
        mList_videos = (RecyclerView) findViewById(R.id.mList_videos);

        ImageView imageViewProfile=findViewById(R.id.imageViewProfile);
        imageViewBanner=findViewById(R.id.imageViewBanner);
        Picasso.get()
                .load(youtubeDataModel.getThumbnail())
                .into(imageViewProfile);

        CHANNEL_GET_URL = "https://www.googleapis.com/youtube/v3/search?part=snippet&order=date&channelId=" +
                youtubeDataModel.getChannel_id() + "&maxResults=10&key=" + GOOGLE_YOUTUBE_API_KEY + "&part=contentDetails";
        CHANNEL_Banner_GET_URL = "https://www.googleapis.com/youtube/v3/channels?part=brandingSettings&id=" + youtubeDataModel.getChannel_id() + "&key=" + GOOGLE_YOUTUBE_API_KEY;
        getChannelListFromServer(CHANNEL_Banner_GET_URL);
        getBannerChannelListFromServer(CHANNEL_GET_URL);


    }

    public ArrayList<YoutubeDataModel> parseVideoListFromResponse(JSONObject jsonObject) {
        if (jsonObject.has("items")) {
            try {
//                pageToken=jsonObject.getString("nextPageToken");
                JSONArray jsonArray = jsonObject.getJSONArray("items");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
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
        System.out.println("CHANNLE_GET_URL*************= " + url);
        //Retrieving response from the server
        JsonObjectRequest js = new JsonObjectRequest(Request.Method.GET, url, jsonObjUserDetail,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("response Channel Api = " + response);
                        try {
                            JSONObject thumbnailArray = (JSONObject) response.getJSONArray("items").get(0);
                            String thumbnail=thumbnailArray.getJSONObject("brandingSettings").getJSONObject("image").getString("bannerMobileExtraHdImageUrl");

//                            String thumbnail = response.getJSONObject("items");
                            Picasso.get()
                                    .load(thumbnail)
                                    .into(imageViewBanner);
                            System.out.println("thumbnail = " + thumbnail);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(ChannelActivity.this, "Server is not reachable!!! " + error, Toast.LENGTH_SHORT).show();
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
        System.out.println("CHANNLE_GET_URL*************= " + url);
        //Retrieving response from the server
        JsonObjectRequest js = new JsonObjectRequest(Request.Method.GET, url, jsonObjUserDetail,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("response Channel Api = " + response);
                        mListData = parseVideoListFromResponse(response);
                        initList(mListData);

                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ChannelActivity.this, "Server is not reachable!!! " + error, Toast.LENGTH_SHORT).show();
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

    private void initList(ArrayList<YoutubeDataModel> mListData) {
        mList_videos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VideoPostAdapter(this, mListData,mList_videos, new OnItemClickListener() {
            @Override
            public void onItemClick(YoutubeDataModel item) {
                YoutubeDataModel youtubeDataModel = item;
                Intent intent = new Intent(ChannelActivity.this, VideoPlayerActivity.class);
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
}
