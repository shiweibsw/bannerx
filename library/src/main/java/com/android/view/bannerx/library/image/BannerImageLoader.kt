package com.android.view.bannerx.library.image

import android.widget.ImageView

interface BannerImageLoader {
    fun showImage(url: String, imageView: ImageView)
}