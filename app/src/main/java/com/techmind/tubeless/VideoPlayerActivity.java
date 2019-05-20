package com.techmind.tubeless;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.ProgressDialog;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.DrawableRes;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.util.Rational;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
//import com.google.android.youtube.player.YouTubeBaseActivity;
//import com.google.android.youtube.player.YouTubeInitializationResult;
//import com.google.android.youtube.player.YouTubePlayer;
//import com.google.android.youtube.player.YouTubeBaseActivity;
//import com.google.android.youtube.player.YouTubePlayerView;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.squareup.picasso.Picasso;
import com.techmind.tubeless.Sqlite.PostsDatabaseHelper;
import com.techmind.tubeless.adapters.CommentAdapter;
import com.techmind.tubeless.adapters.VideoPostAdapter;
import com.techmind.tubeless.config.AppController;
import com.techmind.tubeless.config.ConstURL;
import com.techmind.tubeless.interfaces.OnItemClickListener;
import com.techmind.tubeless.models.YoutubeCommentModel;
import com.techmind.tubeless.models.YoutubeDataModel;
import com.techmind.tubeless.util.ConnectionDetector;
import com.techmind.tubeless.util.Localization;
import com.techmind.tubeless.util.PermissionHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import at.huber.youtubeExtractor.YouTubeUriExtractor;
import at.huber.youtubeExtractor.YtFile;

import static com.techmind.tubeless.config.ConstURL.CHANNEL_GET_URL;
import static com.techmind.tubeless.config.ConstURL.GOOGLE_YOUTUBE_API_KEY;
import static com.techmind.tubeless.config.ConstURL.VIDEOS_TYPE;
import static com.techmind.tubeless.util.AnimationUtils.animateView;


