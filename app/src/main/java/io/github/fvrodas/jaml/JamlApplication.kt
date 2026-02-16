package io.github.fvrodas.jaml

import android.app.Application
import android.content.pm.ApplicationInfo
import io.github.fvrodas.jaml.core.coreModule
import io.github.fvrodas.jaml.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class JamlApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

        startKoin {
            androidLogger(if (isDebuggable) Level.ERROR else Level.NONE)
            androidContext(this@JamlApplication)
            modules(appModule, coreModule)
        }

    }

}

