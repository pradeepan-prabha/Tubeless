package com.techmind.tubeless;

import android.content.Context;
import android.util.Log;

import com.techmind.tubeless.config.ConstURL;
import com.techmind.tubeless.models.YoutubeDataModel;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Created by Abhishek on 14-Feb-18.
 */

public class YoutubeConnector {

    //Youtube object for executing api related queries through Youtube Data API
    private YouTube youtube;

    //custom list of youtube which gets returned when searched for keyword
    //Returns a collection of search results that match the query parameters specified in the API request
    //By default, a search result set identifies matching video, channel, and playlist resources,
    //but you can also configure queries to only retrieve a specific type of resource
    private YouTube.Search.List query;

    //Developer API key a developer can obtain after creating a new project in google developer console
    //Developer has to enable YouTube Data API v3 in the project
    //Add credentials and then provide the Application's package name and SHA fingerprint
    public static final String KEY = "AIzaSyACgDfexg1uu-HzrpJhlZhidbQeBSs0gRY";

    //Package name of the app that will call the YouTube Data API
    public static final String PACKAGENAME = "com.test.abhishek.searchyoutube";

    //SHA1 fingerprint of APP can be found by double clicking on the app signing report on right tab called gradle
    public static final String SHA1 = "49:36:21:D2:76:9A:02:CD:E6:E5:DD:12:AD:76:7A:BD:59:C5:F4:38";

    //maximum results that should be downloaded via the YouTube data API at a time
    private static final long MAXRESULTS = 5;
    private YouTube.Channels.List query1;

