package com.techmind.tubeless.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.techmind.tubeless.R;
import com.techmind.tubeless.models.YoutubeCommentModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by mdmunirhossain on 1/4/18.
 */

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.YoutubeCommentHolder> {

    private ArrayList<YoutubeCommentModel> dataSet;
    private static Context mContext = null;

    public CommentAdapter(Context mContext, ArrayList<YoutubeCommentModel> data) {
        this.dataSet = data;
        this.mContext = mContext;
    }

    @Override
    public CommentAdapter.YoutubeCommentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.youtube_comment_layout, parent, false);
        YoutubeCommentHolder postHolder = new YoutubeCommentHolder(view);
        return postHolder;
    }

    @Override
    public void onBindViewHolder(YoutubeCommentHolder holder, int position) {
        TextView detail_video_title_view = holder.detail_video_title_view;
        TextView feedback = holder.feedback;
        ImageView imageView = holder.imageViewIcon;
        YoutubeCommentModel object = dataSet.get(position);
        detail_video_title_view.setText(object.getTitle());
        feedback.setText(object.getComment());
        try {
            if (object.getThumbnailHigh() != null) {
                if (object.getThumbnailHigh().startsWith("http")) {
                    Picasso.get()
                            .load(object.getThumbnailHigh())
                            .into(imageView);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public static class YoutubeCommentHolder extends RecyclerView.ViewHolder {

        TextView detail_video_title_view;
        TextView feedback;
        ImageView imageViewIcon;

        public YoutubeCommentHolder(View itemView) {
            super(itemView);
            this.detail_video_title_view = (TextView) itemView.findViewById(R.id.detail_video_title_view);
            this.imageViewIcon = (ImageView) itemView.findViewById(R.id.profile_image);
            this.feedback = (TextView) itemView.findViewById(R.id.feedback);

        }

    }

}


