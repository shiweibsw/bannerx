package com.android.view.bannerx;

import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.android.view.bannerx.library.image.BannerImagePlayer;
import com.bumptech.glide.Glide;

/**
 * @Author shiwei
 * @Date 2023/8/1-11:27
 * @Email shiweibsw@gmail.com
 */
class GlideImageBanner implements BannerImagePlayer {
    @Override
    public void showImage(@NonNull String url, @NonNull ImageView imageView) {
        Glide.with(imageView.getContext()).load(url).centerCrop().into(imageView);
    }
}