    //Constructor to properly initialize Youtube's object
    public YoutubeConnector(Context context) {

        //Youtube.Builder returns an instance of a new builder
        //Parameters:
        //transport - HTTP transport
        //jsonFactory - JSON factory
        //httpRequestInitializer - HTTP request initializer or null for none
        // This object is used to make YouTube Data API requests. The last
        // argument is required, but since we don't need anything
        // initialized when the HttpRequest is initialized, we override
        // the interface and provide a no-op function.
        youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {

            //initialize method helps to add any extra details that may be required to process the query
            @Override
            public void initialize(HttpRequest request) throws IOException {

                //setting package name and sha1 certificate to identify request by server
                request.getHeaders().set("X-Android-Package", "");
                request.getHeaders().set("X-Android-Cert", "");
            }
        }).setApplicationName("SearchYoutube").build();


    }

    public List<YoutubeDataModel> search(String keywords, String requiredResponse, Context context, String channelId) {
        try {
            System.out.println("requiredResponse = " + requiredResponse);

            // Restrict the search results to only include videos. See:
            // https://developers.google.com/youtube/v3/docs/search/list#type

            //setting fields which should be returned
            //setting only those fields which are rresponse Channel Apiequired
            //for maximum efficiency
            //here we are retreiving fiels:
            //-kind of video
            //-video ID
            //-title of video
            //-description of video
            //high quality thumbnail url of the video
            if (requiredResponse.equals(context.getString(R.string.channelKey))) {
                // Define the API request for retrieving search results.
                query = youtube.search().list("id,snippet");
                query.setType("channel");
                query.setFields("items(id/kind,id/videoId,id/channelId,snippet/title,snippet/description,snippet/thumbnails/high/url)");
                query.setQ(keywords);
                //max results that should be returned
                query.setMaxResults(MAXRESULTS);
            } else
                if (requiredResponse.equals(context.getString(R.string.videoKey))) {
                // Define the API request for retrieving search results.
                query = youtube.search().list("id,snippet");
                    query.getPageToken();
                query.setType("videos");
                query.setFields("items(id/kind,id/videoId,id/channelId,snippet/title,snippet/description,snippet/thumbnails/high/url)");
                query.setQ(keywords);
                //max results that should be returned
                query.setMaxResults(MAXRESULTS);
            } else if (requiredResponse.equals(context.getString(R.string.playlistKey))) {
                // Define the API request for retrieving search results.
                query = youtube.search().list("id,snippet,");
                query.setType("playlist");
                query.setFields("items(id/kind,id/videoId,snippet/title,snippet/description,snippet/thumbnails/high/url)");
                //max results that should be returned
                query.setMaxResults(MAXRESULTS);
            } else if (requiredResponse.equals(context.getString(R.string.channelBannerImageKey))) {
                // Define the API request for retrieving search results.
                query1 = youtube.channels().list("brandingSettings");
//                query.setType("channel");
                query.setChannelId(channelId);
//                query.setFields("items(id/kind,id/videoId,snippet/title,snippet/description,snippet/thumbnails/high/url)");
                query.setFields("brandingSettings/image/bannerMobileExtraHdImageUrl)");
            }
            //setting API key to query
            // Set your developer key from the {{ Google Cloud Console }} for
            // non-authenticated requests. See:
            // {{ https://cloud.google.com/console }}
            query.setKey(ConstURL.GOOGLE_YOUTUBE_API_KEY);
//                query.setKey(ConstURL.GOOGLE_YOUTUBE_API_KEY);

            System.out.println("query = " + query);
            //executing prepared query and calling Youtube API
            SearchListResponse response = query.execute();

            //retrieving list from response received
            //getItems method returns a list from the response which is originally in the form of JSON
            List<SearchResult> results = response.getItems();
            System.out.println("results Json response= " + results);
            //list of type VideoItem for saving all data individually
            List<YoutubeDataModel> items = new ArrayList<>();

            //check if result is found and call our setItemsList method
            if (results != null) {

                //iterator method returns a Iterator instance which can be used to iterate through all values in list
                items = setItemsList(results.iterator());
            }

            return items;

        } catch (IOException e) {

            //catch exception and print on console
            Log.d("YC", "Could not search: " + e);
            return null;
        }

    }

    //method for filling our array list
    private static List<YoutubeDataModel> setItemsList(Iterator<SearchResult> iteratorSearchResults) {

        //temporary list to store the raw data from the returned results
        List<YoutubeDataModel> tempSetItems = new ArrayList<>();

        //if no result then printing appropriate output
        if (!iteratorSearchResults.hasNext()) {
            System.out.println(" There aren't any results for your query.");
        }

        //iterating through all search results
        //hasNext() method returns true until it has no elements left to iterate
        while (iteratorSearchResults.hasNext()) {

            //next() method returns single instance of current video item
            //and returns next item everytime it is called
            //SearchResult is Youtube's custom result type which can be used to retrieve data of each video item
            SearchResult singleVideo = iteratorSearchResults.next();

            //getId() method returns the resource ID of one video in the result obtained
            ResourceId rId = singleVideo.getId();

            // Confirm that the result represents a video. Otherwise, the
            // item will not contain a video ID.
            //getKind() returns which type of resource it is which can be video, playlist or channel
            String channel = rId.getKind();
            System.out.println("channel ==================== " + channel);
            if (rId.getKind().equals("youtube#video") || rId.getKind().equals("youtube#channel")) {

                //object of VideoItem class that can be added to array list
                YoutubeDataModel item = new YoutubeDataModel();

                //getting High quality thumbnail object
                //URL of thumbnail is in the heirarchy snippet/thumbnails/high/url
                Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().getHigh();

                //retrieving title,description,thumbnail url, id from the heirarchy of each resource
                //Video ID - id/videoId
                //Title - snippet/title
                //Description - snippet/description
                //Thumbnail - snippet/thumbnails/high/url
                item.setVideo_id(singleVideo.getId().getVideoId());
                item.setKind(singleVideo.getId().getKind());
                item.setChannel_id(singleVideo.getId().getChannelId());
                item.setTitle(singleVideo.getSnippet().getTitle());
                item.setDescription(singleVideo.getSnippet().getDescription());
                item.setThumbnail(thumbnail.getUrl());

                //adding one Video item to temporary array list
                tempSetItems.add(item);

                //for debug purpose printing one by one details of each Video that was found
                System.out.println(" Video Id" + rId.getVideoId());
                System.out.println(" Title: " + singleVideo.getSnippet().getTitle());
                System.out.println(" Thumbnail: " + thumbnail.getUrl());
                System.out.println(" Description: " + singleVideo.getSnippet().getDescription());
                System.out.println("\n-------------------------------------------------------------\n");
            }
        }
        return tempSetItems;
    }
}