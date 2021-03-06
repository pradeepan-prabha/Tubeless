package com.techmind.tubeless.adapters;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.techmind.tubeless.R;
import com.techmind.tubeless.config.ConstURL;
import com.techmind.tubeless.interfaces.OnItemClickListener;
import com.techmind.tubeless.models.YoutubeDataModel;
import com.techmind.tubeless.util.Localization;

import java.util.ArrayList;

import static com.techmind.tubeless.util.Localization.localizeDate;

/**
 * Created by mdmunirhossain on 12/18/17.
 */

public class FavouriteGridViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Activity activity;
    private ArrayList<YoutubeDataModel> dataSet;
    private Context mContext = null;
    private final OnItemClickListener listener;


    public FavouriteGridViewAdapter(Activity mContext, ArrayList<YoutubeDataModel> dataSet, ArrayList<YoutubeDataModel> recyclerView, OnItemClickListener listener) {
        this.dataSet = dataSet;
        this.mContext = mContext;
        this.activity = mContext;
        this.listener = listener;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        switch (viewType) {
            case ConstURL.CHANNEL_ID_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.favourite_channel_adapter, parent, false);
                return new ChannelViewHolder(view);
            case ConstURL.PLAYLIST_ID_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.favouriate_playlist_adapter, parent, false);
                return new PlaylistViewHolder(view);
            case ConstURL.VIDEOS_ID_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.favourite_video_adapter, parent, false);
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
//        ((ChannelViewHolder) holder).itemUploaderView.setText(object.getChannelTitle());
        ((ChannelViewHolder) holder).detail_upload_date_view.setText(getDetailViewsSub(object));
        ((ChannelViewHolder) holder).bind(dataSet.get(position), listener);
        //TODO: image will be downloaded from url
        Picasso.get().load(object.getThumbnailHigh()).into(((ChannelViewHolder) holder).ImageThumb);
    }

    private void setPlayListHolderUI(RecyclerView.ViewHolder holder, YoutubeDataModel object, int position) {
        ((PlaylistViewHolder) holder).textViewTitle.setText(object.getTitle());
        ((PlaylistViewHolder) holder).itemUploaderView.setText(object.getChannelTitle());
        ((PlaylistViewHolder) holder).itemPlaylistCount.setText(getDetailVideosCount(object));
        ((PlaylistViewHolder) holder).bind(dataSet.get(position), listener);
        //TODO: image will be downloaded from url
        Picasso.get().load(object.getThumbnailHigh()).into(((PlaylistViewHolder) holder).ImageThumb);
    }

    private void setVideoHolderUI(RecyclerView.ViewHolder holder, YoutubeDataModel object, int position) {
        ((VideosViewHolder) holder).textViewTitle.setText(object.getTitle());
        ((VideosViewHolder) holder).detail_description_view.setText(object.getChannelTitle());
//        ((VideosViewHolder) holder).detail_upload_date_view.setText(getStreamInfoDetailLine(object));
        if (!object.getDuration().isEmpty()) {
            ((VideosViewHolder) holder).detail_duration_view.setText(Localization.getDurationString(object.getDuration()));
            ((VideosViewHolder) holder).detail_duration_view.setBackgroundColor(ContextCompat.getColor(mContext,
                    R.color.duration_background_color));
            ((VideosViewHolder) holder).detail_duration_view.setVisibility(View.VISIBLE);
        }
//               if (item.duration > 0) {
//            ((VideosViewHolder) holder).detail_duration_view.setText(Localization.getDurationString(item.duration));
//             ((VideosViewHolder) holder).detail_duration_view.setBackgroundColor(ContextCompat.getColor(itemBuilder.getContext(),
//                    R.color.duration_background_color));
//            itemDurationView.setVisibility(View.VISIBLE);
//        } else {
//            itemDurationView.setVisibility(View.GONE);
//        }
//        System.out.println("Localization.getDurationString(checkEmptyStr(object.getDuration())) = " + Localization.getDurationString(checkEmptyStr(object.getDuration())));
        ((VideosViewHolder) holder).bind(dataSet.get(position), listener);
        //TODO: image will be downloaded from url
        Picasso.get().load(object.getThumbnailHigh()).into(((VideosViewHolder) holder).ImageThumb);
    }

        private long checkEmptyStr(String duration) {
//       if (item.duration > 0) {
//            itemDurationView.setText(Localization.getDurationString(item.duration));
//            itemDurationView.setBackgroundColor(ContextCompat.getColor(itemBuilder.getContext(),
//                    R.color.duration_background_color));
//            itemDurationView.setVisibility(View.VISIBLE);
//        } else {
//            itemDurationView.setVisibility(View.GONE);
//        }
        return 0;
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public static class VideosViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView detail_description_view;
        TextView detail_duration_view;
//        TextView detail_upload_date_view;
        ImageView ImageThumb;

        public VideosViewHolder(View itemView) {
            super(itemView);
            this.textViewTitle = (TextView) itemView.findViewById(R.id.textViewTitle);
            this.detail_description_view = (TextView) itemView.findViewById(R.id.detail_description_view);
//            this.detail_upload_date_view = (TextView) itemView.findViewById(R.id.itemAdditionalDetails);
            this.detail_duration_view = (TextView) itemView.findViewById(R.id.detail_duration_view);
            this.ImageThumb = (ImageView) itemView.findViewById(R.id.imageThumb);

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
        TextView itemPlaylistCount;
        ImageView ImageThumb;

        public PlaylistViewHolder(View itemView) {
            super(itemView);
            this.textViewTitle = (TextView) itemView.findViewById(R.id.itemTitleView);
            this.itemUploaderView = (TextView) itemView.findViewById(R.id.itemUploaderView);
            this.itemPlaylistCount = (TextView) itemView.findViewById(R.id.itemPlaylistCount);
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
    private String getStreamInfoDetailLine(final YoutubeDataModel infoItem) {
        String viewsAndDate = "";
        try
        {
        if (!infoItem.getViewCount().isEmpty()) {
            if (Long.parseLong(infoItem.getViewCount()) >= 0) {
                viewsAndDate = Localization.shortViewCount(activity, Long.parseLong(infoItem.getViewCount()));
            }}
            if (!TextUtils.isEmpty(infoItem.getPublishedAt())) {
                if (viewsAndDate.isEmpty()) {
                    viewsAndDate = localizeDate(mContext,infoItem.getPublishedAt());
                } else {
                    viewsAndDate += " • " + localizeDate(mContext,infoItem.getPublishedAt());
                }

        }
        }catch(NumberFormatException ex){ // handle your exception
            System.out.println("NumberFormatException ="+ex);
        }
        return viewsAndDate;
    }
    private String getDetailViewsSub(final YoutubeDataModel item) {
        String details = "";
        if (!item.getSubscriberCount().isEmpty()&&Long.parseLong(item.getSubscriberCount()) >= 0) {
            details += Localization.shortSubscriberCount(activity,
                    Long.parseLong(item.getSubscriberCount()));
        }
        if (!item.getVideoCount().isEmpty()&&Long.parseLong(item.getVideoCount() )>= 0) {
            String formattedVideoAmount = Localization.localizeStreamCount(activity,
                    Long.parseLong(item.getVideoCount()));

            if (!details.isEmpty()) {
                details += " • " + formattedVideoAmount;
            } else {
                details = formattedVideoAmount;
            }
        }

        return details;
    } private String getDetailVideosCount(final YoutubeDataModel item) {
        String details = "";
        if (!item.getVideoCount().isEmpty()&&Long.parseLong(item.getVideoCount() )>= 0) {
            String formattedVideoAmount = Localization.localizeStreamCount(activity,
                    Long.parseLong(item.getVideoCount()));

            if (!details.isEmpty()) {
                details += " • " + formattedVideoAmount;
            } else {
                details = formattedVideoAmount;
            }
        }

        return details;
    }

}
