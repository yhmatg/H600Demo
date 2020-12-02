package com.android.sourthuhf.jgjdemo.ui.scandetail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.android.sourthuhf.original.BaseActivity;
import com.android.sourthuhf.original.BaseFragment;
import com.android.sourthuhf.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class DetailActivity extends BaseActivity {
    @BindView(R.id.viewpager)
    ViewPager mViewpager;
    private ArrayList<BaseFragment> fragments = new ArrayList<>();

    @Override
    protected void initEventAndData() {
        fragments.add(new PictureFragmentOne());
        fragments.add(new PictureFragmentTwo());
        mViewpager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments == null ? 0 : fragments.size();
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_tool_detail_two;
    }
}
