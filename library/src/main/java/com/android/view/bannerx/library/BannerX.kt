package com.android.view.bannerx.library

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.Gravity
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import com.android.view.bannerx.library.image.BannerImageLoader
import com.android.view.bannerx.library.video.BannerVideoPlayer
import com.android.view.bannerx.library.video.DefaultBannerPlayer
import com.android.view.bannerx.library.video.InnerPlayerListener
import com.google.android.exoplayer2.Player.REPEAT_MODE_OFF
import com.google.android.exoplayer2.Player.REPEAT_MODE_ONE
import java.lang.IllegalArgumentException

class BannerX : FrameLayout {
    private val TAG = "BannerX=========="

    companion object {
        const val HANDLER_PLAY_TASK = 0
        const val MIN_LOOP_TIME: Long = 1000
        const val DEFAULT_LOOP_TIME: Long = 5000
    }

    /**
     *  each item show time ,default is 3 second.
     */
    private var mLoopTime: Long = DEFAULT_LOOP_TIME

    /**
     * current item index ,default 0
     */
    private var currentIndex = 0

    /**
     * video player play when ready
     */
    private var playWhenReady: Boolean = true

    /**
     * Actual data
     */
    private var banners = mutableListOf<MediaBean>()

    /**
     * video player
     */
    private var videoPlayer: BannerVideoPlayer? = null

    /**
     * image loader
     */
    private var imageLoader: BannerImageLoader? = null

    private var loadingView: ProgressBar? = null

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context, attrs, defStyle
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        initAttrs(attrs)
    }

    @SuppressLint("Recycle")
    private fun initAttrs(attrs: AttributeSet?) {
        attrs?.let {
            val a = context.obtainStyledAttributes(attrs, R.styleable.BannerX)
            mLoopTime =
                a.getInt(R.styleable.BannerX_bannerx_loop_time, DEFAULT_LOOP_TIME.toInt()).toLong()
            a.recycle()
        }
    }


    val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                HANDLER_PLAY_TASK -> {
                    currentIndex = (++currentIndex) % banners.size
                    playItem()
                }
            }
        }
    }

    private fun clearHandlerTask() {
        if (mHandler.hasMessages(HANDLER_PLAY_TASK)) mHandler.removeMessages(HANDLER_PLAY_TASK)
    }

    private fun playItem() {
        if (banners.indices.contains(currentIndex)) {
            if (banners[currentIndex].isVideo()) {
                playVideo(currentIndex)
            } else {
                playImage(currentIndex)
            }
        }
    }

    private fun getVideoPlayer(): BannerVideoPlayer {
        if (videoPlayer == null) initDefaultVideoPlayer()
        return videoPlayer!!
    }

    private fun getImagePlayer(): BannerImageLoader? {
        if (imageLoader == null) throw IllegalArgumentException("Image player can not be empty1")
        return imageLoader
    }


    private fun initDefaultVideoPlayer() {
        videoPlayer = DefaultBannerPlayer(context)
        setInnerPlayerListener()
    }

    private fun setInnerPlayerListener() {
        videoPlayer?.addEventListener(object : InnerPlayerListener {
            override fun onComplete() {
                if (banners.size > 1) mHandler.sendEmptyMessageDelayed(HANDLER_PLAY_TASK, 0)
            }

            override fun onError() {
                if (banners.size > 1) mHandler.sendEmptyMessageDelayed(HANDLER_PLAY_TASK, 0)
            }

            override fun onBuffering() {
                loadingView = ProgressBar(context)
                addView(
                    loadingView, LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER
                    )
                )
            }

            override fun onReady(duration: Long) {
                removeView(loadingView)
                loadingView = null
            }
        })
    }

    private fun playVideo(position: Int) {
        removeAllViews()
        val textureView = TextureView(context)
        addView(textureView)
        getVideoPlayer().setVideoTextureView(textureView)
        getVideoPlayer().prepare(banners[position].url)
        getVideoPlayer().setPlayWhenReady(playWhenReady)
        if (banners.size == 1) getVideoPlayer().setRepeatMode(REPEAT_MODE_ONE)
        else getVideoPlayer().setRepeatMode(REPEAT_MODE_OFF)

    }

    private fun playImage(position: Int) {
        removeAllViews()
        if (getVideoPlayer().isPlaying()) pauseVideo()
        val imageView = ImageView(context)
        getImagePlayer()?.showImage(banners[position].url, imageView)
        addView(imageView)
        if (banners.size > 1) mHandler.sendEmptyMessageDelayed(HANDLER_PLAY_TASK, mLoopTime)
    }


    private fun pauseVideo() {
        if (getVideoPlayer().isPlaying()) {
            getVideoPlayer().pause()
            getVideoPlayer().stop()
        }
    }

    private fun releaseVideo() {
        pauseVideo()
        getVideoPlayer().release()
    }

    //======================================public method==============================

    /**
     * set data's for banner
     */
    fun setInstance(l: MutableList<MediaBean>) {
        if (getVideoPlayer().isPlaying()) pauseVideo()
        clearHandlerTask()
        currentIndex = 0
        banners.clear()
        banners.addAll(l)
        playItem()
    }

    /**
     * Set current item
     */
    fun setCurrentItem(position: Int) {
        if (position < 0 || position >= banners.size) {
            return
        }
        currentIndex = position
        clearHandlerTask()
        playItem()
    }

    internal fun seVideoPlayer(player: BannerVideoPlayer) {
        videoPlayer = player
        setInnerPlayerListener()
    }

    fun setImagePlayer(player: BannerImageLoader) {
        imageLoader = player
    }

    fun setVideoPlayWhenReady(flag: Boolean) {
        playWhenReady = flag
    }

    /**
     * Set the loop time for each item
     */
    fun setLoopTime(time: Long) {
        mLoopTime = if (time < MIN_LOOP_TIME) {
            DEFAULT_LOOP_TIME
        } else {
            time
        }
    }

    fun destroy() {
        releaseVideo()
        clearHandlerTask()
        banners.clear()
        removeAllViews()
    }
}

data class MediaBean(val url: String) {
    fun isVideo(): Boolean {
        return url.endsWith("mp4")
    }
}