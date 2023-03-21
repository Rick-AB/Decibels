package com.rickinc.decibels.presentation.nowplaying

import com.rickinc.decibels.domain.exception.ErrorHolder

sealed interface NowPlayingBottomSheetState {
    object Loading : NowPlayingBottomSheetState
    data class LyricsLoaded(val lyrics: String) : NowPlayingBottomSheetState
    data class ErrorLoadingLyrics(val error: ErrorHolder) : NowPlayingBottomSheetState
}