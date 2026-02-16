package io.github.fvrodas.jaml.di

import io.github.fvrodas.jaml.AppConfig
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.ui.launcher.viewmodels.HomeViewModel
import io.github.fvrodas.jaml.ui.settings.viewmodels.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        AppConfig(androidContext())
    }
    single {
        val directBootContext = androidContext().createDeviceProtectedStorageContext()
        directBootContext.getSharedPreferences(
            androidContext().getString(R.string.prefs_name),
            android.content.Context.MODE_PRIVATE
        )
    }
    viewModel {
        HomeViewModel(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel {
        SettingsViewModel(
            get()
        )
    }
}
