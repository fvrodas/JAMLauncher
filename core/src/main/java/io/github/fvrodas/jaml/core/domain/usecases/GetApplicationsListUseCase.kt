package io.github.fvrodas.jaml.core.domain.usecases

import io.github.fvrodas.jaml.core.common.usecases.UseCase
import io.github.fvrodas.jaml.core.domain.entities.AppInfo
import io.github.fvrodas.jaml.core.domain.repositories.IApplicationsRepository

class GetApplicationsListUseCase(
    private val repository: IApplicationsRepository
) : UseCase<List<AppInfo>, Nothing?>() {
    override suspend fun invoke(params: Nothing?): List<AppInfo> =
        repository.getApplicationsList()
}
