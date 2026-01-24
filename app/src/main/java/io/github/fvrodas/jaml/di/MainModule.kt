package io.github.fvrodas.jaml.di

import io.github.fvrodas.jaml.ui.launcher.viewmodels.HomeViewModel
import io.github.fvrodas.jaml.ui.settings.viewmodels.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { androidContext().getSharedPreferences("preferences", android.content.Context.MODE_PRIVATE)}
    viewModel { HomeViewModel(get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get()) }
}
