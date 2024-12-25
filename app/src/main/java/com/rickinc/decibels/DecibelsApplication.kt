package com.rickinc.decibels

import android.app.Application
import com.rickinc.decibels.di.dataModule
import com.rickinc.decibels.di.databaseModule
import com.rickinc.decibels.di.networkModule
import com.rickinc.decibels.di.playerModule
import com.rickinc.decibels.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class DecibelsApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(MyDebugTree())

        startKoin {
            androidContext(this@DecibelsApplication)
            modules(databaseModule, dataModule, networkModule, playerModule, viewModelModule)
        }
    }

    class MyDebugTree : Timber.DebugTree() {

        override fun createStackElementTag(element: StackTraceElement): String? {
            return String.format(
                " [M:%s] [C:%s]",
                element.methodName,
                super.createStackElementTag(element)
            )
        }
    }
}