package com.android.view.bannerx.library

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.ORIENTATION_HORIZONTAL
import com.android.view.bannerx.library.adapter.BannerXAdapter
import com.android.view.bannerx.library.extends.dp2px
import com.android.view.bannerx.library.indicator.Indicator
import com.android.view.bannerx.library.transformer.ZoomOutSlideTransformer
import com.android.view.bannerx.library.util.ScrollSpeedManger
import com.android.view.bannerx.library.video.BannerPlayer
import com.android.view.bannerx.library.video.DefaultBannerPlayer
import com.android.view.bannerx.library.video.InnerPlayerListener
import com.google.android.exoplayer2.Player.REPEAT_MODE_OFF
import com.google.android.exoplayer2.Player.REPEAT_MODE_ONE


/**
 * @Author shiwei
 * @Date 2021/10/18-16:19
 * @Email shiweibsw@gmail.com
 */
class BannerX<T, BA : BannerXAdapter<T, out RecyclerView.ViewHolder>> : FrameLayout {
    private val TAG = "BannerX"
    private val MIN_LOOP_TIME: Long = 1000
    private val DEFUT_LOOP_TIME: Long = 3000
    private val DEFUT_SCROLL_TIME: Int = 800
    private val HANDLER_TASK = 0
    private lateinit var viewPager: ViewPager2
    private lateinit var mCompositePageTransformer: CompositePageTransformer
    private var mIndicator: Indicator? = null
    private var mAdapter: BA? = null

    /**
     * current item index ,default 1
     */
    private var currentItemIndex = 1

    /**
     *  each item show time ,default is 3 second.
     */
    private var mLoopTime: Long = DEFUT_LOOP_TIME

    /**
     *  scroll time for each item
     */
    private var mScrollTime: Int = DEFUT_SCROLL_TIME

    /**
     * Infinite loop flag
     */
    private var isInfiniteLoop = true

    /**
     * Auto loop flag
     */
    private var isAutoLoop = true

    private var mOrientation = ORIENTATION_HORIZONTAL

    /**
     * video player play when ready
     */
    private var playWhenReady: Boolean = true

    private var banners = mutableListOf<T>()

