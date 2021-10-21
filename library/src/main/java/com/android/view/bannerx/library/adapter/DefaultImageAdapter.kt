package com.android.view.bannerx.library.adapter

import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

/**
 * @Author shiwei
 * @Date 2021/8/26-10:28
 * @Email shiweibsw@gmail.com
 */
open class DefaultImageAdapter<T>(
    private var banners: MutableList<T>,
) :
    BannerXAdapter<T, DefaultImageAdapter.DefaultImageHolder>(banners) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefaultImageHolder {
        val imageView = ImageView(parent.context)
        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        imageView.layoutParams = params
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        return DefaultImageHolder(imageView)
    }

    override fun getItemCount(): Int = banners.size

    class DefaultImageHolder internal constructor(var img: ImageView) : RecyclerView.ViewHolder(img)

}