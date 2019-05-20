package com.techmind.tubeless.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.techmind.tubeless.fragments.DemoFragment;
import com.techmind.tubeless.fragments.PopularVideosFragment;

;


public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                PopularVideosFragment tab1 = new PopularVideosFragment();
                return tab1;
            case 1:
                DemoFragment tab2 = new DemoFragment();
                return tab2;
//            case 2:
//                Demo1Fragment tab3 = new Demo1Fragment();
//                return tab3;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
