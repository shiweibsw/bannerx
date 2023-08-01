package com.android.view.bannerx.library.video

interface InnerPlayerListener {
    fun onReady(duration: Long) {}
    fun onError() {}
    fun onComplete() {}
    fun onIDLE() {}
    fun onBuffering() {}
    fun updateProgress(duration: Long, position: Long) {}
    fun onPlayingChanged(isPlaying: Boolean) {}
}