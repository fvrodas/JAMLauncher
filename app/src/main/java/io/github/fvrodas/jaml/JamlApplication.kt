package io.github.fvrodas.jaml

import android.app.Application
import android.content.SharedPreferences
import io.github.fvrodas.jaml.core.coreModule
import io.github.fvrodas.jaml.features.launcher.presentation.viewmodels.AppsViewModel
import io.github.fvrodas.jaml.features.settings.presentation.viewmodels.SettingsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module

class JamlApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(if(BuildConfig.DEBUG) Level.ERROR else Level.NONE)
            androidContext(this@JamlApplication)
            modules(appModule, coreModule)
        }
    }
}

val appModule = module {
    single { androidApplication().getSharedPreferences("preferences", android.content.Context.MODE_PRIVATE)}
    viewModel { AppsViewModel(get(), get(), get()) }
    viewModel { SettingsViewModel(get()) }
}