package io.github.fvrodas.jaml.core.domain.usecases

import io.github.fvrodas.jaml.core.common.usecases.UseCase
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo
import io.github.fvrodas.jaml.core.domain.repositories.ApplicationsRepository

class LaunchApplicationShortcutUseCase(
    private val repository: ApplicationsRepository
) : UseCase<Unit, PackageInfo.ShortcutInfo>() {
    override suspend fun invoke(params: PackageInfo.ShortcutInfo) {
        repository.launchShortcut(params)
    }
}
