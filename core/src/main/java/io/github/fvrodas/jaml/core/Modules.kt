package io.github.fvrodas.jaml.core

import io.github.fvrodas.jaml.core.data.repositories.ApplicationRepository
import io.github.fvrodas.jaml.core.data.repositories.ShortcutsUtil
import io.github.fvrodas.jaml.core.domain.repositories.IApplicationsRepository
import io.github.fvrodas.jaml.core.domain.usecases.GetApplicationsListUseCase
import io.github.fvrodas.jaml.core.domain.usecases.GetShortcutsListForApplicationUseCase
import org.koin.dsl.module

val coreModule = module {
    single { ShortcutsUtil(get()) }
    single<IApplicationsRepository> { ApplicationRepository(get(), get()) }
    single { GetApplicationsListUseCase(get()) }
    single { GetShortcutsListForApplicationUseCase(get()) }
}