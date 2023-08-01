package com.android.view.bannerx.library.video

import android.view.TextureView
import com.google.android.exoplayer2.Player

interface BannerVideoPlayer {
    fun play()
    fun pause()
    fun stop()
    fun release()
    fun isPlaying(): Boolean
    fun getDuration(): Long
    fun getCurrentPosition(): Long
    fun seekTo(positionMs: Long)
    fun setVideoTextureView(textureView: TextureView?)
    fun prepare(url: String?)
    fun setPlayWhenReady(flag: Boolean)
    fun addEventListener(listener: InnerPlayerListener)
    fun removeEventListener(listener: InnerPlayerListener)
    fun setRepeatMode(@Player.RepeatMode repeatMode: Int)
}