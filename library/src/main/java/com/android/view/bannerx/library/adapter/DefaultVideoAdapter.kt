package com.android.view.bannerx.library.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.view.bannerx.library.R
import com.android.view.bannerx.library.util.ControllerUtil
import com.android.view.bannerx.library.video.BannerPlayer
import com.android.view.bannerx.library.video.InnerPlayerListener

open class DefaultVideoAdapter(
    private var player: BannerPlayer,
    private var banners: MutableList<String>,
) :
    BannerXAdapter<String, DefaultVideoAdapter.DefaultVideoHolder>(banners) {
    private  val TAG = "BaishiweiBannerX"

    override fun getItemViewType(position: Int): Int {
        return if (banners[position].endsWith("mp4")) TYPE_VIDEO else TYPE_IMG
    }

    override fun getItemCount(): Int = banners.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefaultVideoHolder {
        return if (viewType == TYPE_IMG) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.bannerx_default_item_img, parent, false)
            DefaultVideoHolder(player, view)
        } else {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.bannerx_default_item_video, parent, false)
            DefaultVideoHolder(player, view)
        }
    }

    override fun onViewAttachedToWindow(holder: DefaultVideoHolder) {
        super.onViewAttachedToWindow(holder)
        if (holder.itemViewType == TYPE_VIDEO)
            holder.addPlayerListener()
    }

    override fun onViewDetachedFromWindow(holder: DefaultVideoHolder) {
        super.onViewDetachedFromWindow(holder)
        if (holder.itemViewType == TYPE_VIDEO)
            holder.removePlayerListener()
    }

    override fun onBindViewHolder(holder: DefaultVideoHolder, position: Int) {
        holder.bind(position, banners[position], getItemViewType(position))
    }

    inner class DefaultVideoHolder internal constructor(
        var player: BannerPlayer,
        private val view: View
    ) : RecyclerView.ViewHolder(view) {
        var ivImg: ImageView? = null
        private var controllPanel: RelativeLayout? = null
        private var ivStatus: ImageView? = null
        private var durationView: TextView? = null
        private var positionView: TextView? = null
        private var seekBar: SeekBar? = null
        private var dragging = false
        internal fun bind(p: Int, item: String, type: Int) {
            Log.i(TAG, "bind: item:$item--position:$p")
            if (type == TYPE_IMG) {
                ivImg = view.findViewById(R.id.ivImg)
            } else {
                val videoView = view.findViewById<TextureView>(R.id.videoView)
                textureViews[p] = videoView
                videoUrls[p] = item

                controllPanel = view.findViewById(R.id.controllPanel)
                seekBar = view.findViewById(R.id.seekBar)
                positionView = view.findViewById(R.id.positionView)
                durationView = view.findViewById(R.id.durationView)
                ivStatus = view.findViewById(R.id.ivStatus)
                ivStatus?.setOnClickListener {
                    if (player.isPlaying()) {
                        player.pause()
                    } else {
                        player.play()
                    }
                }
                videoView.setOnClickListener {
                    controllPanel?.let {
                        if (it.translationY == it.height.toFloat()) {
                            it.slideIn()
                        } else {
                            it.slideOut()
                        }
                    }
                }
                seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) = Unit

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        dragging = true
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        dragging = false
                        player.seekTo(seekBar.progress * player.getDuration() / ControllerUtil.PROGRESS_BAR_MAX)
                    }
                })
            }
        }

        fun addPlayerListener() {
            player.addEventListener(listener)
        }

        fun removePlayerListener() {
            player.removeEventListener(listener)
        }

        var listener = object : InnerPlayerListener {
            override fun onPlayingChanged(isPlaying: Boolean) {
                ivStatus?.let {
                    if (isPlaying) {
                        it.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
                    } else {
                        it.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
                    }
                }
            }

            override fun updateProgress(duration: Long, position: Long) {
                if (!dragging) {
                    seekBar?.progress = ControllerUtil.progressBarValue(duration, position)
                    positionView?.text = ControllerUtil.stringForTime(position)
                }
            }

            override fun onReady(duration: Long) {
                seekBar?.max = ControllerUtil.PROGRESS_BAR_MAX
                durationView?.text = ControllerUtil.stringForTime(duration)
            }
        }

        fun View.slideOut() {
            if (translationY == 0f) animate().setInterpolator(AccelerateInterpolator())
                .setDuration(500).translationY(height.toFloat())
        }

        fun View.slideIn(autoSlideOut: Boolean = true, delay: Long = 5000L) {
            if (translationY == height.toFloat()) animate().setInterpolator(DecelerateInterpolator())
                .setDuration(500).translationY(0f)
            if (autoSlideOut) {
                removeCallbacks(slideOutAction)
                postDelayed(slideOutAction, delay)
            }
        }

        private val slideOutAction = Runnable { controllPanel?.slideOut() }
    }
}