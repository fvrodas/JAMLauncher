package io.github.fvrodas.jaml.core

import io.github.fvrodas.jaml.core.data.repositories.ConcreteApplicationsRepository
import io.github.fvrodas.jaml.core.domain.repositories.ApplicationsRepository
import io.github.fvrodas.jaml.core.domain.usecases.GetApplicationsListUseCase
import io.github.fvrodas.jaml.core.domain.usecases.GetShortcutsListForApplicationUseCase
import io.github.fvrodas.jaml.core.domain.usecases.LaunchApplicationShortcutUseCase
import org.koin.dsl.module

val coreModule = module {
    single<ApplicationsRepository> { ConcreteApplicationsRepository(get()) }
    single { GetApplicationsListUseCase(get()) }
    single { GetShortcutsListForApplicationUseCase(get()) }
    single { LaunchApplicationShortcutUseCase(get()) }
}