    //====================================================
    private var videoPlayer: BannerPlayer? = null

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
        viewPager = ViewPager2(context)
        viewPager.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        setOrientation(mOrientation)
        ScrollSpeedManger.reflectLayoutManager(this)
        mCompositePageTransformer = CompositePageTransformer()
        viewPager.setPageTransformer(mCompositePageTransformer)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentItemIndex = position
                mIndicator?.let { it.onPageSelected(toRealPosition(position)) }
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                when (state) {
                    ViewPager2.SCROLL_STATE_IDLE -> {
                        if (banners.size > 1) {
                            if (currentItemIndex == banners.size - 1) {
                                clearHandlerTask()
                                if (isInfiniteLoop) {
                                    currentItemIndex = 1
                                    viewPager.setCurrentItem(currentItemIndex, false)
                                }
                                moveToNextItem()
                            } else if (currentItemIndex == 0) {
                                clearHandlerTask()
                                if (isInfiniteLoop) {
                                    currentItemIndex = banners.size - 2
                                    viewPager.setCurrentItem(currentItemIndex, false)
                                }
                                moveToNextItem()
                            } else {
                                clearHandlerTask()
                                moveToNextItem()
                            }
                        }
                    }
                    ViewPager2.SCROLL_STATE_DRAGGING -> {
                        clearHandlerTask()
                    }
                }
                mIndicator?.let { it.onPageScrollStateChanged(state) }
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                mIndicator?.let {
                    it.onPageScrolled(
                        toRealPosition(position),
                        positionOffset,
                        positionOffsetPixels
                    )
                }
            }
        })
        addView(viewPager)
    }

    @SuppressLint("Recycle")
    private fun initAttrs(attrs: AttributeSet?, defStyle: Int) {
        attrs?.let {
            val a = context.obtainStyledAttributes(attrs, R.styleable.BannerX)
            mLoopTime =
                a.getInt(R.styleable.BannerX_bannerx_loop_time, DEFUT_LOOP_TIME.toInt()).toLong()
            isAutoLoop =
                a.getBoolean(R.styleable.BannerX_bannerx_auto_loop, true)
            mOrientation =
                a.getInt(R.styleable.BannerX_bannerx_orientation, ORIENTATION_HORIZONTAL)
            a.recycle()
        }
    }


    val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            currentItemIndex = (++currentItemIndex) % banners.size
            viewPager.currentItem = currentItemIndex
        }
    }

    private fun clearHandlerTask() {
        if (mHandler.hasMessages(HANDLER_TASK))
            mHandler.removeMessages(HANDLER_TASK)
    }

    private fun moveToNextItem() {
        if (getViewPager2().adapter?.getItemViewType(currentItemIndex) == BannerXAdapter.TYPE_VIDEO) {
            postDelayed({ playVideo(currentItemIndex) }, 200)// wait for arrays prepared
        } else {
            pauseVideo()
            if (isAutoLoop && banners.size > 1)
                mHandler.sendEmptyMessageDelayed(HANDLER_TASK, mLoopTime)
        }
    }

    private fun createReaDatas(datas: MutableList<T>) {
        if (banners.isNotEmpty())
            banners.clear()
        banners.add(datas[datas.size - 1])
        banners.addAll(datas)
        banners.add(datas[0])
    }

    private fun getRealCount(): Int {
        if (isInfiniteLoop) {
            return banners.size - 2
        } else {
            return banners.size
        }
    }

    private fun toRealPosition(position: Int): Int {
        if (isInfiniteLoop) {
            var realPosition = 0
            if (getRealCount() > 1) {
                realPosition = (position - 1) % getRealCount()
            }
            if (realPosition < 0) {
                realPosition += getRealCount()
            }
            return realPosition
        } else {
            return position
        }
    }

    private fun getVideoPlayer(): BannerPlayer {
        if (videoPlayer == null)
            initDefaultVideoPlayer()
        return videoPlayer!!
    }

    private fun initDefaultVideoPlayer() {
        videoPlayer = DefaultBannerPlayer(context)
        setInnerPlayerListener()
    }

    private fun setInnerPlayerListener() {
        videoPlayer?.addEventListener(object : InnerPlayerListener {
            override fun onComplete() {
                if (banners.size > 1 && isAutoLoop)
                    mHandler.sendEmptyMessageDelayed(HANDLER_TASK, 0)
            }

            override fun onError() {
                if (banners.size > 1 && isAutoLoop)
                    mHandler.sendEmptyMessageDelayed(HANDLER_TASK, 0)
            }
        })
    }

    private fun playVideo(position: Int) {
        getVideoPlayer().setVideoTextureView(mAdapter?.getTextureView(position))
        getVideoPlayer().prepare(mAdapter?.getVideoUrl(position))
        getVideoPlayer().setPlayWhenReady(playWhenReady)
        if (banners.size == 1)
            getVideoPlayer().setRepeatMode(REPEAT_MODE_ONE)
        else
            getVideoPlayer().setRepeatMode(REPEAT_MODE_OFF)
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

    fun setAdapter(adapter: BA, isInfiniteLoop: Boolean = true) {
        if (adapter.getDatas().isEmpty()) {
            throw IllegalArgumentException("datas can not be empty")
        }
        if (mAdapter != null) {
            throw IllegalArgumentException("adapter can not be bind twice")
        }
        this.isInfiniteLoop = isInfiniteLoop
        if (isInfiniteLoop && adapter.getDatas().size > 1) {
            currentItemIndex = 1
            createReaDatas(adapter.getDatas())
            adapter.setDatas(banners)
        } else {
            currentItemIndex = 0
            banners = adapter.getDatas()
        }
        mAdapter = adapter
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = banners.size
        viewPager.setCurrentItem(currentItemIndex, false)
        mIndicator?.let { it.initIndicatorCount(getRealCount(), toRealPosition(currentItemIndex)) }
    }

    /**
     * set indicator view
     */
    fun setIndicator(indicator: Indicator, attachToRoot: Boolean = true) {
        if (mIndicator != null) {
            removeView(indicator.view)
        }
        mIndicator = indicator
        if (attachToRoot) {
            addView(mIndicator?.view, mIndicator?.params)
        }
    }

    /**
     * If isAutoLoop is true ,this method must be called.
     */
    fun start() {
        moveToNextItem()
    }

    fun destroy() {
        releaseVideo()
        clearHandlerTask()
        banners.clear()
        removeAllViews()
    }

    /**
     * Set wether auto loop ,this method effective immediately.
     */
    fun setIsAutoLoop(flag: Boolean) {
        isAutoLoop = flag
    }

    /**
     * Set wether infinite loop ,this method effective immediately.
     */
    fun setIsInfiniteLoop(flag: Boolean) {
        isInfiniteLoop = flag
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

    /**
     * Get the scroll time
     */
    fun getScrollTime(): Int = mScrollTime

    /**
     * Set the scroller time for each item
     */
    fun setScrollTime(time: Int) {
        mScrollTime = time
    }

    /**
     * Get the real ViewPager2
     */
    fun getViewPager2(): ViewPager2 = viewPager

    /**
     * Adds a page transformer to the list
     */
    fun addPageTransformer(transformer: ViewPager2.PageTransformer) {
        mCompositePageTransformer.addTransformer(transformer)
    }

    /**
     * Removes a page transformer from the list
     */
    fun removePageTransformer(transformer: ViewPager2.PageTransformer) {
        mCompositePageTransformer.removeTransformer(transformer)
    }

    /**
     * Enable or disable user initiated scrolling.
     */
    fun setUserInputEnabled(flag: Boolean) {
        viewPager.isUserInputEnabled = flag
    }

    /**
     * Sets the orientation of the BannerX.
     */
    fun setOrientation(@ViewPager2.Orientation orientation: Int) {
        viewPager.orientation = orientation
    }

    /**
     * Three page on one screen effect
     * use minscale to controll the margin between to items.
     */
    fun useThreePagesOnOneScreen(leftPadding: Float, rightPadding: Float, minScale: Float) {
//        addPageTransformer(OverlapSliderTransformer(getViewPager2().orientation, 0.25f, 0f, 1f, 0f))
        addPageTransformer(ZoomOutSlideTransformer(minScale))
        setPageMargin(leftPadding, rightPadding)
    }

    /**
     * Three page on one screen effect
     */
    fun setPageMargin(leftPadding: Float, rightPadding: Float) {
        val recyclerView = viewPager.getChildAt(0) as RecyclerView
        if (getViewPager2().orientation == ViewPager2.ORIENTATION_VERTICAL) {
            recyclerView.setPadding(
                viewPager.paddingLeft,
                context.dp2px(leftPadding),
                viewPager.paddingRight,
                context.dp2px(rightPadding)
            )
        } else {
            recyclerView.setPadding(
                context.dp2px(leftPadding),
                viewPager.paddingTop,
                context.dp2px(rightPadding),
                viewPager.paddingBottom
            )
        }
        recyclerView.clipToPadding = false
    }

    /**
     * Set current item
     */
    fun setCurrentItem(position: Int) {
        if (position < 0 && position >= banners.size) {
            return
        }
        viewPager.currentItem = position + 1
    }

    /**
     * Get the real video player instance
     * use this to controll player states
     */
    fun getPlayer(): BannerPlayer = getVideoPlayer()

    fun setPlayer(player: BannerPlayer) {
        videoPlayer = player
        setInnerPlayerListener()
    }

    fun setVideoPlayWhenReady(flag: Boolean) {
        playWhenReady = flag
    }
}