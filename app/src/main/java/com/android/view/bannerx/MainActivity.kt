package com.android.view.bannerx

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.android.view.bannerx.databinding.ActivityMainBinding
import com.android.view.bannerx.library.adapter.DefaultVideoAdapter
import com.android.view.bannerx.library.indicator.IndicatorView
import com.android.view.bannerx.library.indicator.IndicatorView.IndicatorStyle.*
import com.bumptech.glide.Glide

class MainActivity : AppCompatActivity() {
    private lateinit var bingding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bingding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        bingding.apply {

            val banners1 = mutableListOf<String>(
                "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4",
                "http://vjs.zencdn.net/v/oceans.mp4",

            )
            val banners2 = mutableListOf<String>(
                "https://media.w3.org/2010/05/sintel/trailer.mp4",
                "https://t7.baidu.com/it/u=938052523,709452322&fm=193&f=GIF",

            )
//            val banners = mutableListOf<String>(
//                "https://t7.baidu.com/it/u=1956604245,3662848045&fm=193&f=GIF",
//                "https://t7.baidu.com/it/u=825057118,3516313570&fm=193&f=GIF",
//                "https://t7.baidu.com/it/u=938052523,709452322&fm=193&f=GIF"
//            )
            val banners= mutableListOf<String>()
            banners.addAll(banners1)

            btnToggle.setOnClickListener {
                banners.clear()
                banners.addAll(banners1)
                bannerX.setNewDatas(banners)
            }
            btnToggle2.setOnClickListener {
                banners.clear()
                banners.addAll(banners2)
                bannerX.setNewDatas(banners)
            }

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

                val mAdapter = object : DefaultVideoAdapter(bannerX.getPlayer(), banners) {
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