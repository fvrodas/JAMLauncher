package io.github.fvrodas.jaml.core.domain.usecases

import io.github.fvrodas.jaml.core.common.usecases.UseCase
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo
import io.github.fvrodas.jaml.core.domain.repositories.ApplicationsRepository

class GetShortcutsListForApplicationUseCase(
    private val repository: ApplicationsRepository
) : UseCase<List<PackageInfo.ShortcutInfo>, String>() {
    override suspend fun invoke(params: String): List<PackageInfo.ShortcutInfo> =
        repository.getShortcutsListForApplication(params)
}
