package com.android.view.bannerx.library.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.android.view.bannerx.library.R
import com.android.view.bannerx.library.extends.dp2px
import com.android.view.bannerx.library.widget.RCRelativeLayout

/**
 * @Author shiwei
 * @Date 2021/8/26-10:28
 * @Email shiweibsw@gmail.com
 */
open class DefaultRcLayoutAdapter<T>(
    private var radius: Float,
    private var banners: MutableList<T>,
) :
    BannerXAdapter<T, DefaultRcLayoutAdapter.DefaultImageHolder>(banners) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefaultImageHolder {
        val rootView = LayoutInflater.from(parent.context)
            .inflate(R.layout.bannerx_defalut_item_rclayout, parent, false) as RCRelativeLayout
        rootView.setRadius(parent.context.dp2px(radius))
        return DefaultImageHolder(rootView)
    }

    override fun getItemCount(): Int = banners.size

    class DefaultImageHolder internal constructor(rootView: RCRelativeLayout) :
        RecyclerView.ViewHolder(rootView) {
        var img: ImageView = rootView.findViewById(R.id.ivImg)
    }

}