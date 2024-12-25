package com.rickinc.decibels.di

import com.rickinc.decibels.presentation.features.home.tracklist.TrackListMenuViewModel
import com.rickinc.decibels.presentation.features.nowplaying.NowPlayingViewModel
import com.rickinc.decibels.presentation.features.home.tracklist.TrackListViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::TrackListViewModel)
    viewModelOf(::NowPlayingViewModel)
    viewModelOf(::TrackListMenuViewModel)
}