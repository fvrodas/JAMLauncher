package io.github.fvrodas.jaml.domain.usecases

import android.content.SharedPreferences
import io.github.fvrodas.jaml.core.common.usecases.UseCase
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo
import io.github.fvrodas.jaml.ui.common.settings.LauncherPreferences
import kotlinx.serialization.json.Json

class GetGroupedApplicationsListUseCase(
    private val sharedPreferences: SharedPreferences
) : UseCase<List<PackageInfo>, Nothing?>() {
    override suspend fun invoke(params: Nothing?): List<PackageInfo> {
        return sharedPreferences.getString(LauncherPreferences.APP_FOLDERS, null)?.let { apps ->
            Json.decodeFromString(apps)
        } ?: emptyList()
    }
}
