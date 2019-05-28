package com.techmind.tubeless.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.techmind.tubeless.R;
import com.techmind.tubeless.pojo.VideosGridCategory;

import java.util.List;

public class MusicGenresAdapter extends RecyclerView.Adapter<MusicGenresAdapter.MyViewHolder> {

        private Context mContext;
        private List<VideosGridCategory> albumList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView title, count;
            public ImageView thumbnail, overflow;

            public MyViewHolder(View view) {
                super(view);
                title = (TextView) view.findViewById(R.id.title);
                count = (TextView) view.findViewById(R.id.count);
                thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
                overflow = (ImageView) view.findViewById(R.id.overflow);
            }
        }


        public MusicGenresAdapter(Context mContext, List<VideosGridCategory> albumList) {
            this.mContext = mContext;
            this.albumList = albumList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.genres_card_grid_adapter, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            VideosGridCategory album = albumList.get(position);
            holder.title.setText(album.getName());
            holder.count.setText(album.getNumOfSongs() + " songs");

            // loading album cover using Glide library
            Picasso.get().load(album.getThumbnail()).into(holder.thumbnail);

            holder.overflow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPopupMenu(holder.overflow);
                }
            });
        }

        /**
         * Showing popup menu when tapping on 3 dots
         */
        private void showPopupMenu(View view) {
            // inflate menu
            PopupMenu popup = new PopupMenu(mContext, view);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.menu_album, popup.getMenu());
            popup.setOnMenuItemClickListener(new MyMenuItemClickListener());
            popup.show();
        }

        /**
         * Click listener for popup menu items
         */
        class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

            public MyMenuItemClickListener() {
            }

            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_add_favourite:
                        Toast.makeText(mContext, "Add to favourite", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.action_play_next:
                        Toast.makeText(mContext, "Play next", Toast.LENGTH_SHORT).show();
                        return true;
                    default:
                }
                return false;
            }
        }

        @Override
        public int getItemCount() {
            return albumList.size();
        }
    }
