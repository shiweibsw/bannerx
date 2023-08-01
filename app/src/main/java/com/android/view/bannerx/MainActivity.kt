package com.android.view.bannerx

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.android.view.bannerx.databinding.ActivityMainBinding
import com.android.view.bannerx.library.MediaBean

class MainActivity : AppCompatActivity() {
    private lateinit var bingding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bingding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        bingding.apply {
            val banners1 = mutableListOf(
                "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4",
                "http://vjs.zencdn.net/v/oceans.mp4",
            )
            val banners2 = mutableListOf(
                "https://media.w3.org/2010/05/sintel/trailer.mp4",
                "https://t7.baidu.com/it/u=938052523,709452322&fm=193&f=GIF",
            )
            val banner0 = mutableListOf<String>(
                "https://t7.baidu.com/it/u=1956604245,3662848045&fm=193&f=GIF",
                "https://t7.baidu.com/it/u=825057118,3516313570&fm=193&f=GIF",
                "https://t7.baidu.com/it/u=938052523,709452322&fm=193&f=GIF"
            )
            val banners = mutableListOf<MediaBean>()
            banners.addAll(banner0.map { MediaBean(it) })

            btnToggle.setOnClickListener {
                banners.clear()
                banners.addAll(banners1.map { MediaBean(it) })
                bannerX.setDatas(banners)
            }
            btnToggle2.setOnClickListener {
                banners.clear()
                banners.addAll(banners2.map { MediaBean(it) })
                bannerX.setDatas(banners)
            }
            bannerX.apply {
                setImagePlayer(GlideImageBanner())
                setDatas(banners)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bingding.bannerX.destroy()
    }
}