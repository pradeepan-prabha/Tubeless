package com.techmind.tubeless.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.techmind.tubeless.ChannelPlaylistActivity;
import com.techmind.tubeless.EndlessRecyclerViewScrollListener;
import com.techmind.tubeless.MyCustomObject;
import com.techmind.tubeless.R;
import com.techmind.tubeless.VideoPlayerActivity;
import com.techmind.tubeless.YoutubeAdapter;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class PopularVideosFragment extends Fragment {
    private RecyclerView mList_videos = null;
    private VideoPostAdapter adapter = null;
    private String pageToken;
    private ArrayList<YoutubeDataModel> mListData = new ArrayList<>();
    public static String CHANNEL_GET_URL;
    JSONObject jsonObjUserDetail = new JSONObject();

    private int previousListPosition;
    //ProgressDialog can be shown while downloading data from the internet
    //which indicates that the query is being processed
    private ProgressDialog mProgressDialog;

    //Handler to run a thread which could fill the list after downloading data
    //from the internet and inflating the images, title and description
    private Handler handler;

    //results list of type VideoItem to store the results so that each item
    //int the array list has id, title, description and thumbnail url
    private List<YoutubeDataModel> searchResults;
//    private List<VideoItem> searchResults;

    //EditText for input search keywords
    private EditText searchInput;
    private YoutubeAdapter youtubeAdapter;
    private String requestTypeCallbackStr;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private EndlessRecyclerViewScrollListener scrollListener;
    private String kind="";

    public PopularVideosFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_channel, container, false);
        mList_videos = (RecyclerView) view.findViewById(R.id.mList_videos);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mList_videos.setLayoutManager(linearLayoutManager);
        mSwipeRefreshLayout=view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                if(mListData!=null) {
                    mListData.clear();
                }
                getChannelListFromServer(ConstURL.TRENDING_VIDEOS_GET_URL);
            }
        });
        mProgressDialog = new ProgressDialog(getContext());
//        searchInput = (EditText)view.findViewById(R.id.search_input);

      /*  Button nextPage = view.findViewById(R.id.nextPage);
        nextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previousListPosition = mList.size();
                nextPageToken(pageToken);
                System.out.println("view = " + view);
            }
        });*/
//        initList(mListData);
        getChannelListFromServer(ConstURL.TRENDING_VIDEOS_GET_URL);


        //setting title and and style for progress dialog so that users can understand
        //what is happening currently
        mProgressDialog.setTitle("Searching...");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        //Fixing the size of recycler view which means that the size of the view
        //should not change if adapter or children size changes
//        mRecyclerView.setHasFixedSize(true);
        //give RecyclerView a layout manager to set its orientation to vertical
        //by default it is vertical
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

//        handler = new Handler();

