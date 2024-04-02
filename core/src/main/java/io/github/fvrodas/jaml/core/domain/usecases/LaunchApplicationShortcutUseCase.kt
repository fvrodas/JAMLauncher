package io.github.fvrodas.jaml.core.domain.usecases

import io.github.fvrodas.jaml.core.common.usecases.UseCase
import io.github.fvrodas.jaml.core.domain.entities.AppShortcutInfo
import io.github.fvrodas.jaml.core.domain.repositories.IApplicationsRepository

class LaunchApplicationShortcutUseCase(
    private val repository: IApplicationsRepository
) : UseCase<Unit, AppShortcutInfo>() {
    override suspend fun invoke(params: AppShortcutInfo) {
        repository.launchShortcut(params)
    }
}
