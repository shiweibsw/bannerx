package com.android.view.bannerx.library.video

import android.content.Context
import android.net.Uri
import android.os.*
import android.view.TextureView
import com.android.view.bannerx.library.util.PlayerConfigUtil
import com.danikula.videocache.HttpProxyCacheServer
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.util.Util

class DefaultBannerPlayer(private var context: Context) : BannerVideoPlayer {
    companion object {
        /** The maximum interval between time bar position updates. */
        const val MAX_UPDATE_INTERVAL_MS = 1000L

        /** The default minimum interval between time bar position updates.  */
        const val DEFAULT_TIME_BAR_MIN_UPDATE_INTERVAL_MS = 200L
    }

    private var trackSelector: DefaultTrackSelector
    private var trackSelectorParameters: DefaultTrackSelector.Parameters
    private var realPlayer: ExoPlayer? = null
    private var mainHandler: Handler = Handler(Looper.getMainLooper())
    private var proxy: HttpProxyCacheServer? = null
    private val listeners: HashSet<InnerPlayerListener> = HashSet()

    init {
        val handlerThread = HandlerThread("download_image")
        handlerThread.start()
        val dataSourceFactory = PlayerConfigUtil.getDataSourceFactory(context)

        val builder = DefaultTrackSelector.Parameters.Builder(context)
        trackSelectorParameters = builder.build()

        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

        trackSelector = DefaultTrackSelector(context)
        trackSelector.parameters = trackSelectorParameters

        realPlayer = ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setTrackSelector(trackSelector)
            .build()

        realPlayer?.addListener(PlayerEventListener())
        realPlayer?.addAnalyticsListener(EventLogger())
        realPlayer?.setAudioAttributes(AudioAttributes.DEFAULT, true)
    }

    inner class PlayerEventListener : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            super.onPlaybackStateChanged(state)
            when (state) {
                Player.STATE_READY -> {
                    for (listener in listeners) {
                        listener.onReady(getDuration())
                    }
                }

                Player.STATE_ENDED -> {
                    for (listener in listeners) {
                        listener.onComplete()
                    }
                }

                Player.STATE_BUFFERING -> {
                }

                Player.STATE_IDLE -> {
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            for (listener in listeners) {
                listener.onPlayingChanged(isPlaying)
            }
            if (isPlaying) {
                mainHandler.post(updateProgressAction)
            } else {
                mainHandler.removeCallbacks(updateProgressAction)
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            for (listener in listeners) {
                listener.onError()
            }
        }
    }

    private val updateProgressAction = Runnable { updateProgress() }

    private fun updateProgress() {
        // Cancel any pending updates and schedule a new one if necessary.
        mainHandler.removeCallbacks(updateProgressAction)
        val playbackState = realPlayer?.playbackState
        if (isPlaying()) {
            val duration: Long = getDuration()
            val position: Long = getCurrentPosition()
            for (listener in listeners) {
                listener.updateProgress(duration, position)
            }
            var mediaTimeDelayMs = MAX_UPDATE_INTERVAL_MS
            // Limit delay to the start of the next full second to ensure position display is smooth.
            val mediaTimeUntilNextFullSecondMs = 1000 - position % 1000
            mediaTimeDelayMs = mediaTimeDelayMs.coerceAtMost(mediaTimeUntilNextFullSecondMs)
            // Calculate the delay until the next update in real time, taking playback speed into account.
            val playbackSpeed: Float = realPlayer?.playbackParameters?.speed ?: 1.0f
            var delayMs =
                if (playbackSpeed > 0) (mediaTimeDelayMs / playbackSpeed).toLong() else MAX_UPDATE_INTERVAL_MS
            // Constrain the delay to avoid too frequent / infrequent updates.
            delayMs = Util.constrainValue(
                delayMs,
                DEFAULT_TIME_BAR_MIN_UPDATE_INTERVAL_MS,
                MAX_UPDATE_INTERVAL_MS
            )
            mainHandler.postDelayed(updateProgressAction, delayMs)
        } else if (playbackState != Player.STATE_ENDED && playbackState != Player.STATE_IDLE) {
            mainHandler.postDelayed(updateProgressAction, MAX_UPDATE_INTERVAL_MS)
        }
    }

    private fun createMediaItem(url: String): MediaItem {
        var realUrl = url
        if (url.startsWith("http")) {
            getProxy()?.let {
                realUrl = it.getProxyUrl(url)
            }
        }
        val mediaItemBuilder = MediaItem.Builder()
        mediaItemBuilder.setUri(Uri.parse(realUrl))
        return mediaItemBuilder.build()
    }

    private fun getProxy(): HttpProxyCacheServer? {
        return if (proxy == null) newProxy().also { proxy = it } else proxy
    }

    private fun newProxy(): HttpProxyCacheServer {
        return HttpProxyCacheServer(context)
    }

    //=====================public method=========================================

    /**
     * get the duration time
     */
    override fun getDuration(): Long {
        realPlayer?.let { return it.duration }
        return -1
    }

    /**
     * get current position
     */
    override fun getCurrentPosition(): Long {
        realPlayer?.let { return it.currentPosition }
        return -1
    }

    /**
     * Quickly locate the playback position
     */
    override fun seekTo(positionMs: Long) {
        realPlayer?.seekTo(positionMs)
    }

    override fun addEventListener(listener: InnerPlayerListener) {
        listeners.add(listener)
    }

    override fun removeEventListener(listener: InnerPlayerListener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener)
        }
    }

    override fun setPlayWhenReady(flag: Boolean) {
        realPlayer?.playWhenReady = flag
    }

    override fun play() {
        realPlayer?.play()
    }

    override fun prepare(url: String?) {
        url?.let {
            realPlayer?.setMediaItem(createMediaItem(it))
            realPlayer?.prepare()
        }
    }

    override fun setRepeatMode(repeatMode: Int) {
        realPlayer?.repeatMode = repeatMode
    }

    override fun isPlaying(): Boolean {
        realPlayer?.let { return it.isPlaying }
        return false
    }

    override fun release() {
        getProxy()?.shutdown()
        mainHandler.removeCallbacks(updateProgressAction)
        realPlayer?.stop()
        realPlayer?.release()
        realPlayer = null
    }

    override fun setVideoTextureView(textureView: TextureView?) {
        realPlayer?.setVideoTextureView(textureView)
    }

    override fun pause() {
        realPlayer?.pause()
    }

    override fun stop() {
        realPlayer?.stop()
    }

}