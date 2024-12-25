package com.rickinc.decibels

import android.app.Application
import com.rickinc.decibels.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class DecibelsApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(MyDebugTree())

        startKoin {
            androidContext(this@DecibelsApplication)
            modules(appModule)
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