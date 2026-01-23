package io.github.fvrodas.jaml

import android.app.Application
import io.github.fvrodas.jaml.core.coreModule
import io.github.fvrodas.jaml.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class JamlApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.ERROR else Level.NONE)
            androidContext(this@JamlApplication)
            modules(appModule, coreModule)
        }

    }
}
