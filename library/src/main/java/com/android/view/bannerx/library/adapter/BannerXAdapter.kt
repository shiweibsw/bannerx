package com.android.view.bannerx.library.adapter

import android.view.TextureView
import androidx.recyclerview.widget.RecyclerView

abstract class BannerXAdapter<T, VH : RecyclerView.ViewHolder>(var mDatas: MutableList<T>) :
    RecyclerView.Adapter<VH>() {

    companion object {
        const val TYPE_IMG = 1
        const val TYPE_VIDEO = 2
    }

    var textureViews = arrayOfNulls<TextureView>(mDatas.size)

    var videoUrls = arrayOfNulls<String>(mDatas.size)

    internal fun getTextureView(position: Int): TextureView? {
        return textureViews[position]
    }

    internal fun getVideoUrl(position: Int): String? {
        return videoUrls[position]
    }

    fun setDatas(datas: MutableList<T>) {
        mDatas.clear()
        mDatas.addAll(datas)
        textureViews = arrayOfNulls(mDatas.size)
        videoUrls = arrayOfNulls(mDatas.size)
        notifyDataSetChanged()
    }

    fun getDatas(): MutableList<T> = mDatas

    fun getData(position: Int): T {
        return mDatas[position]
    }

    override fun onBindViewHolder(holder: VH, position: Int) {}



}