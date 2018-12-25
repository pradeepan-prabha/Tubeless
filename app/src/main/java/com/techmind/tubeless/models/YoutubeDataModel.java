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
    private String ThumbnailMedium = "";

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

    private String ThumbnailDefault = "";
    private String video_id = "";

    public String getKind() {
        return kind;
    }

    private String channel_id= "";

    public String getPlayList_id() {
        return playList_id;
    }

    public void setPlayList_id(String playList_id) {
        this.playList_id = playList_id;
    }

    private String playList_id= "";

    public void setKind(String kind) {
        this.kind = kind;
    }
    private String kind = "";


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
