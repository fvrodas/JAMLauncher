package io.github.fvrodas.jaml.core.usecases

import io.github.fvrodas.jaml.core.domain.entities.AppInfo
import io.github.fvrodas.jaml.core.domain.repositories.IApplicationsRepository
import io.github.fvrodas.jaml.core.domain.usecases.GetApplicationsListUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class GetApplicationsListUseCaseTest {

    @Mock
    private lateinit var repository: IApplicationsRepository

    private lateinit var useCase: GetApplicationsListUseCase

    private val expectedResult = listOf(
        AppInfo(
            packageName = "com.example.package_name",
            label = "Example App",
            icon = null,
            hasNotification = false
        )
    )

    @Test
    fun getApplicationsList_shouldReturnList() {

        runBlocking {
            useCase = GetApplicationsListUseCase(
                repository = repository
            )

            `when`(repository.getApplicationsList()).thenReturn(expectedResult)

            val result = useCase(null)

            assert(expectedResult == result)
        }

    }
}
