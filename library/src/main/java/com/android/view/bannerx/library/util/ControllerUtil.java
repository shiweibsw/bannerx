package com.android.view.bannerx.library.util;

import com.google.android.exoplayer2.C;

import java.util.Formatter;
import java.util.Locale;

public class ControllerUtil {

    public static final int PROGRESS_BAR_MAX = 1000;
    public static final StringBuilder formatBuilder = new StringBuilder();
    public static final Formatter formatter = new Formatter(formatBuilder, Locale.getDefault());

    public static int progressBarValue(long duration, long position) {
        return duration == C.TIME_UNSET || duration == 0 ? 0
                : (int) ((position * PROGRESS_BAR_MAX) / duration);
    }

    public static String stringForTime(long timeMs) {
        if (timeMs == C.TIME_UNSET) {
            timeMs = 0;
        }
        long totalSeconds = (timeMs + 500) / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;
        formatBuilder.setLength(0);
        return hours > 0 ? formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
                : formatter.format("%02d:%02d", minutes, seconds).toString();
    }
}
