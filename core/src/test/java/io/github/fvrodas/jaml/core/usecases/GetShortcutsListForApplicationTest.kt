package io.github.fvrodas.jaml.core.usecases

import io.github.fvrodas.jaml.core.domain.entities.AppInfo
import io.github.fvrodas.jaml.core.domain.entities.AppShortcutInfo
import io.github.fvrodas.jaml.core.domain.repositories.IApplicationsRepository
import io.github.fvrodas.jaml.core.domain.usecases.GetShortcutsListForApplicationUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class GetShortcutsListForApplicationTest {

    @Mock
    private lateinit var repository: IApplicationsRepository

    private lateinit var useCase: GetShortcutsListForApplicationUseCase

    private val exampleAppInfo = AppInfo(
        packageName = "com.example.package_name",
        label = "Example App",
        icon = null,
        hasNotification = false
    )

    private val expectedResult = listOf(
        AppShortcutInfo(
            id = "id",
            packageName = "com.example.package_name",
            label = "Example Shortcut",
            icon = null
        )
    )

    @Test
    fun getShortcutsListForApplication_shouldReturnList() {
        runBlocking {
            useCase = GetShortcutsListForApplicationUseCase(
                repository = repository
            )

            `when`(repository.getShortcutsListForApplication(exampleAppInfo.packageName)).thenReturn(
                Result.success(
                    expectedResult
                )
            )

            val result = useCase(exampleAppInfo.packageName)

            assert(result.isSuccess)
            assert(expectedResult == result.getOrNull())
        }
    }


}