public class VideoPlayerActivity extends AppCompatActivity implements View.OnClickListener,
        View.OnLongClickListener {
    private static final int READ_STORAGE_PERMISSION_REQUEST_CODE = 1;
    private YoutubeDataModel youtubeDataModel = null;
    TextView videoTitleTextView;
    TextView textViewDes;
    private ImageView uploaderThumb;
    JSONObject jsonObjUserDetail = new JSONObject();
    // ImageView imageViewIcon;
    public static final String VIDEO_ID = "c2UNv38V6y4";
    private YouTubePlayerView mYoutubePlayerView = null;
    private YouTubePlayer mYoutubePlayer = null;
    private ArrayList<YoutubeDataModel> mListData = new ArrayList<>();
    private CommentAdapter mAdapter = null;
    private RecyclerView mList_videos = null;
    private String pageToken;
    private VideoPostAdapter adapter = null;
    ArrayList<YoutubeDataModel> mList = new ArrayList<>();
    private ImageButton img_bookmark;
    private boolean bookmarkedId = false;
    private ImageView videoTitleToggleArrow;
    private LinearLayout videoDescriptionRootLayout;
    private View videoTitleRoot;
    private TextView uploaderTextView;
    private TextView videoCountView;

    private TextView detailControlsBackground;
    private TextView detailControlsPopup;
    private TextView detailControlsAddToPlaylist;
    private TextView detailControlsDownload;
    private TextView appendControlsDetail;
    private ProgressBar loadingProgressBar;

    protected View errorPanelRoot;
    protected Button errorButtonRetry;
    protected TextView errorTextView;
    private TextView videoUploadDateView;
    private View uploaderRootLayout;
    private TextView thumbsUpTextView;
    private ImageView thumbsUpImageView;
    private TextView thumbsDownTextView;
    private ImageView thumbsDownImageView;
    private LinearLayout activityDetailsLayout;
    private ImageView popupBtn;
    private PictureInPictureParams.Builder pictureInPictureParamsBuilder;
    private LinearLayout scrollLinearLayout;
    private String mPlay;
    private String mPause;
    /**
     * Intent action for media controls from Picture-in-Picture mode.
     */
    private static final String ACTION_MEDIA_CONTROL = "media_control";

    /**
     * Intent extra for media controls from Picture-in-Picture mode.
     */
    private static final String EXTRA_CONTROL_TYPE = "control_type";

    /**
     * The request code for play action PendingIntent.
     */
    private static final int REQUEST_PLAY = 1;

    /**
     * The request code for pause action PendingIntent.
     */
    private static final int REQUEST_PAUSE = 2;

    /**
     * The request code for info action PendingIntent.
     */
    private static final int REQUEST_INFO = 3;

    /**
     * The intent extra value for play action.
     */
    private static final int CONTROL_TYPE_PLAY = 1;

    /**
     * The intent extra value for pause action.
     */
    private static final int CONTROL_TYPE_PAUSE = 2;
    private BroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        {
            youtubeDataModel = getIntent().getParcelableExtra(YoutubeDataModel.class.toString());
            mYoutubePlayerView = findViewById(R.id.youtube_player_view);
            getLifecycle().addObserver(mYoutubePlayerView);

            mYoutubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                @Override
                public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                    youTubePlayer.loadVideo(youtubeDataModel.getVideo_id(), 0);
                }
            });
        }
        {
            uploaderTextView = findViewById(R.id.detail_uploader_text_view);
            activityDetailsLayout = findViewById(R.id.activityDetailsLayout);
            videoTitleRoot = findViewById(R.id.detail_title_root_layout);
            videoTitleTextView = findViewById(R.id.detail_video_title_view);
            videoDescriptionRootLayout = findViewById(R.id.detail_description_root_layout);
            videoTitleToggleArrow = findViewById(R.id.detail_toggle_description_view);
            textViewDes = (TextView) findViewById(R.id.detail_description_view);
            scrollLinearLayout = findViewById(R.id.scrollLinearLayout);
            img_bookmark = findViewById(R.id.img_bookmark);
            uploaderThumb = findViewById(R.id.detail_uploader_thumbnail_view);

            videoTitleTextView.setText(youtubeDataModel.getTitle());
            textViewDes.setText(youtubeDataModel.getDescription());

            videoTitleRoot = findViewById(R.id.detail_title_root_layout);
            videoTitleTextView = findViewById(R.id.detail_video_title_view);
            videoTitleToggleArrow = findViewById(R.id.detail_toggle_description_view);
            videoCountView = findViewById(R.id.detail_view_count_view);
            loadingProgressBar = findViewById(R.id.loading_progress_bar);

            errorPanelRoot = findViewById(R.id.error_panel);
            errorButtonRetry = findViewById(R.id.error_button_retry);
            errorTextView = findViewById(R.id.error_message_view);
            scrollLinearLayout.setVisibility(View.GONE);

            thumbsUpTextView = findViewById(R.id.detail_thumbs_up_count_view);
            thumbsUpImageView = findViewById(R.id.detail_thumbs_up_img_view);
            thumbsDownTextView = findViewById(R.id.detail_thumbs_down_count_view);
            thumbsDownImageView = findViewById(R.id.detail_thumbs_down_img_view);
            uploaderRootLayout = findViewById(R.id.detail_uploader_root_layout);
            videoUploadDateView = findViewById(R.id.detail_upload_date_view);
            popupBtn = findViewById(R.id.popupBtn);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            pictureInPictureParamsBuilder =
                    new PictureInPictureParams.Builder();
            // Prepare string resources for Picture-in-Picture actions.
            mPlay = getString(R.string.play);
            mPause = getString(R.string.pause);
        }
        popupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if (Build.VERSION.SDK_INT >= 26) {
                    if (!PermissionHelper.isPopupEnabled(getApplicationContext())) {
                        PermissionHelper.showPopupEnablementToast(getApplicationContext());
                        return;
                    }
                }*/
                if (Build.VERSION.SDK_INT >= 26) {
                    //Trigger PiP mode
                    try {
                        startPictureInPictureFeature();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(VideoPlayerActivity.this, "API 26 needed to perform PiP", Toast.LENGTH_SHORT).show();
                }
            }

        });
        {
            if (!TextUtils.isEmpty(youtubeDataModel.getPublishedAt())) {
                videoUploadDateView.setText(Localization.localizeDate(this, youtubeDataModel.getPublishedAt()));
            }
            mList_videos = (RecyclerView) findViewById(R.id.mList_videos);
            mList_videos.setLayoutManager(new LinearLayoutManager(this));
            mList_videos.hasFixedSize();

            videoTitleRoot.setOnClickListener(this);
            uploaderRootLayout.setOnClickListener(this);
            img_bookmark.setOnClickListener(this);
            checkBookmarkTag();
            ViewCompat.setNestedScrollingEnabled(mList_videos, false);
        }
        {
            ConnectionDetector connectionDetector = new ConnectionDetector(getApplicationContext());
            if (connectionDetector.isConnectingToInternet()) {
                animateView(errorButtonRetry, true, 600);
                getRelatedVideoListFromServer("https://www.googleapis.com/youtube/v3/search?part=snippet&type=video" +
                        "&part=contentDetails&relatedToVideoId=" + youtubeDataModel.getVideo_id() + "&maxResults=10&key=" + GOOGLE_YOUTUBE_API_KEY);
            } else {
                animateView(errorButtonRetry, false, 0);
                animateView(errorPanelRoot, true, 300);
            }

            if (!checkPermissionForReadExtertalStorage()) {
                try {
                    requestPermissionForReadExtertalStorage();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            setVideoStatisticsDetails();
        }
    }

    private void startPictureInPictureFeature() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            System.out.println("mYoutubePlayerView.getWidth() = " + mYoutubePlayerView.getWidth());
            System.out.println(" mYoutubePlayerView.getHeight() = " + mYoutubePlayerView.getHeight());
            Rational aspectRatio = new Rational(mYoutubePlayerView.getWidth(), mYoutubePlayerView.getHeight());
            pictureInPictureParamsBuilder.setAspectRatio(aspectRatio).build();
            enterPictureInPictureMode(pictureInPictureParamsBuilder.build());
        }
    }

    @Override
    public void onUserLeaveHint() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!isInPictureInPictureMode()) {
                Rational aspectRatio = new Rational(mYoutubePlayerView.getWidth(), mYoutubePlayerView.getHeight());
                pictureInPictureParamsBuilder.setAspectRatio(aspectRatio).build();
                enterPictureInPictureMode(pictureInPictureParamsBuilder.build());
            }
        }
    }

    private void setVideoStatisticsDetails() {
        getStatisticsResponse(channelIdStatisticsQuery(youtubeDataModel.getChannel_id()), youtubeDataModel, false);
        if (!TextUtils.isEmpty(youtubeDataModel.getChannelTitle())) {
            uploaderTextView.setText(youtubeDataModel.getChannelTitle());
            uploaderTextView.setVisibility(View.VISIBLE);
            uploaderTextView.setSelected(true);
        } else {
            uploaderTextView.setVisibility(View.GONE);
        }
        uploaderThumb.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.buddy));

        if (!youtubeDataModel.getViewCount().isEmpty() && Long.parseLong(youtubeDataModel.getViewCount()) >= 0) {
            videoCountView.setText(Localization.localizeViewCount(this, Long.parseLong(youtubeDataModel.getViewCount())));
            videoCountView.setVisibility(View.VISIBLE);
        } else {
            videoCountView.setVisibility(View.GONE);
        }

        if (!youtubeDataModel.getDislikeCount().isEmpty() && Long.parseLong(youtubeDataModel.getDislikeCount()) == -1
                && !youtubeDataModel.getLikeCount().isEmpty() && Long.parseLong(youtubeDataModel.getLikeCount()) == -1) {
            thumbsDownImageView.setVisibility(View.VISIBLE);
            thumbsUpImageView.setVisibility(View.VISIBLE);
            thumbsUpTextView.setVisibility(View.GONE);
            thumbsDownTextView.setVisibility(View.GONE);
        } else {
            if (!youtubeDataModel.getDislikeCount().isEmpty() && Long.parseLong(youtubeDataModel.getDislikeCount()) >= 0) {
                thumbsDownTextView.setText(Localization.shortCount(this, Long.parseLong(youtubeDataModel.getDislikeCount())));
                thumbsDownTextView.setVisibility(View.VISIBLE);
                thumbsDownImageView.setVisibility(View.VISIBLE);
            } else {
                thumbsDownTextView.setVisibility(View.GONE);
                thumbsDownImageView.setVisibility(View.GONE);
            }

            if (!youtubeDataModel.getLikeCount().isEmpty() && Long.parseLong(youtubeDataModel.getLikeCount()) >= 0) {
                thumbsUpTextView.setText(Localization.shortCount(this, Long.parseLong(youtubeDataModel.getLikeCount())));
                thumbsUpTextView.setVisibility(View.VISIBLE);
                thumbsUpImageView.setVisibility(View.VISIBLE);
            } else {
                thumbsUpTextView.setVisibility(View.GONE);
                thumbsUpImageView.setVisibility(View.GONE);
            }
        }

        /*if (!youtubeDataModel.getDuration().isEmpty() &&Long.parseLong(youtubeDataModel.getDuration()) > 0) {
            detailDurationView.setText(Localization.getDurationString(Long.parseLong(youtubeDataModel.getDuration())));
            detailDurationView.setBackgroundColor(ContextCompat.getColor(this, R.color.duration_background_color));
            animateView(detailDurationView, true, 100);
        } else {
            detailDurationView.setVisibility(View.GONE);
        }*/
        if (!TextUtils.isEmpty(youtubeDataModel.getUploaderAvatarUrl())) {
        }
        if (videoTitleToggleArrow != null) {
            videoTitleRoot.setClickable(true);
            videoTitleToggleArrow.setVisibility(View.VISIBLE);
            videoTitleToggleArrow.setImageResource(R.drawable.arrow_down);
            videoDescriptionRootLayout.setVisibility(View.GONE);
        } else {
            videoDescriptionRootLayout.setVisibility(View.VISIBLE);
        }
        if (!TextUtils.isEmpty(youtubeDataModel.getPublishedAt())) {
            videoUploadDateView.setText(Localization.localizeDate(this, youtubeDataModel.getPublishedAt()));
        }
    }

    private void getVideoStatistics(String url) {
        System.out.println("Get Video statistics*************= " + url);
        //Retrieving response from the server
        JsonObjectRequest js = new JsonObjectRequest(Request.Method.GET, url, jsonObjUserDetail,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("response Channel Api = " + response);
                        mListData = parseTrendingVideoStatistics(response);
                        initList(mListData);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplication(), "Server is not reachable!!! " + error, Toast.LENGTH_SHORT).show();
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

    private ArrayList<YoutubeDataModel> parseTrendingVideoStatistics(JSONObject response) {
        return null;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.detail_title_root_layout:
                toggleTitleAndDescription();
                break;
            case R.id.detail_uploader_root_layout:
                Intent intent = new Intent(VideoPlayerActivity.this, ChannelPlaylistActivityWithoutAnim.class);
                intent.putExtra(YoutubeDataModel.class.toString(), youtubeDataModel);
                System.out.println("youtubeDataModel.getUploaderAvatarUrl() = " + youtubeDataModel.getUploaderAvatarUrl());
                intent.putExtra("requestStatistics", true);
                startActivity(intent);
//                toggleTitleAndDescription();
                break;
            case R.id.img_bookmark:
                // Add sample post to the database
                System.out.println("OnClicked Video_id=************** " + youtubeDataModel.getVideo_id());
                if (!bookmarkedId) {
                    youtubeDataModel.setKind(VIDEOS_TYPE);
                    if (PostsDatabaseHelper.getInstance(v.getContext()).addPost(youtubeDataModel, VIDEOS_TYPE)) {
//                    Toast.makeText(getApplicationContext(),"Channel is Bookmarked successfully",Toast.LENGTH_SHORT).show();
                        checkBookmarkTag();
                    } else {
//                        Toast.makeText(getApplicationContext(), "Channel is already Bookmarked", Toast.LENGTH_SHORT).show();
                    }
                } else {

                    if (PostsDatabaseHelper.getInstance(v.getContext()).deleteId(youtubeDataModel.getVideo_id())) {
//                        Toast.makeText(getApplicationContext(), "Channel bookmarked is removed successfully", Toast.LENGTH_SHORT).show();
                        checkBookmarkTag();
                    }
                }
                break;
        }
    }

    private String videosIdStatisticsQuery(String ids) {
        return "https:///www.googleapis.com/youtube/v3/videos?part=contentDetails,statistics&id=" + ids + "&key=" + ConstURL.GOOGLE_YOUTUBE_API_KEY;
    }

    private void toggleTitleAndDescription() {
        if (videoTitleToggleArrow != null) {    //it is null for tablets
            if (videoDescriptionRootLayout.getVisibility() == View.VISIBLE) {
                videoTitleTextView.setMaxLines(1);
                videoDescriptionRootLayout.setVisibility(View.GONE);
                videoTitleToggleArrow.setImageResource(R.drawable.arrow_down);
            } else {
                videoTitleTextView.setMaxLines(10);
                videoDescriptionRootLayout.setVisibility(View.VISIBLE);
                videoTitleToggleArrow.setImageResource(R.drawable.arrow_up);
            }
        }
    }

    private void getRelatedVideoListFromServer(String url) {
        System.out.println("GET_RELATED_VIDEO_LIST_URL*************= " + url);
        if (loadingProgressBar != null) animateView(loadingProgressBar, true, 400);
        animateView(errorPanelRoot, false, 150);
        //Retrieving response from the server
        JsonObjectRequest js = new JsonObjectRequest(Request.Method.GET, url, jsonObjUserDetail,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
//                        showLoading();
                        System.out.println("response related video list Api = " + response);
                        mListData = parseTrendingVideoListFromResponse(response);
                        initList(mListData);

                        if (loadingProgressBar != null) animateView(loadingProgressBar, false, 0);
                        animateView(errorPanelRoot, false, 150);
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplication(), "Server is not reachable!!! " + error, Toast.LENGTH_SHORT).show();
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

    /* public void showLoading() {
         loadingProgressBar.setVisibility(View.VISIBLE);
         if (loadingProgressBar != null) animateView(loadingProgressBar, true, 400);
         animateView(errorPanelRoot, false, 150);
     }*/
   /* public void showEmptyState() {
        loadingProgressBar.setVisibility(View.INVISIBLE);
        if (loadingProgressBar != null) animateView(loadingProgressBar, false, 0);
        animateView(errorPanelRoot, false, 150);
    }*/
    private void initList(ArrayList<YoutubeDataModel> mListData) {
        adapter = new VideoPostAdapter(getApplicationContext(), mListData, mList_videos, new OnItemClickListener() {
            @Override
            public void onItemClick(YoutubeDataModel item) {
                ConnectionDetector connectionDetector = new ConnectionDetector(getApplicationContext());
                if (connectionDetector.isConnectingToInternet()) {
                    if (item != null && !item.getVideo_id().isEmpty()) {
                        getStatisticsResponse(videosIdStatisticsQuery(item.getVideo_id()), item, true);
                    }
                } else {
                    Snackbar snackbar = Snackbar.make(activityDetailsLayout, "Check Network Connection", Snackbar.LENGTH_LONG);
                    // Changing message text color
//                    snackbar.setActionTextColor(Color.RED);
                    // Changing action button text color
                    View sbView = snackbar.getView();
                    TextView textView = (TextView) sbView.findViewById(R.id.snackbar_text);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    } else {
                        textView.setGravity(Gravity.CENTER_HORIZONTAL);
                    }
                    textView.setTextColor(Color.WHITE);
                    snackbar.show();

                }
            }
        });
        mList_videos.setAdapter(adapter);
//        mList_videos.smoothScrollToPosition(previousListPosition);
    }

    private void checkBookmarkTag() {
        if (PostsDatabaseHelper.getInstance(getApplicationContext()).checkTypeIdExistsOrNot(youtubeDataModel.getVideo_id()) == -1) {
            img_bookmark.setImageResource(R.drawable.ic_bookmarks_outline);
            bookmarkedId = false;
        } else {
            img_bookmark.setImageResource(R.drawable.ic_bookmarks_color);
            bookmarkedId = true;
        }
    }

    private void getStatisticsResponse(String url, YoutubeDataModel item, Boolean passToNextActivity) {
        System.out.println("Request_Statistics_URL=****************** " + url);
        JsonObjectRequest js = new JsonObjectRequest(Request.Method.GET, url, jsonObjUserDetail,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("Get Statistics Response Api************* = " + response);
                        Intent intent;
                        parseTrendingStatisticsResponse(response, item);
                        if (passToNextActivity) {
                            if (item.getKind().equalsIgnoreCase("youtube#video")) {
//                                parseTrendingStatisticsResponse(response, item);
                                intent = new Intent(getApplicationContext(), VideoPlayerActivity.class);
                                intent.putExtra(YoutubeDataModel.class.toString(), item);
                                intent.putExtra("activity", "VideoPlayerActivity");
                                startActivity(intent);
                            }
                        }
                        overridePendingTransition(R.animator.right_in, R.animator.left_out);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(VideoPlayerActivity.this, "Server is not reachable!!! " + error, Toast.LENGTH_SHORT).show();
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
        if (jsonObject.has("items")) {
            try {
                JSONArray jsonArray = jsonObject.getJSONArray("items");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    String video_id = "";
//                    YoutubeDataModel youtubeObject = new YoutubeDataModel();
                    String kind = json.getString("kind");
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
                        Picasso.get().load(youtubeDataModel.getUploaderAvatarUrl()).into(uploaderThumb);

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
//            adapter.notifyDataSetChanged();
        }
    }


    public ArrayList<YoutubeDataModel> parseTrendingVideoListFromResponse(JSONObject jsonObject) {


        if (jsonObject.has("items")) {
            try {
                pageToken = jsonObject.getString("nextPageToken");
                JSONArray jsonArray = jsonObject.getJSONArray("items");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    if (json.has("id")) {
                        String video_id = "";
                        JSONObject jsonObj = json.getJSONObject("id");
                        String kind = "";
                        if (jsonObj.has("videoId")) {
                            video_id = jsonObj.getString("videoId");
                            kind = jsonObj.getString("kind");
                        }
                        if (json.has("kind")) {
                            if (json.getString("kind").equals("youtube#video") || json.getString("kind").equals("youtube#searchResult")) {
                                YoutubeDataModel youtubeObject = new YoutubeDataModel();
                                JSONObject jsonSnippet = json.getJSONObject("snippet");
                                String title = jsonSnippet.getString("title");
                                if (json.has("videoId")) {
                                    video_id = json.getString("videoId");
                                }
                                youtubeObject.setChannel_id(jsonSnippet.getString("channelId"));
                                String description = jsonSnippet.getString("description");
                                String publishedAt = jsonSnippet.getString("publishedAt");

                                String thumbnailHigh = jsonSnippet.getJSONObject("thumbnails").getJSONObject("high").getString("url");
                                String thumbnailMedium = jsonSnippet.getJSONObject("thumbnails").getJSONObject("medium").getString("url");
                                String thumbnailDefault = jsonSnippet.getJSONObject("thumbnails").getJSONObject("default").getString("url");
                                youtubeObject.setChannelTitle(jsonSnippet.getString("channelTitle"));
                                youtubeObject.setKind(kind);
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

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return mList;

    }

    private void nextPageToken(String pageToken) {
        CHANNEL_GET_URL = "https://www.googleapis.com/youtube/v3/search?pageToken=" + pageToken + "&part=snippet&chart=mostPopular&regionCode=IN&" +
                "maxResults=10&key=" + ConstURL.GOOGLE_YOUTUBE_API_KEY + "&part=contentDetails";
        getRelatedVideoListFromServer(CHANNEL_GET_URL);

    }

    public void share_btn_pressed(View view) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        String link = ("https://www.youtube.com/watch?v=" + youtubeDataModel.getVideo_id());
        // this is the text that will be shared
        sendIntent.putExtra(Intent.EXTRA_TEXT, link);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, youtubeDataModel.getTitle()
                + "Share");

        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "share"));
    }

    public void downloadVideo(View view) {
        //get the download URL
        String youtubeLink = ("https://www.youtube.com/watch?v=" + youtubeDataModel.getVideo_id());
        YouTubeUriExtractor ytEx = new YouTubeUriExtractor(this) {
            @Override
            public void onUrisAvailable(String videoID, String videoTitle, SparseArray<YtFile> ytFiles) {
                if (ytFiles != null) {
                    int itag = 22;
                    //This is the download URL
                    String downloadURL = ytFiles.get(itag).getUrl();
                    Log.e("download URL :", downloadURL);

                    //now download it like a file
                    new RequestDownloadVideoStream().execute(downloadURL, videoTitle);


                }

            }
        };

        ytEx.execute(youtubeLink);
    }

    private ProgressDialog pDialog;

    @Override
    public boolean onLongClick(View view) {
        return false;
    }


    private class RequestDownloadVideoStream extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(VideoPlayerActivity.this);
            pDialog.setMessage("Downloading file. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setMax(100);
            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            InputStream is = null;
            URL u = null;
            int len1 = 0;
            int temp_progress = 0;
            int progress = 0;
            try {
                u = new URL(params[0]);
                is = u.openStream();
                URLConnection huc = (URLConnection) u.openConnection();
                huc.connect();
                int size = huc.getContentLength();

                if (huc != null) {
                    String file_name = params[1] + ".mp4";
                    String storagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/YoutubeVideos";
                    File f = new File(storagePath);
                    if (!f.exists()) {
                        f.mkdir();
                    }

                    FileOutputStream fos = new FileOutputStream(f + "/" + file_name);
                    byte[] buffer = new byte[1024];
                    int total = 0;
                    if (is != null) {
                        while ((len1 = is.read(buffer)) != -1) {
                            total += len1;
                            // publishing the progress....
                            // After this onProgressUpdate will be called
                            progress = (int) ((total * 100) / size);
                            if (progress >= 0) {
                                temp_progress = progress;
                                publishProgress("" + progress);
                            } else
                                publishProgress("" + temp_progress + 1);

                            fos.write(buffer, 0, len1);
                        }
                    }

                    if (fos != null) {
                        publishProgress("" + 100);
                        fos.close();
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            pDialog.setProgress(Integer.parseInt(values[0]));
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (pDialog.isShowing())
                pDialog.dismiss();
        }

    }

    public ArrayList<YoutubeCommentModel> parseJson(JSONObject jsonObject) {
        ArrayList<YoutubeCommentModel> mList = new ArrayList<>();

        if (jsonObject.has("items")) {
            try {
                JSONArray jsonArray = jsonObject.getJSONArray("items");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);

                    YoutubeCommentModel youtubeObject = new YoutubeCommentModel();
                    JSONObject jsonTopLevelComment = json.getJSONObject("snippet").getJSONObject("topLevelComment");
                    JSONObject jsonSnippet = jsonTopLevelComment.getJSONObject("snippet");
                    String title = jsonSnippet.getString("authorDisplayName");
                    String thumbnail = jsonSnippet.getString("authorProfileImageUrl");
                    String comment = jsonSnippet.getString("textDisplay");

                    youtubeObject.setTitle(title);
                    youtubeObject.setComment(comment);
                    youtubeObject.setThumbnailHigh(thumbnail);
                    mList.add(youtubeObject);


                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return mList;

    }

    public void requestPermissionForReadExtertalStorage() throws Exception {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ActivityCompat.requestPermissions((Activity) this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        READ_STORAGE_PERMISSION_REQUEST_CODE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public boolean checkPermissionForReadExtertalStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            int result2 = this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            return (result == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED);
        }
        return false;
    }

    private void requestYoutubeCommentAPI() {
        String VIDEO_COMMENT_URL = "https://www.googleapis.com/youtube/v3/commentThreads?part=snippet&maxResults=100&videoId=" + youtubeDataModel.getVideo_id() + "&key=" + GOOGLE_YOUTUBE_API_KEY;

        JSONObject jsonObjUserDetail = new JSONObject();
        System.out.println("VIDEO_COMMENT_URL*************= " + VIDEO_COMMENT_URL);
        //Retrieving response from the server
        JsonObjectRequest js = new JsonObjectRequest(Request.Method.GET, VIDEO_COMMENT_URL, jsonObjUserDetail,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
//                        mListData = parseJson(response);
//                        initVideoList(mListData);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(getApplicationContext(), "Server is not reachable!!! " + error, Toast.LENGTH_SHORT).show();
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
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && !isInPictureInPictureMode()) {
//            if (mYoutubePlayer != null && !mYoutubePlayer.isPlaying())
//                mYoutubePlayer.play();
            // Continue playback...
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && isInPictureInPictureMode()) {
//            if (mYoutubePlayer != null && !mYoutubePlayer.isPlaying())
//                mYoutubePlayer.play();
            // Continue playback...
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        this.overridePendingTransition(R.animator.left_to_right, R.animator.right_to_left);
        super.onBackPressed();
    }

    private String channelIdStatisticsQuery(String ids) {
        return "https://www.googleapis.com/youtube/v3/channels?part=snippet,statistics&id=" + ids + "&key=" + ConstURL.GOOGLE_YOUTUBE_API_KEY;
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        if (isInPictureInPictureMode) {
//            if (mYoutubePlayer != null && !mYoutubePlayer.isPlaying())
//                mYoutubePlayer.play();
//            if (mYoutubePlayer != null && !mYoutubePlayer.isPlaying())
//                mYoutubePlayer.play();
            // Hide the full-screen UI (controls, etc.) while in picture-in-picture mode.
            System.out.println("isInPictureInPictureMode = " + isInPictureInPictureMode);
        } else {
            scrollLinearLayout.setVisibility(View.VISIBLE);
//            if (mYoutubePlayer != null && !mYoutubePlayer.isPlaying())
//                mYoutubePlayer.play();
//                mYoutubePlayer.play();
//            if (mYoutubePlayer != null && !mYoutubePlayer.isPlaying())
//                mYoutubePlayer.play();
            // Restore the full-screen UI.
            System.out.println("isInPictureInPictureMode = " + isInPictureInPictureMode);

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

    private void adjustFullScreen(Configuration config) {
        final View decorView = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                decorView.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                scrollLinearLayout.setVisibility(View.GONE);
            } else {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                scrollLinearLayout.setVisibility(View.VISIBLE);
            }
        }
    }
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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            final ArrayList<RemoteAction> actions = new ArrayList<>();

            // This is the PendingIntent that is invoked when a user clicks on the action item.
            // You need to use distinct request codes for play and pause, or the PendingIntent won't
            // be properly updated.
            final PendingIntent intent =
                    PendingIntent.getBroadcast(
                            VideoPlayerActivity.this,
                            requestCode,
                            new Intent(ACTION_MEDIA_CONTROL).putExtra(EXTRA_CONTROL_TYPE, controlType),
                            0);
            final Icon icon;
            icon = Icon.createWithResource(VideoPlayerActivity.this, iconId);
            actions.add(new RemoteAction(icon, title, title, intent));

            pictureInPictureParamsBuilder.setActions(actions);

            // This is how you can update action items (or aspect ratio) for Picture-in-Picture mode.
            // Note this call can happen even when the app is not in PiP mode. In that case, the
            // arguments will be used for at the next call of #enterPictureInPictureMode.
            setPictureInPictureParams(pictureInPictureParamsBuilder.build());
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration configuration) {
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
}
