package com.rickinc.decibels.presentation.util

import java.util.concurrent.TimeUnit

fun formatTrackDuration(long: Long): String {
    return String.format(
        "%02d:%02d",
        TimeUnit.MILLISECONDS.toMinutes(long) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(long)),
        TimeUnit.MILLISECONDS.toSeconds(long) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(long))
    )
}