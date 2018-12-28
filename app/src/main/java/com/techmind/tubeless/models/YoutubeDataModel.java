package com.techmind.tubeless.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mdmunirhossain on 12/18/17.
 */

public class YoutubeDataModel implements Parcelable {
    private String title = "";
    private String description = "";
    private String publishedAt = "";
    private String ThumbnailHigh = "";
    private String kind = "";
    private String ThumbnailMedium = "";
    private String playList_id = "";
    private String channelTitle = "";
    private String ThumbnailDefault = "";
    private String video_id = "";
    private String channel_id = "";
    private String viewCount = "";
    private String likeCount = "";
    private String dislikeCount = "";
    private String favoriteCount = "";
    private String commentCount = "";
    private String subscriberCount = "";
    private String videoCount = "";

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    private String duration = "";

    public String getPlayListCount() {
        return playListCount;
    }

    public void setPlayListCount(String playListCount) {
        this.playListCount = playListCount;
    }

    private String playListCount = "";


    public String getViewCount() {
        return viewCount;
    }

    public void setViewCount(String viewCount) {
        this.viewCount = viewCount;
    }

    public String getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(String likeCount) {
        this.likeCount = likeCount;
    }

    public String getDislikeCount() {
        return dislikeCount;
    }

    public void setDislikeCount(String dislikeCount) {
        this.dislikeCount = dislikeCount;
    }

    public String getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(String favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public String getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(String commentCount) {
        this.commentCount = commentCount;
    }

    public String getSubscriberCount() {
        return subscriberCount;
    }

    public void setSubscriberCount(String subscriberCount) {
        this.subscriberCount = subscriberCount;
    }

    public String getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(String videoCount) {
        this.videoCount = videoCount;
    }


    public String getChannelTitle() {
        return channelTitle;
    }

    public void setChannelTitle(String channelTitle) {
        this.channelTitle = channelTitle;
    }


    public String getThumbnailMedium() {
        return ThumbnailMedium;
    }

    public void setThumbnailMedium(String thumbnailMedium) {
        ThumbnailMedium = thumbnailMedium;
    }

    public String getThumbnailDefault() {
        return ThumbnailDefault;
    }

    public void setThumbnailDefault(String thumbnailDefault) {
        ThumbnailDefault = thumbnailDefault;
    }

    public String getKind() {
        return kind;
    }


    public String getPlayList_id() {
        return playList_id;
    }

    public void setPlayList_id(String playList_id) {
        this.playList_id = playList_id;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getChannel_id() {
        return channel_id;
    }

    public void setChannel_id(String channel_id) {
        this.channel_id = channel_id;
    }

    public static Creator<YoutubeDataModel> getCREATOR() {
        return CREATOR;
    }

    public String getVideo_id() {
        return video_id;
    }

    public void setVideo_id(String video_id) {
        this.video_id = video_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getThumbnailHigh() {
        return ThumbnailHigh;
    }

    public void setThumbnailHigh(String ThumbnailHigh) {
        this.ThumbnailHigh = ThumbnailHigh;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(publishedAt);
        dest.writeString(ThumbnailHigh);
        dest.writeString(ThumbnailDefault);
        dest.writeString(ThumbnailMedium);
        dest.writeString(video_id);
        dest.writeString(channel_id);
        dest.writeString(kind);
        dest.writeString(playList_id);
        dest.writeString(channelTitle);
        dest.writeString(viewCount );
        dest.writeString(likeCount );
        dest.writeString(dislikeCount);
        dest.writeString(favoriteCount);
        dest.writeString(commentCount );
        dest.writeString(subscriberCount);
        dest.writeString(videoCount );
        dest.writeString(playListCount );
        dest.writeString(duration );
    }

    public YoutubeDataModel() {
        super();
    }


    protected YoutubeDataModel(Parcel in) {
        this();
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        this.title = in.readString();
        this.description = in.readString();
        this.publishedAt = in.readString();
        this.ThumbnailHigh = in.readString();
        this.ThumbnailMedium = in.readString();
        this.ThumbnailDefault = in.readString();
        this.video_id = in.readString();
        this.channel_id = in.readString();
        this.kind = in.readString();
        this.playList_id = in.readString();
        this.channelTitle = in.readString();
        this.viewCount = in.readString();
        this.likeCount = in.readString();
        this.dislikeCount = in.readString();
        this.favoriteCount = in.readString();
        this.commentCount = in.readString();
        this.subscriberCount = in.readString();
        this.videoCount = in.readString();
        this.playListCount = in.readString();
        this.duration = in.readString();

    }

    public static final Creator<YoutubeDataModel> CREATOR = new Creator<YoutubeDataModel>() {
        @Override
        public YoutubeDataModel createFromParcel(Parcel in) {
            return new YoutubeDataModel(in);
        }

        @Override
        public YoutubeDataModel[] newArray(int size) {
            return new YoutubeDataModel[size];
        }
    };
}
