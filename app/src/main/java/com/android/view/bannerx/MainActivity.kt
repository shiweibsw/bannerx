package com.android.view.bannerx

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.android.view.bannerx.databinding.ActivityMainBinding
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
            val banners = mutableListOf<String>()
            val root = Environment.getExternalStorageDirectory().absolutePath + File.separator
            banners.addAll(
                listOf(
                    "http://hengdawb-res.oss-cn-hangzhou.aliyuncs.com/zlg_test_res/video01.mp4",
                    "http://hengdawb-res.oss-cn-hangzhou.aliyuncs.com/zlg_test_res/video02.mp4",
                    "${root}HD_DSP_TJBWG_CH/EXHIBIT/4/lb_img.png",
                    "${root}HD_DSP_TJBWG_CH/EXHIBIT/5/lb_img.png",
                    "${root}HD_DSP_TJBWG_CH/EXHIBIT/11/lb_img.jpg"
                )
            )
            bannerX.apply {
                val indicatorView = IndicatorView(this@MainActivity)
                    .setIndicatorColor(Color.BLACK)
                    .setIndicatorSelectorColor(Color.WHITE)
                    .setIndicatorStyle(INDICATOR_CIRCLE_RECT)
                setIndicator(indicatorView)
                setAdapter(object : DefaultVideoAdapter(bannerX.getPlayer(), banners) {
                    override fun onBindViewHolder(holder: DefaultVideoHolder, position: Int) {
                        super.onBindViewHolder(holder, position)
                        if (getItemViewType(position) == TYPE_IMG)
                            Glide.with(holder.itemView).load(banners[position]).into(holder.ivImg!!)
                    }
                })
                useThreePagesOnOneScreen(32.0f, 32.0f, 0.7f)
                start()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bingding.bannerX.destroy()
    }
}