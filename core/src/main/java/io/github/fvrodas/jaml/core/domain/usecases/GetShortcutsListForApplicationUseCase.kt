package io.github.fvrodas.jaml.core.domain.usecases

import io.github.fvrodas.jaml.core.common.usecases.UseCase
import io.github.fvrodas.jaml.core.domain.entities.AppShortcutInfo
import io.github.fvrodas.jaml.core.domain.repositories.IApplicationsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetShortcutsListForApplicationUseCase(
    private val repository: IApplicationsRepository,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : UseCase<List<AppShortcutInfo>, String>() {
    override suspend fun invoke(params: String): Result<List<AppShortcutInfo>> =
        withContext(coroutineDispatcher) {
            return@withContext repository.getShortcutsListForApplication(params)
        }
}