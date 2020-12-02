package com.android.sourthuhf.jgjdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.android.sourthuhf.original.BaseFragment;
import com.android.sourthuhf.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class DetailActivity extends AppCompatActivity {
    private Unbinder unBinder;
    /*@BindView(R.id.banner)
    Banner banner;*/
    @BindView(R.id.viewpager)
    ViewPager mViewpager;
    private ArrayList<BaseFragment> fragments = new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tool_detail_two);
        unBinder = ButterKnife.bind(this);
       /* ArrayList<Integer> images = new ArrayList<>();
        images.add(R.mipmap.pic_four);
        images.add(R.mipmap.pic_five);
        banner.setImageLoader(new GlideImageLoader());
        //设置图片集合
        banner.setImages(images);
        banner.isAutoPlay(false);
        //banner设置方法全部调用完毕时最后调用
        banner.start();*/
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
    protected void onDestroy() {
        super.onDestroy();
        if (unBinder != null && unBinder != Unbinder.EMPTY) {
            unBinder.unbind();
            unBinder = null;
        }
    }
}
