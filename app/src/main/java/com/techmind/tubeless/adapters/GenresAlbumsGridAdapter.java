package com.techmind.tubeless.adapters;

import android.content.Context;
import android.content.Intent;
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

import com.techmind.tubeless.GenresVideosList;
import com.techmind.tubeless.R;
import com.techmind.tubeless.pojo.VideosGridCategory;

import java.util.List;

public class GenresAlbumsGridAdapter extends RecyclerView.Adapter<GenresAlbumsGridAdapter.MyViewHolder> {

    private final int viewGridOrHorizontal;
    private Context mContext;
    private List<VideosGridCategory> albumList;
    private View itemView;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public ImageView thumbnail, overflow;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
//                overflow = (ImageView) view.findViewById(R.id.overflow);
        }
    }


    public GenresAlbumsGridAdapter(Context mContext, List<VideosGridCategory> albumList,int viewGridOrHorizontal) {
        this.mContext = mContext;
        this.albumList = albumList;
        this.viewGridOrHorizontal = viewGridOrHorizontal;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewGridOrHorizontal==1) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.genres_card_grid_adaptor, parent, false);
        }else {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.genres_card_horizontal_adaptor, parent, false);
        }

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        VideosGridCategory album = albumList.get(position);
        holder.title.setText(album.getName());
        holder.thumbnail.setBackground(mContext.getResources().getDrawable(album.getThumbnail()));
        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, GenresVideosList.class);
                intent.putExtra("genresName", album.getName());
                intent.putExtra("genresThumbnail", album.getThumbnail());
                mContext.startActivity(intent);
            }
        });
        // loading album cover using Glide library
//            Picasso.get().load(album.getThumbnail()).into(holder.thumbnail.setBackground(););

//            holder.overflow.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    showPopupMenu(holder.overflow);
//                }
//            });
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
