package io.github.fvrodas.jaml.core.domain.usecases

import io.github.fvrodas.jaml.core.common.usecases.UseCase
import io.github.fvrodas.jaml.core.domain.entities.AppInfo
import io.github.fvrodas.jaml.core.domain.repositories.IApplicationsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetApplicationsListUseCase(
    private val repository: IApplicationsRepository,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : UseCase<List<AppInfo>, Nothing?>() {
    override suspend fun invoke(ignoredParams: Nothing?): Result<List<AppInfo>> =
        withContext(coroutineDispatcher) {
            return@withContext repository.getApplicationsList()
        }
}