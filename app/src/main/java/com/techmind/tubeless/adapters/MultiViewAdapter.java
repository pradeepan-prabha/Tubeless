package com.techmind.tubeless.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.techmind.tubeless.R;
import com.techmind.tubeless.config.ConstURL;
import com.techmind.tubeless.interfaces.OnItemClickListener;
import com.techmind.tubeless.models.YoutubeDataModel;
import com.techmind.tubeless.util.Localization;

import java.util.ArrayList;

/**
 * Created by mdmunirhossain on 12/18/17.
 */

public class MultiViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<YoutubeDataModel> dataSet;
    private Context mContext = null;
    private final OnItemClickListener listener;


    public MultiViewAdapter(Context mContext, ArrayList<YoutubeDataModel> dataSet, RecyclerView recyclerView, OnItemClickListener listener) {
        this.dataSet = dataSet;
        this.mContext = mContext;
        this.listener = listener;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        switch (viewType) {
            case ConstURL.CHANNEL_ID_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_channel_item, parent, false);
                return new ChannelViewHolder(view);
            case ConstURL.PLAYLIST_ID_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_playlist_item, parent, false);
                return new PlaylistViewHolder(view);
            case ConstURL.VIDEOS_ID_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_video_item, parent, false);
                return new VideosViewHolder(view);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {

        switch (dataSet.get(position).getKind()) {
            case "youtube#channel":
                return ConstURL.CHANNEL_ID_TYPE;
            case "youtube#playlist":
                return ConstURL.PLAYLIST_ID_TYPE;
            case "youtube#video":
                return ConstURL.VIDEOS_ID_TYPE;
            default:
                return -1;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        YoutubeDataModel object = dataSet.get(position);
        System.out.println("holder.getItemViewType() = " + holder.getItemViewType());
        switch (holder.getItemViewType()) {
            case ConstURL.CHANNEL_ID_TYPE:
                setChannelHolderUI(holder, object, position);
                break;
            case ConstURL.PLAYLIST_ID_TYPE:
                setPlayListHolderUI(holder, object, position);
                break;
            case ConstURL.VIDEOS_ID_TYPE:
                setVideoHolderUI(holder, object, position);
                break;
        }
    }

    private void setChannelHolderUI(RecyclerView.ViewHolder holder, YoutubeDataModel object, int position) {
        ((ChannelViewHolder) holder).textViewTitle.setText(object.getTitle());
        ((ChannelViewHolder) holder).itemUploaderView.setText(object.getChannelTitle());
        ((ChannelViewHolder) holder).detail_upload_date_view.setText(Localization.localizeDate(mContext, object.getPublishedAt()));
        ((ChannelViewHolder) holder).bind(dataSet.get(position), listener);
        //TODO: image will be downloaded from url
        Picasso.get().load(object.getThumbnailMedium()).into(((ChannelViewHolder) holder).ImageThumb);
    }

    private void setPlayListHolderUI(RecyclerView.ViewHolder holder, YoutubeDataModel object, int position) {
        ((PlaylistViewHolder) holder).textViewTitle.setText(object.getTitle());
        ((PlaylistViewHolder) holder).itemStreamCountView.setText(object.getVideoCount());
        ((PlaylistViewHolder) holder).itemUploaderView.setText(object.getChannelTitle());
        ((PlaylistViewHolder) holder).bind(dataSet.get(position), listener);
        //TODO: image will be downloaded from url
        Picasso.get().load(object.getThumbnailMedium()).into(((PlaylistViewHolder) holder).ImageThumb);
    }

    private void setVideoHolderUI(RecyclerView.ViewHolder holder, YoutubeDataModel object, int position) {
        ((VideosViewHolder) holder).textViewTitle.setText(object.getTitle());
        ((VideosViewHolder) holder).detail_description_view.setText(object.getChannelTitle());
        ((VideosViewHolder) holder).detail_upload_date_view.setText(Localization.localizeDate(mContext, object.getPublishedAt()));
        ((VideosViewHolder) holder).bind(dataSet.get(position), listener);
        //TODO: image will be downloaded from url
        Picasso.get().load(object.getThumbnailMedium()).into(((VideosViewHolder) holder).ImageThumb);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public static class VideosViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView detail_description_view;
        TextView detail_upload_date_view;
        ImageView ImageThumb;

        public VideosViewHolder(View itemView) {
            super(itemView);
            this.textViewTitle = (TextView) itemView.findViewById(R.id.itemTitleView);
            this.detail_description_view = (TextView) itemView.findViewById(R.id.itemUploaderView);
            this.detail_upload_date_view = (TextView) itemView.findViewById(R.id.itemAdditionalDetails);
            this.ImageThumb = (ImageView) itemView.findViewById(R.id.itemThumbnailView);

        }

        public void bind(final YoutubeDataModel item, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }
    }

    public static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView itemUploaderView;
        TextView itemStreamCountView;
        ImageView ImageThumb;

        public PlaylistViewHolder(View itemView) {
            super(itemView);
            this.textViewTitle = (TextView) itemView.findViewById(R.id.itemTitleView);
            this.itemUploaderView = (TextView) itemView.findViewById(R.id.itemUploaderView);
            this.itemStreamCountView = (TextView) itemView.findViewById(R.id.itemStreamCountView);
            this.ImageThumb = (ImageView) itemView.findViewById(R.id.itemThumbnailView);

        }

        public void bind(final YoutubeDataModel item, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }
    }

    public static class ChannelViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView itemUploaderView;
        TextView detail_upload_date_view;
        ImageView ImageThumb;

        public ChannelViewHolder(View itemView) {
            super(itemView);
            this.textViewTitle = (TextView) itemView.findViewById(R.id.itemTitleView);
            this.itemUploaderView = (TextView) itemView.findViewById(R.id.itemUploaderView);
            this.detail_upload_date_view = (TextView) itemView.findViewById(R.id.itemAdditionalDetails);
            this.ImageThumb = (ImageView) itemView.findViewById(R.id.itemThumbnailView);

        }

        public void bind(final YoutubeDataModel item, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}
