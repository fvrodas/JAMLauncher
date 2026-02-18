package io.github.fvrodas.jaml.core.domain.usecases

import io.github.fvrodas.jaml.core.common.usecases.UseCase
import io.github.fvrodas.jaml.core.domain.entities.IconConfig
import io.github.fvrodas.jaml.core.domain.repositories.ApplicationsRepository

class ClearIconsAndReloadUseCase(
    private val repository: ApplicationsRepository
) : UseCase<Unit, IconConfig>() {
    override suspend fun invoke(params: IconConfig) {
        repository.clearIconCacheAndLoad(
            params.shouldUseThemedIcons,
            params.backgroundColor,
            params.foregroundColor
        )
    }
}
