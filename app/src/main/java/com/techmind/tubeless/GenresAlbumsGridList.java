package com.techmind.tubeless;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import com.techmind.tubeless.adapters.GenresAlbumsGridAdapter;
import com.techmind.tubeless.pojo.VideosGridCategory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenresAlbumsGridList extends AppCompatActivity {
    private RecyclerView recyclerView;
    private GenresAlbumsGridAdapter adapter;
    private List<VideosGridCategory> albumList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.genres_albums_grid_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.mList_videos);

        albumList = new ArrayList<>();
        //Grid view=1 change view
        adapter = new GenresAlbumsGridAdapter(this, albumList,1);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(5), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        prepareAlbums();
    }

    /**
     * Adding few albums for testing
     */
    private void prepareAlbums() {
        int[] covers = new int[]{
                R.drawable.gradient_1,
                R.drawable.gradient_2,
                R.drawable.gradient_3,
                R.drawable.gradient_4,
                R.drawable.gradient_5,
                R.drawable.gradient_6,
                R.drawable.gradient_7,
                R.drawable.gradient_8,
                R.drawable.gradient_9,
                R.drawable.gradient_10,
                R.drawable.gradient_11,
                R.drawable.gradient_12};
        List<String> musicGenres = Arrays.asList(getResources().getStringArray(R.array.music_genres));
        int coverGrandientColor=0;
        for (int i = 0; i < musicGenres.size(); i++) {
            System.out.println("coverGradientColor = " +"I="+i+ "="+coverGrandientColor);
            albumList.add(new VideosGridCategory(musicGenres.get(i), 13, covers[coverGrandientColor]));
            //Endless gradient color for card
            if (coverGrandientColor==covers.length-1) {
                coverGrandientColor=0;
            } else {
                coverGrandientColor++;
            }
        }
        adapter.notifyDataSetChanged();
    }
    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    public static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
}