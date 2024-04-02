package io.github.fvrodas.jaml.core

import io.github.fvrodas.jaml.core.data.repositories.ApplicationRepository
import io.github.fvrodas.jaml.core.domain.repositories.IApplicationsRepository
import io.github.fvrodas.jaml.core.domain.usecases.GetApplicationsListUseCase
import io.github.fvrodas.jaml.core.domain.usecases.GetShortcutsListForApplicationUseCase
import io.github.fvrodas.jaml.core.domain.usecases.LaunchApplicationShortcutUseCase
import org.koin.dsl.module

val coreModule = module {
    single<IApplicationsRepository> { ApplicationRepository(get()) }
    single { GetApplicationsListUseCase(get()) }
    single { GetShortcutsListForApplicationUseCase(get()) }
    single { LaunchApplicationShortcutUseCase(get()) }
}