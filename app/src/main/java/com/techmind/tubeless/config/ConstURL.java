package com.techmind.tubeless.config;

public class ConstURL {
    public final static String GOOGLE_YOUTUBE_API_KEY = "AIzaSyACgDfexg1uu-HzrpJhlZhidbQeBSs0gRY";
    //    public final static String PLAYLIST_ID = "PL7u4lWXQ3wfI_7PgX0C-VTiwLeu0S4v34";//unbox therapy
    public final static String PLAYLIST_ID = "PLeEP84ImH_Lse3Ij9oppjtpex3zSLXhk6";//Madan Gowri

    public static String CHANNEL_ID = "UCsTcErHg8oDvUnTzoqsYeNw";//unbox therapy
    //    public static String CHANNEL_ID = "UCoMdktPbSTixAyNGwb-UYkQ";
    public static String CHANNEL_GET_URL = "https://www.googleapis.com/youtube/v3/search?part=snippet&order=date&channelId=" +
            CHANNEL_ID + "&maxResults=10&key=" + GOOGLE_YOUTUBE_API_KEY + "&part=contentDetails";
    public static String CHANNEL_LIVE_GET_URL = "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&channelId=" +
            ConstURL.CHANNEL_ID + "&eventType=live&maxResults=10&key=" + ConstURL.GOOGLE_YOUTUBE_API_KEY + "";
    public static String CHANNEL_PLAYLIST_GET_URL = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&playlistId=" +
            ConstURL.PLAYLIST_ID + "&maxResults=10&key=" + ConstURL.GOOGLE_YOUTUBE_API_KEY + "";
    public static String TRENDING_VIDEOS_GET_URL = "https://www.googleapis.com/youtube/v3/search?part=snippet&chart=mostPopular&regionCode=IN&" +
            "maxResults=10&key=" + ConstURL.GOOGLE_YOUTUBE_API_KEY + "&part=contentDetail";
}