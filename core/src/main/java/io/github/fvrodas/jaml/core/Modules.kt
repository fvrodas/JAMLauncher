package io.github.fvrodas.jaml.core

import androidx.annotation.Keep
import io.github.fvrodas.jaml.core.data.repositories.ConcreteApplicationsRepository
import io.github.fvrodas.jaml.core.domain.repositories.ApplicationsRepository
import io.github.fvrodas.jaml.core.domain.usecases.ClearIconsAndReloadUseCase
import io.github.fvrodas.jaml.core.domain.usecases.GetApplicationsListUseCase
import io.github.fvrodas.jaml.core.domain.usecases.GetShortcutsListForApplicationUseCase
import io.github.fvrodas.jaml.core.domain.usecases.LaunchApplicationShortcutUseCase
import org.koin.dsl.module

@Keep
val coreModule = module {
    single<ApplicationsRepository> { ConcreteApplicationsRepository(get()) }
    factory { GetApplicationsListUseCase(get()) }
    factory { GetShortcutsListForApplicationUseCase(get()) }
    factory { LaunchApplicationShortcutUseCase(get()) }
    factory { ClearIconsAndReloadUseCase(get()) }
}
