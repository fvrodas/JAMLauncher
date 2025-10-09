package io.github.fvrodas.jaml.core.domain.usecases

import io.github.fvrodas.jaml.core.common.usecases.UseCase
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo
import io.github.fvrodas.jaml.core.domain.repositories.ApplicationsRepository

class GetApplicationsListUseCase(
    private val repository: ApplicationsRepository
) : UseCase<List<PackageInfo>, Nothing?>() {
    override suspend fun invoke(params: Nothing?): List<PackageInfo> =
        repository.getApplicationsList()
}
