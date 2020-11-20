package com.android.sourthuhf.jgjdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.android.sourthuhf.R;
import com.youth.banner.Banner;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class DetailActivity extends AppCompatActivity {
    private Unbinder unBinder;
    @BindView(R.id.banner)
    Banner banner;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tool_detail);
        unBinder = ButterKnife.bind(this);
        ArrayList<Integer> images = new ArrayList<>();
        images.add(R.mipmap.pic_four);
        images.add(R.mipmap.pic_five);
        banner.setImageLoader(new GlideImageLoader());
        //设置图片集合
        banner.setImages(images);
        banner.isAutoPlay(false);
        //banner设置方法全部调用完毕时最后调用
        banner.start();

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