/*        //add listener to the EditText view which listens to changes that occurs when
        //users changes the text or deletes the text
        //passing object of Textview's EditorActionListener to this method
        searchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            //onEditorAction method called when user clicks ok button or any custom
            //button set on the bottom right of keyboard
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                //actionId of the respective action is returned as integer which can
                //be checked with our set custom search button in keyboard
                if(actionId == EditorInfo.IME_ACTION_SEARCH){

                    //setting progress message so that users can understand what is happening
                    mProgressDialog.setMessage("Finding videos for "+v.getText().toString());

                    //displaying the progress dialog on the top of activity for two reasons
                    //1.user can see what is going on
                    //2.User cannot click anything on screen for time being
                    mProgressDialog.show();

                    //calling our search method created below with input keyword entered by user
                    //by getText method which returns Editable type, get string by toString method
                    searchOnYoutube(v.getText().toString());

                    //getting instance of the keyboard or any other input from which user types
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    //hiding the keyboard once search button is clicked
                    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                            InputMethodManager.RESULT_UNCHANGED_SHOWN);

                    return false;
                }
                return true;
            }
        });*/

        MyCustomObject.getInstance().setCustomObjectListener(new MyCustomObject.MyCustomObjectListener() {
            @Override
            public void onDataLoad(String title, String requestType) {
                requestTypeCallbackStr = requestType;
                //setting progress message so that users can understand what is happening
                mProgressDialog.setMessage("Finding videos for " + title.trim());

                //displaying the progress dialog on the top of activity for two reasons
                //1.user can see what is going on
                //2.User cannot click anything on screen for time being
                mProgressDialog.show();

                //calling our search method created below with input keyword entered by user
                //by getText method which returns Editable type, get string by toString method
                if (!title.isEmpty() && !requestType.isEmpty()) {
//                    searchOnYoutube(title, requestType);
                    mListData.clear();
                } else {
                    Toast.makeText(getContext(), "Select request Type", Toast.LENGTH_SHORT).show();
                }

                //getting instance of the keyboard or any other input from which user types
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                //hiding the keyboard once search button is clicked
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                        InputMethodManager.RESULT_UNCHANGED_SHOWN);
                System.out.println("title = " + title);
            }
        });
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
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
        // Adds the scroll listener to RecyclerView
        mList_videos.addOnScrollListener(scrollListener);

        return view;
    }

    private void initList(ArrayList<YoutubeDataModel> mListData) {
        adapter = new VideoPostAdapter(getActivity(), mListData,mList_videos, new OnItemClickListener() {
            private Intent intent;

            @Override
            public void onItemClick(YoutubeDataModel item) {
                YoutubeDataModel youtubeDataModel = item;
                if (youtubeDataModel.getKind().equalsIgnoreCase("youtube#channel")) {
//                    &&requestTypeCallbackStr.equalsIgnoreCase(getString(R.string.channelKey))
                    intent = new Intent(getActivity(), ChannelPlaylistActivity.class);
                    intent.putExtra(YoutubeDataModel.class.toString(), youtubeDataModel);
                    startActivity(intent);
//                    requestTypeCallbackStr.equalsIgnoreCase(getString(R.string.videoKey))
                } else if (youtubeDataModel.getKind().equalsIgnoreCase("youtube#video")) {
                    intent = new Intent(getActivity(), VideoPlayerActivity.class);
                    intent.putExtra(YoutubeDataModel.class.toString(), youtubeDataModel);
                    startActivity(intent);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
                    getActivity().overridePendingTransition(R.animator.right_in, R.animator.left_out);
                }
            }
        });
        mList_videos.setAdapter(adapter);
//        mList_videos.smoothScrollToPosition(previousListPosition);
    }


    public ArrayList<YoutubeDataModel> parseVideoListFromResponse(JSONObject jsonObject) {
        ArrayList<YoutubeDataModel> mList = new ArrayList<>();

        if (jsonObject.has("items")) {
            try {
                pageToken = jsonObject.getString("nextPageToken");
                System.out.println("pageToken = " + pageToken);
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
                        previousListPosition = mList.size();
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return mList;

    }
    public ArrayList<YoutubeDataModel> nextTokenVideoListFromResponse(JSONObject jsonObject) {

        ArrayList<YoutubeDataModel> mList = new ArrayList<>();

        if (jsonObject.has("items")) {
            try {
                if (jsonObject.has("jsonObject")) {
                    pageToken = jsonObject.getString("jsonObject");
                }
                JSONArray jsonArray = jsonObject.getJSONArray("items");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    if (json.has("kind")) {
                        if (json.getString("kind").equals("youtube#searchResult")) {
                            String video_id = "";
                            String channelId = "";
                            String kind="";
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
                            String thumbnail = jsonSnippet.getJSONObject("thumbnails").getJSONObject("high").getString("url");
                            System.out.println("thumbnail = " + thumbnail);

                            youtubeObject.setTitle(title);
                            youtubeObject.setDescription(description);
                            youtubeObject.setPublishedAt(publishedAt);
                            youtubeObject.setThumbnail(thumbnail);
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


    public ArrayList<YoutubeDataModel> parseTrendingVideoListFromResponse(JSONObject jsonObject) {
        ArrayList<YoutubeDataModel> mList = new ArrayList<>();

        if (jsonObject.has("items")) {
            try {
                pageToken = jsonObject.getString("nextPageToken");
                System.out.println("pageToken = " + pageToken);
                JSONArray jsonArray = jsonObject.getJSONArray("items");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    if (json.has("id")) {
                        String video_id = "";
                        video_id = json.getString("id");
                       /* if (jsonID.has("videoId")) {
                            video_id = jsonID.getString("videoId");
                        }*/
                        if (json.has("kind")) {
                            if (json.getString("kind").equals("youtube#video") || json.getString("kind").equals("youtube#searchResult")) {
                                YoutubeDataModel youtubeObject = new YoutubeDataModel();
                                JSONObject jsonSnippet = json.getJSONObject("snippet");
                                String title = jsonSnippet.getString("title");
                                System.out.println("title = " + title);
                                if (json.has("videoId")) {
                                    video_id = json.getString("videoId");
                                    System.out.println("video_id = " + video_id);
                                }
                                String description = jsonSnippet.getString("description");
                                System.out.println("description = " + description);
                                String publishedAt = jsonSnippet.getString("publishedAt");
                                System.out.println("publishedAt = " + publishedAt);
                                String thumbnail = jsonSnippet.getJSONObject("thumbnails").getJSONObject("high").getString("url");
                                System.out.println("thumbnail = " + thumbnail);

                                youtubeObject.setTitle(title);
                                youtubeObject.setDescription(description);
                                youtubeObject.setPublishedAt(publishedAt);
                                youtubeObject.setThumbnail(thumbnail);
                                youtubeObject.setKind(json.getString("kind"));
                                youtubeObject.setVideo_id(video_id);
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
                        ArrayList<YoutubeDataModel> nextTokenArrayList= new ArrayList<>();
                        nextTokenArrayList = requestVideoListFromResponse(response);
//                        initList(mListData);
                        System.out.println("nextTokenArrayList = " + nextTokenArrayList+"       Size="+nextTokenArrayList.size());
                        mListData.addAll(nextTokenArrayList);
                        System.out.println("mListData = " + mListData+"       Size="+mListData.size());
                        adapter.notifyItemInserted(mListData.size());
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                Toast.makeText(getActivity(), "Server is not reachable!!! " + error, Toast.LENGTH_SHORT).show();
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
    private void getChannelListFromServer(String url) {
        System.out.println("CHANNLE_GET_URL*************= " + url);
        //Retrieving response from the server
        JsonObjectRequest js = new JsonObjectRequest(Request.Method.GET, url, jsonObjUserDetail,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (mSwipeRefreshLayout.isRefreshing()) {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                        System.out.println("response Channel Api = " + response);
                        mListData = requestVideoListFromResponse(response);
                        initList(mListData);

                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                Toast.makeText(getActivity(), "Server is not reachable!!! " + error, Toast.LENGTH_SHORT).show();
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

    private void nextPageToken(String pageToken) {
        CHANNEL_GET_URL = "https://www.googleapis.com/youtube/v3/search?pageToken=" + pageToken + "&part=snippet&" +
                "maxResults=10&key=" + ConstURL.GOOGLE_YOUTUBE_API_KEY + "";
        getEndlessListFromServer(CHANNEL_GET_URL);

    }
    public ArrayList<YoutubeDataModel> requestVideoListFromResponse(JSONObject jsonObject) {

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
                            String channelId="";
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
                            String thumbnail = jsonSnippet.getJSONObject("thumbnails").getJSONObject("high").getString("url");
                            System.out.println("thumbnail = " + thumbnail);

                            youtubeObject.setTitle(title);
                            youtubeObject.setDescription(description);
                            youtubeObject.setPublishedAt(publishedAt);
                            youtubeObject.setThumbnail(thumbnail);
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
  /*  //custom search method which takes argument as the keyword for which videos is to be searched
    private void searchOnYoutube(final String keywords, final String requestType) {

        //A thread that will execute the searching and inflating the RecyclerView as and when
        //results are found
        new Thread() {

            //implementing run method
            public void run() {

                //create our YoutubeConnector class's object with Activity context as argument
                YoutubeConnector yc = new YoutubeConnector(getActivity());

                //calling the YoutubeConnector's search method by entered keyword
                //and saving the results in list of type VideoItem class
                searchResults = yc.search(keywords, requestType.toLowerCase(), getActivity(), "UCsTcErHg8oDvUnTzoqsYeNw");

                //handler's method used for doing changes in the UI
                handler.post(new Runnable() {

                    //implementing run method of Runnable
                    public void run() {

                        //call method to create Adapter for RecyclerView and filling the list
                        //with thumbnail, title, id and description
//                        fillYoutubeVideos();
                        initList((ArrayList<YoutubeDataModel>) searchResults);
                        //after the above has been done hiding the ProgressDialog
                        mProgressDialog.dismiss();
                    }
                });
            }
            //starting the thread
        }.start();
    }
*/
    //method for creating adapter and setting it to recycler view
    private void fillYoutubeVideos() {

        //object of YoutubeAdapter which will fill the RecyclerView
//        youtubeAdapter = new YoutubeAdapter(getContext(),searchResults);
        System.out.println("searchResults =************************************* " + searchResults);

        //setAdapter to RecyclerView
        mList_videos.setAdapter(youtubeAdapter);

        //notify the Adapter that the data has been downloaded so that list can be updapted
        youtubeAdapter.notifyDataSetChanged();
    }


   /* @Override
    public void onDataLoaded(String data) {
        System.out.println("data = " + data);

    }*/
}