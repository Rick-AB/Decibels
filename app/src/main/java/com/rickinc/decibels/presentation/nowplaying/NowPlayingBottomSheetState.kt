package com.rickinc.decibels.presentation.nowplaying

sealed interface NowPlayingBottomSheetState {
    object Loading : NowPlayingBottomSheetState
    data class LyricsLoaded(val lyrics: String) : NowPlayingBottomSheetState
    data class ErrorLoadingLyrics(val errorMessage: String) : NowPlayingBottomSheetState
}