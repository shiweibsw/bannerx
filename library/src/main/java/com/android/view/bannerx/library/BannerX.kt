package com.android.view.bannerx.library

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.TextureView
import android.widget.FrameLayout
import android.widget.ImageView
import com.android.view.bannerx.library.image.BannerImagePlayer
import com.android.view.bannerx.library.video.BannerVideoPlayer
import com.android.view.bannerx.library.video.DefaultBannerPlayer
import com.android.view.bannerx.library.video.InnerPlayerListener
import com.google.android.exoplayer2.Player.REPEAT_MODE_OFF
import com.google.android.exoplayer2.Player.REPEAT_MODE_ONE
import java.lang.IllegalArgumentException

data class MediaBean(val url: String) {
    fun isVideo(): Boolean {
        return url.endsWith("mp4")
    }
}

/**
 * @Author shiwei
 * @Date 2021/10/18-16:19
 * @Email shiweibsw@gmail.com
 */
class BannerX : FrameLayout {
    private val TAG = "BaishiweiBannerX"

    companion object {
        const val ZOOM_OUT = 0
        const val OVER_LAP = 1
    }

    private val HANDLER_TASK = 0
    private val MIN_LOOP_TIME: Long = 1000
    private val DEFUT_LOOP_TIME: Long = 3000

    /**
     *  each item show time ,default is 3 second.
     */
    private var mLoopTime: Long = DEFUT_LOOP_TIME

    /**
     * current item index ,default 0
     */
    private var currentItemIndex = 0

    /**
     * video player play when ready
     */
    private var playWhenReady: Boolean = true


    private var banners = mutableListOf<MediaBean>()

    private var videoPlayer: BannerVideoPlayer? = null

    private var imagePlayer: BannerImagePlayer? = null

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        initAttrs(attrs, defStyle)
    }

    @SuppressLint("Recycle")
    private fun initAttrs(attrs: AttributeSet?, defStyle: Int) {
        attrs?.let {
            val a = context.obtainStyledAttributes(attrs, R.styleable.BannerX)
            mLoopTime = a.getInt(R.styleable.BannerX_bannerx_loop_time, DEFUT_LOOP_TIME.toInt()).toLong()
            a.recycle()
        }
    }


    val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            currentItemIndex = (++currentItemIndex) % banners.size
            playItem()
        }
    }

    private fun clearHandlerTask() {
        if (mHandler.hasMessages(HANDLER_TASK))
            mHandler.removeMessages(HANDLER_TASK)
    }

    private fun playItem() {
        if (banners.indices.contains(currentItemIndex)) {
            if (banners[currentItemIndex].isVideo()) {
                playVideo(currentItemIndex)
            } else {
                playImage(currentItemIndex)
            }
        }
    }

    private fun getVideoPlayer(): BannerVideoPlayer {
        if (videoPlayer == null)
            initDefaultVideoPlayer()
        return videoPlayer!!
    }

    private fun getImagePlayer(): BannerImagePlayer? {
        if (imagePlayer == null)
            throw IllegalArgumentException("Image player can not be empty1")
        return imagePlayer
    }



    private fun initDefaultVideoPlayer() {
        videoPlayer = DefaultBannerPlayer(context)
        setInnerPlayerListener()
    }

    private fun setInnerPlayerListener() {
        videoPlayer?.addEventListener(object : InnerPlayerListener {
            override fun onComplete() {
                if (banners.size > 1)
                    mHandler.sendEmptyMessageDelayed(HANDLER_TASK, 0)
            }

            override fun onError() {
                if (banners.size > 1)
                    mHandler.sendEmptyMessageDelayed(HANDLER_TASK, 0)
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
        if (banners.size == 1)
            getVideoPlayer().setRepeatMode(REPEAT_MODE_ONE)
        else
            getVideoPlayer().setRepeatMode(REPEAT_MODE_OFF)

    }

    private fun playImage(position: Int) {
        removeAllViews()
        if (getVideoPlayer().isPlaying())
            pauseVideo()
        val imageView = ImageView(context)
        getImagePlayer()?.showImage(banners[position].url,imageView)
        addView(imageView)
        if (banners.size > 1)
            mHandler.sendEmptyMessageDelayed(HANDLER_TASK, mLoopTime)
    }


    private fun pauseVideo() {
        if (getVideoPlayer().isPlaying()) {
            getVideoPlayer().pause()
            getVideoPlayer().stop()
        }
    }

    private fun releaseVideo() {
        if (getVideoPlayer().isPlaying()) {
            getVideoPlayer().pause()
            getVideoPlayer().stop()
        }
        getVideoPlayer().release()
    }

    //======================================public method==============================

    /**
     * set datas for bannerx
     */
    fun setDatas(l: MutableList<MediaBean>) {
        if (getVideoPlayer().isPlaying())
            pauseVideo()
        clearHandlerTask()
        currentItemIndex = 0
        banners.clear()
        banners.addAll(l)
        playItem()
    }

    /**
     * If isAutoLoop is true ,this method must be called.
     */
    fun start() {
        playItem()
    }

    fun destroy() {
        releaseVideo()
        clearHandlerTask()
        banners.clear()
        removeAllViews()
    }

    /**
     * Set current item
     */
    fun setCurrentItem(position: Int, smoothScroll: Boolean = true) {
        if (position < 0 && position >= banners.size) {
            return
        }
        currentItemIndex = position
        playItem()
    }

    fun seVideoPlayer(player: BannerVideoPlayer) {
        videoPlayer = player
        setInnerPlayerListener()
    }

    fun setImagePlayer(player: BannerImagePlayer) {
        imagePlayer = player
    }

    fun setVideoPlayWhenReady(flag: Boolean) {
        playWhenReady = flag
    }


    /**
     * Set the loop time for each item
     */
    fun setLoopTime(time: Long) {
        if (time < MIN_LOOP_TIME) {
            mLoopTime = DEFUT_LOOP_TIME
        } else {
            mLoopTime = time
        }
    }
}