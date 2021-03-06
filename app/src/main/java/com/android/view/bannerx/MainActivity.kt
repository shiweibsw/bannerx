package com.android.view.bannerx

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.widget.ViewPager2
import com.android.view.bannerx.databinding.ActivityMainBinding
import com.android.view.bannerx.library.BannerX.Companion.OVER_LAP
import com.android.view.bannerx.library.BannerX.Companion.ZOOM_OUT
import com.android.view.bannerx.library.adapter.BannerXAdapter
import com.android.view.bannerx.library.adapter.DefaultImageAdapter
import com.android.view.bannerx.library.adapter.DefaultRcLayoutAdapter
import com.android.view.bannerx.library.adapter.DefaultVideoAdapter
import com.android.view.bannerx.library.indicator.IndicatorView
import com.android.view.bannerx.library.indicator.IndicatorView.IndicatorStyle.*
import com.android.view.bannerx.library.transformer.*
import com.bumptech.glide.Glide
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var bingding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bingding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        bingding.apply {
//            val banners = mutableListOf<String>(
//                "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4",
//                "http://vjs.zencdn.net/v/oceans.mp4",
//                "https://media.w3.org/2010/05/sintel/trailer.mp4"
//            )
            val banners = mutableListOf<String>(
                "https://t7.baidu.com/it/u=1956604245,3662848045&fm=193&f=GIF",
                "https://t7.baidu.com/it/u=825057118,3516313570&fm=193&f=GIF",
                "https://t7.baidu.com/it/u=938052523,709452322&fm=193&f=GIF"
            )
            bannerX.apply {
                val indicatorView = IndicatorView(this@MainActivity)
                    .setIndicatorColor(Color.BLACK)
                    .setIndicatorSelectorColor(Color.WHITE)
                    .setIndicatorStyle(INDICATOR_CIRCLE)
                setIndicator(indicatorView)
//                val mAdapter = object : DefaultImageAdapter<String>(banners) {
//                    override fun onBindViewHolder(holder: DefaultImageHolder, position: Int) {
//                        super.onBindViewHolder(holder, position)
//                        Glide.with(holder.itemView).load(banners[position]).into(holder.img)
//                    }
//                }
//                val mAdapter = object : DefaultRcLayoutAdapter<String>(10.0f, banners) {
//                    override fun onBindViewHolder(holder: DefaultImageHolder, position: Int) {
//                        super.onBindViewHolder(holder, position)
//                        Glide.with(holder.itemView).load(banners[position]).into(holder.img)
//                    }
//                }

                var mAdapter = object : DefaultVideoAdapter(bannerX.getPlayer(), banners) {
                    override fun onBindViewHolder(holder: DefaultVideoHolder, position: Int) {
                        super.onBindViewHolder(holder, position)
                        if (getItemViewType(position) == TYPE_IMG)
                            Glide.with(holder.itemView).load(banners[position]).into(holder.ivImg!!)
                    }
                }
                setAdapter(mAdapter)
                start()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bingding.bannerX.destroy()
    }
}